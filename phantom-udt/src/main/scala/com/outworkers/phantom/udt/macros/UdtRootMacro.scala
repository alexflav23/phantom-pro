package com.outworkers.phantom.udt.macros

import java.nio.BufferUnderflowException

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.outworkers.phantom.macros.RootMacro
import com.outworkers.phantom.udt.Udt
import scala.collection.generic.CanBuildFrom
import scala.reflect.macros.whitebox

@macrocompat.bundle
trait UdtRootMacro extends RootMacro {
  val c: whitebox.Context

  import c.universe._


  val udtPackage = q"com.outworkers.phantom.udt"
  val prefix = q"com.outworkers.phantom.udt"
  val keySpaceTpe = tq"com.outworkers.phantom.dsl.KeySpace"
  val udtValueTpe = tq"com.datastax.driver.core.UDTValue"

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  val executableQuery: Type = typeOf[com.outworkers.phantom.builder.query.execution.ExecutableCqlQuery]
  val emptyOptions = q"com.outworkers.phantom.builder.query.QueryOptions.empty"

  object Symbols {
    val listSymbol: Symbol = typed[scala.collection.immutable.List[_]]
    val setSymbol: Symbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol: Symbol = typed[scala.collection.immutable.Map[_, _]]
    val udtSymbol: Symbol = typed[com.outworkers.phantom.udt.UDTPrimitive[_]]
  }

  /**
    * Checks if the field type on a case class is an UDT type.
    * This logic requires deep checking to deal with the fact that we provide DSL level [[scala.Option]]
    * support for all Cassandra types.
    *
    * Nested UDT types require freezing on Cassandra and if the type is wrapped inside an [[Option]] it is not enough
    * to check if the type is a case class. Simply checking if the type is a case class can also mislead the
    * macro generation and prevent it from using auto-tupled case class primitives with support available via
    * phantom auto-tables.
    * @param tpe The type of the field to check for annotations.
    * @return True if the type contains a UDT annotation or if the type is an Option wrapping an UDT annotated type.
    */
  def deepCaseClass(tpe: Type): Boolean = {
    if (tpe <:< typeOf[scala.Option[_]]) {
      isCaseClass(tpe.typeArgs.head)
    } else {
      isCaseClass(tpe)
    }
  }

  def hasAnnotation(tpe: Type): Boolean = {
    tpe.typeSymbol.typeSignature

    val annotations = tpe.typeSymbol.annotations

    val out = annotations.collect {
      case annot if annot.tree.tpe <:< weakTypeOf[Udt] => annot
    }

    out.nonEmpty
  }

  case class Accessor(
    name: TermName,
    paramType: Type,
    annotations: List[Tree] = Nil
  ) {
    def tpe: TypeName = symbol.name.toTypeName

    def symbol = paramType.typeSymbol
  }

  object Accessors {
    def apply[M[X] <: TraversableOnce[X]](source: M[(Name, Type)])(
      implicit cbf: CanBuildFrom[Nothing, Accessor, M[Accessor]]
    ): M[Accessor] = {
      val builder = cbf()
      for ((nm, tp) <- source) builder += Accessor(nm.toTermName, tp, Nil)
      builder.result()
    }
  }


  /**
    * Retrieves the accessor fields on a case class and returns an iterable of tuples of the form Name -> Type.
    * For every single field in a case class, a reference to the string name and string type of the field are returned.
    *
    * Example:
    *
    * {{{
    *   case class Test(id: UUID, name: String, age: Int)
    *
    *   accessors(Test) = Iterable("id" -> "UUID", "name" -> "String", age: "Int")
    * }}}
    *
    * @param params The list of params retrieved from the case class.
    * @return An iterable of tuples where each tuple encodes the string name and string type of a field.
    */
  def accessors(
    params: Seq[ValDef]
  ): Iterable[Accessor] = {
    params.map {
      case ValDef(mods, name: TermName, tpt: Tree, tree: Tree) => {
        val tpe = c.typecheck(tq"$tpt", c.TYPEmode).tpe
        Accessor(name, tpe, mods.annotations)
      }
    }
  }

  /**
    * The base implementation of an UDT extractor derived from a case class field.
    */
  trait Extractor {

    /**
      * The corresponding case class accessor that encloses the relevant type information.
      * This will include information about the name and the type of the underlying case class field.
      * @return A reference to a case class accessor.
      */
    def accessor: Accessor

    /**
      * The string name of the case class field.
      * This is used to determine the name of the columns in Cassandra.
      * We will use the same name for the UDT columns as the name typed into the case classes
      * by uers.
      * @return A string name for the case filed.
      */
    def field: String = accessor.name.decodedName.toString

    /**
      * The string name of the case class field.
      * This is used to determine the name of the columns in Cassandra.
      * We will use the same name for the UDT columns as the name typed into the case classes
      * by uers.
      * @return A string name for the case filed.
      */
    def schemaField: Tree = {
      if (hasAnnotation(accessor.paramType)) {
        q"$udtPackage.UDTPrimitive[${accessor.paramType}].name"
      } else {
        q"${accessor.name.decodedName.toString}"
      }
    }

