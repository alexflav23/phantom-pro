package com.outworkers.phantom.udt

import java.nio.BufferUnderflowException

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.macros.RootMacro

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Udt extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro UdtMacroImpl.impl
}

//noinspection ScalaStyle
@macrocompat.bundle
class UdtMacroImpl(val c: whitebox.Context) extends RootMacro {

  import c.universe._

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  object Symbols {
    val listSymbol: Symbol = typed[scala.collection.immutable.List[_]]
    val setSymbol: Symbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol: Symbol = typed[scala.collection.immutable.Map[_, _]]
    val udtSymbol: Symbol = typed[com.outworkers.phantom.udt.UDTPrimitive[_]]
  }

  case class Accessor(
    name: TermName,
    paramType: Type,
    annotations: List[Tree] = Nil
  ) {
    def tpe: TypeName = symbol.name.toTypeName

    def symbol = paramType.typeSymbol
  }

  val primitivePkg = q"com.outworkers.phantom.builder.primitives"
  val udtPackage = q"com.outworkers.phantom.udt"

  val prefix = q"com.outworkers.phantom.udt"
  val keySpaceTpe = tq"com.outworkers.phantom.dsl.KeySpace"
  val cqlQueryTpe = typeOf[CQLQuery]
  val udtValueTpe = tq"com.datastax.driver.core.UDTValue"

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
      case ValDef(mods, name: TermName, tpt: Tree, _) => {
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
      if (isUdtType) {
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

    def typeQualifier: Tree = q""

    /**
      * Whether or not this extractor is derived for an underlying UDT type.
      * The parent type of the extractor is always an UDT, but this property marks
      * whether or not we have identified the current field/accessor is also a case class
      * that needs to be treated as an UDT type.
      * @return A boolean value that marks whether or not the current extractor should be
      *         derived for an UDT type.
      */
    def isUdtType: Boolean = isCaseClass(accessor.paramType)

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
    override val typeQualifier: Tree,
    override val isUdtType: Boolean
  ) extends Extractor {
    def cassandraType: Tree = if (isUdtType) {
      val udtName = q"$prefix.UDTPrimitive[${accessor.paramType}].name"
      q"$prefix.Helper.frozen($udtName)"
    } else {
      q"$typeQualifier.cassandraType"
    }

    override def serializer: Tree = q"""$field -> $typeQualifier.asCql(instance.${accessor.name})"""
  }


  /**
    * Derives the full primitive type for a given root type.
    * This will not yield a method call of any kind, it will simply return
    * a reference to the right primitive.
    * @param accessor The root type to compute the primitive type for.
    * @return
    */
  private[this] def derivePrimitive(accessor: Accessor): Extractor = {
    RegularExtractor(
      accessor,
      q"$primitivePkg.Primitive[${accessor.paramType}]",
      isUdtType = false
    )
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
    udtDeps(params) map (tp =>
      q"""new $udtPackage.query.UDTCreateQuery(
         $udtPackage.UDTPrimitive[$tp].schemaQuery
      )"""
    )
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

  def udtPrimitive(tpe: TypeName, stringName: String, params: List[ValDef]): Tree = {

    val sourceTerm = TermName("source")
    val versionTerm = TermName("version")

    val source = accessors(params)

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

      def typeDependencies()(implicit space: $keySpaceTpe): $collections.Seq[$udtPackage.query.UDTCreateQuery] = {
        $collections.Seq.apply[$udtPackage.query.UDTCreateQuery](..${queryDependencies(source)})
      }

      override def asCql(instance: $tpe): String = {
        val baseString = $collections.List(..${udtFields.map(_.serializer)}).map {
          case (name, ext) => name + ": " + ext
        } mkString(", ")

        "{" + baseString + "}"
      }

      def schemaQuery()(implicit space: $keySpaceTpe): $cqlQueryTpe = {
        val membersList = $collections.List(..${udtFields.map(_.schema)}).map {
          case (name, casType) => name + " " + casType
        }

        val base = "CREATE TYPE IF NOT EXISTS " + space.name + "." + ${stringName.toLowerCase} + " (" + membersList.mkString(", ") + ")"

        $enginePkg.CQLQuery(base)
      }


      override def dataType: $strTpe = $stringName

      override def serialize($sourceTerm: $tpe, $versionTerm: $pVersion): $bufferType = {
        if ($sourceTerm == null) {
           null
        } else {
          val size = {for (..$sizeComp) yield ($sumTerm) } get

          val length = ${udtFields.size}
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
    val primitive = udtPrimitive(tpe, stringName, params)

    val tree = q"""
        implicit val $objName: $prefix.UDTPrimitive[$tpe] = $primitive
     """

    if (showTrees) {
      c.echo(c.enclosingPosition, s"Generated tree for $tpe:\n${showCode(tree)}")
    }

    tree
  }

  def impl(annottees: c.Expr[Any]*): Tree = {
    annottees.map(_.tree) match {
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$params) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: Nil if mods.hasFlag(Flag.CASE) =>

        val name = tpname.toTermName
        val primitive = makePrimitive(tpname.toTypeName, name, params.head)

        q"""
         $classDef
         object $name {
           $primitive
         }
         """

      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$params) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: q"object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf => ..$objDefs }"
        :: Nil if mods.hasFlag(Flag.CASE) =>

        q"""
         $classDef
         object $objName extends { ..$objEarlyDefs} with ..$objParents { $objSelf =>
           ..${makePrimitive(tpname.toTypeName, tpname.toTermName, params.head)}
           ..$objDefs
         }
         """

      case _ => c.abort(c.enclosingPosition, "Invalid annotation target, UDTs must be a case classes")
    }
  }
}