    /**
      * The name of the field extractor used in the generated for yield expression.
      * This will need to be the same name across the 2 places where it is required,
      * namely the generated for yield block as well as the companion apply block.
      *
      * Let's look at how the macro expands the annotated case classes with respect to extracting
      * the relevant type information.
      *
      * Example: {{{
      *   @Udt case class Test(id: UUID, text: String)
      *
      *   new UDTPrimitive[Test] {
      *     def fromRow(udt: UDTValue): Option[Test] = {
      *        for {
      *          idOpt <- Primitive[UUID].fromRow("id", udt)
      *          nameOpt <- Primitive[String].fromRow("name", udt)
      *        } yield Test.apply(idOpt, nameOpt)
      *     }
      *   }
      *
      * }}}
      * @return
      */
    def extractorName = TermName(field + "Opt")

    def typeQualifier: Tree

    /**
      * Whether or not this extractor is derived for an underlying UDT type.
      * The parent type of the extractor is always an UDT, but this property marks
      * whether or not we have identified the current field/accessor is also a case class
      * that needs to be treated as an UDT type.
      * @return A boolean value that marks whether or not the current extractor should be
      *         derived for an UDT type.
      */
    def isUdtType: Boolean = deepCaseClass(accessor.paramType)

    /**
      * The tree enclosing the definition of the Cassandra type information of the extractor.
      * This will be provided by implementors as it based on a combination of inner and outer types.
      * @return The CQL type tree that will be used to produce the CQL type string during schema auto-generation.
      */
    def cassandraType: Tree

    def schema: Tree = q"""$field -> $cassandraType"""

    def serializer: Tree
  }

  case class RegularExtractor(
    accessor: Accessor,
    override val typeQualifier: Tree
  ) extends Extractor {
    def cassandraType: Tree = if (isUdtType) {
      val udtName = q"$prefix.UDTPrimitive[${accessor.paramType}].name"
      q"$prefix.Helper.frozen($udtName)"
    } else {
      q"$typeQualifier.cassandraType"
    }

    override def serializer: Tree = q"""$field -> $typeQualifier.asCql(instance.${accessor.name})"""
  }

  case class NestedExtractor(
    accessor: Accessor,
    override val typeQualifier: Tree
  ) extends Extractor {
    def cassandraType: Tree = if (isUdtType) {
      val udtName = q"$prefix.UDTPrimitive[${accessor.paramType}].name"
      q"$prefix.Helper.frozen($udtName)"
    } else {
      q"$typeQualifier.cassandraType"
    }

    override def serializer: Tree = q"""$field -> instance.${accessor.name}.fold("null")(item => $typeQualifier.asCql(item))"""
  }


  /**
    * Derives the full primitive type for a given root type.
    * This will not yield a method call of any kind, it will simply return
    * a reference to the right primitive.
    * @param accessor The root type to compute the primitive type for.
    * @return
    */
  private[this] def derivePrimitive(accessor: Accessor): Extractor = {
    if (accessor.paramType <:< typeOf[scala.Option[_]] && deepCaseClass(accessor.paramType)) {
      val nestedType = accessor.paramType.typeArgs.head
      NestedExtractor(
        accessor.copy(paramType = nestedType),
        q"$primitivePkg.Primitive[$nestedType]"
      )
    } else {
      RegularExtractor(
        accessor,
        q"$primitivePkg.Primitive[${accessor.paramType}]"
      )
    }
  }

  def isCaseClass(sym: Symbol): Boolean = {
    sym.isClass && sym.asClass.isCaseClass
  }

  def isCaseClass(tpe: Type): Boolean = isCaseClass(tpe.typeSymbol)

  def udtDeps(params: Iterable[Accessor]): Seq[Type] = {
    params.foldLeft(Seq.empty[Type]) { case (acc, accessor) =>
      if (isCaseClass(accessor.paramType)) {
        acc :+ accessor.paramType
      } else {
        accessor.symbol match {
          case Symbols.listSymbol | Symbols.setSymbol | Symbols.mapSymbol =>
            acc ++ accessor.paramType.typeArgs.filter(isCaseClass)

          case _ => acc
        }
      }
    }
  }

  def typeDependencies(params: Iterable[Accessor]): Seq[Tree] = {
    udtDeps(params) map (tp => q"$udtPackage.UDTPrimitive[$tp]")
  }

  def queryDependencies(params: Iterable[Accessor]): Seq[Tree] = {
    udtDeps(params) map (tp => q"""$udtPackage.UDTPrimitive[$tp].schemaQuery""")
  }

  def elTerm(i: Int): TermName = TermName(s"el$i")

  def fieldTerm(i: Int): TermName = TermName(s"n$i")

  def fqTerm(i: Int): TermName = TermName(s"fq$i")

  private[this] val boolType = tq"_root_.scala.Boolean"
  private[this] val codecUtils = q"_root_.com.datastax.driver.core.CodecUtils"
  private[this] val pVersion = tq"_root_.com.datastax.driver.core.ProtocolVersion"
  private[this] val bufferType = tq"_root_.java.nio.ByteBuffer"
  private[this] val bufferCompanion = q"_root_.java.nio.ByteBuffer"
  private[this] val bufferException = typeOf[BufferUnderflowException]
  private[this] val invalidTypeException = typeOf[InvalidTypeException]

  def udtPrimitive(tpe: TypeName, stringName: String, params: Iterable[Accessor]): Tree = {

    val sourceTerm = TermName("source")
    val versionTerm = TermName("version")

    val source = params

    val indexedFields = source.zipWithIndex
    val udtFields = source map derivePrimitive

    val sizeComp = indexedFields.map { case (vd, i) =>
      val term = elTerm(i)
      fq"""
      $term <- {
        val $term = $primitivePkg.Primitive[${vd.paramType}].serialize(source.${vd.name}, $versionTerm)
        Some((4 + { if ($term == null) 0 else $term.remaining()}))
      }
    """
    }

    val serializedComponents = indexedFields.map { case (vd, i) =>
      fq""" ${elTerm(i)} <- {
        val serialized = $primitivePkg.Primitive[${vd.paramType}].serialize(source.${vd.name}, $versionTerm)

        val buf = if (serialized == null) {
           res.putInt(-1)
         } else {
           res.putInt(serialized.remaining())
           res.put(serialized.duplicate())
         }
      Some(buf)
    }
    """
    }

    val inputTerm = TermName("input")

    val deserializedFields = indexedFields.map { case (vd, i) =>
      val tm = fieldTerm(i)
      val el = elTerm(i)
      fq"""
       ${fqTerm(i)} <- {
          val $tm = $inputTerm.getInt()
          val $el = if ($tm < 0) { null } else { $codecUtils.readBytes($inputTerm, $tm) }
          Some($primitivePkg.Primitive[${vd.paramType}].deserialize($el, $versionTerm))
       }
    """
    }

    val sumTerm = indexedFields.foldRight(q"") { case ((_, pos), acc) =>
      acc match {
        case t if t.isEmpty => q"${elTerm(pos)}"
        case _ => q"${elTerm(pos)} + $acc"
      }
    }

    val extractorTerms = indexedFields.map { case (_, i) => fqTerm(i) }
    val fieldExtractor = q"for (..$deserializedFields) yield new $tpe(..$extractorTerms)"

    val tree = q"""
    new $prefix.UDTPrimitive[$tpe] {

      def name: $strTpe = $stringName

      def deps()(implicit space: $keySpaceTpe): $collections.Seq[$udtPackage.UDTPrimitive[_]] = {
        $collections.Seq.apply[$prefix.UDTPrimitive[_]](..${typeDependencies(source)})
      }

      def typeDependencies()(implicit space: $keySpaceTpe): $collections.Seq[$executableQuery] = {
        $collections.Seq.apply[$executableQuery](..${queryDependencies(source)})
      }

      override def asCql(instance: $tpe): String = {
        val baseString = $collections.List(..${udtFields.map(_.serializer)}).map {
          case (name, ext) => name + ": " + ext
        } mkString(", ")

        "{" + baseString + "}"
      }

      def schemaQuery()(implicit space: $keySpaceTpe): $executableQuery = {
        val membersList = $collections.List(..${udtFields.map(_.schema)}).map {
          case (name, casType) => name + " " + casType
        }

        val base = "CREATE TYPE IF NOT EXISTS " + space.name + "." + ${stringName.toLowerCase} + " (" + membersList.mkString(", ") + ")"

        new $executableQuery(new $enginePkg.CQLQuery(base), $emptyOptions, Nil)
      }


      override def dataType: $strTpe = $stringName

      override def serialize($sourceTerm: $tpe, $versionTerm: $pVersion): $bufferType = {
        if ($sourceTerm == null) {
           null
        } else {
          val size = {for (..$sizeComp) yield ($sumTerm) } get
          val res = $bufferCompanion.allocate(size)
          val buf = for (..$serializedComponents) yield ()
          buf.get

          res.flip().asInstanceOf[$bufferType]
        }
      }

      override def deserialize($sourceTerm: $bufferType, $versionTerm: $pVersion): $tpe = {
        if ($sourceTerm == null) {
          null
        } else {
          try {
            val $inputTerm = $sourceTerm.duplicate()
            $fieldExtractor.get
          } catch {
            case e: $bufferException =>
              throw new $invalidTypeException("Not enough bytes to deserialize an UDT value", e)
          }
        }
      }

      override def frozen: $boolType = true

      override def shouldFreeze: $boolType = true
    }"""

    if (showTrees) {
      c.echo(c.enclosingPosition, showCode(tree))
    }

    tree
  }


  def makePrimitive(
    tpe: TypeName,
    name: TermName,
    params: List[ValDef]
  ): Tree = {
    val stringName = name.decodedName.toString
    val objName = TermName(c.freshName(stringName + "_udt_primitive"))
    val primitive = udtPrimitive(tpe, stringName, accessors(params))

    val tree = q"""
        implicit val $objName: $prefix.UDTPrimitive[$tpe] = $primitive
     """

    if (showTrees) {
      c.echo(c.enclosingPosition, s"Generated tree for $tpe:\n${showCode(tree)}")
    }

    tree
  }


}
