package com.outworkers.phantom.udt

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Udt extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro UdtMacroImpl.impl
}

//noinspection ScalaStyle
@macrocompat.bundle
class UdtMacroImpl(val c: blackbox.Context) {

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

  val builder = q"com.outworkers.phantom.builder"
  val primitivePkg = q"com.outworkers.phantom.builder.primitives"
  val udtPackage = q"com.outworkers.phantom.udt"

  val packagePrefix = q"com.outworkers.phantom.udt"
  val keySpaceTpe = tq"com.outworkers.phantom.dsl.KeySpace"
  val cqlQueryTpe = tq"com.outworkers.phantom.builder.query.engine.CQLQuery"
  val udtValueTpe = tq"com.datastax.driver.core.UDTValue"
  val collections = q"scala.collection.immutable"

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

    def caster: Tree => Tree

    /**
      * The tree enclosing the definition of the Cassandra type information of the extractor.
      * This will be provided by implementors as it based on a combination of inner and outer types.
      * @return The CQL type tree that will be used to produce the CQL type string during schema auto-generation.
      */
    def cassandraType: Tree

    def parser: Tree

    def schema: Tree = q"""$field -> $cassandraType"""

    def serializer: Tree

    def extractor: Tree = fq"""$extractorName <- $parser"""
  }

  case class RegularExtractor(
    accessor: Accessor,
    override val typeQualifier: Tree,
    override val isUdtType: Boolean
  ) extends Extractor {
    val caster: Tree => Tree = tr => if (!isUdtType) q"$tr.fromRow($field, udt)" else {
      q"""$tr.fromRow(udt.getUDTValue($field))"""
    }

    def cassandraType: Tree = if (isUdtType) {
      val udtName = q"$packagePrefix.UDTPrimitive[${accessor.paramType}].name"
      q"$packagePrefix.Helper.frozen($udtName)"
    } else {
      q"$typeQualifier.cassandraType"
    }

    def parser: Tree = caster(typeQualifier)

    override def serializer: Tree = q"""$field -> $typeQualifier.asCql(instance.${accessor.name})"""
  }

  case class CollectionExtractor(
    accessor: Accessor,
    innerTypes: List[InnerType],
    collection: List[TypeName] => Tree
  ) extends Extractor {
    val caster: Tree => Tree = tr => q"$tr.fromRow($field, udt)"

    def cassandraType: Tree = {
      q"""$builder.QueryBuilder.Collections.listType(
        $primitivePkg.Primitive[..${innerTypes.map(_.tpe)}].cassandraType
      ).queryString"""
    }

    def parser: Tree = caster(typeQualifier)

    override def typeQualifier: Tree = {
      q"$primitivePkg.Primitive[${collection(innerTypes.map(_.tpe))}]"
    }

    override def serializer: Tree = {
      q"""
        $field -> $builder.QueryBuilder.Collections.serialize(
          instance.${accessor.name}.map($primitivePkg.Primitive[..${innerTypes.map(_.tpe)}].asCql(_))
         ).queryString
       """
    }
  }

  case class MapExtractor(
    accessor: Accessor,
    keyType: InnerType,
    valueType: InnerType
  ) extends Extractor {
    val caster: Tree => Tree = tr => q"$tr.fromRow($field, udt)"

    val udtPrimitiveClz: Tree = q"$udtPackage.UDTPrimitive.udtClz"

    def primitive(tpe: InnerType): Tree = {
      if (tpe.udt) q"$udtPackage.UDTPrimitive.apply[${tpe.tpe}]" else q"$primitivePkg.Primitive.apply[${tpe.tpe}]"
    }

    def implicitRef(tpe: InnerType): Tree = {
      if (tpe.udt) q"$udtPackage.UDTPrimitive.apply[${tpe.tpe}]" else q"$primitivePkg.Primitive.apply[${tpe.tpe}]"
    }

    val keyPrimitive = primitive(keyType)
    val valuePrimitive = primitive(valueType)

    def inferType(primitive: Tree, udt: Boolean): Tree = {
      if (udt) {
        q"$packagePrefix.Helper.frozen($primitive.name)"
      } else {
        q"$primitive.cassandraType"
      }
    }

    def cassandraType: Tree = {
      q"""
        $builder.QueryBuilder.Collections.mapType(
          ${inferType(keyPrimitive, keyType.udt)},
          ${inferType(valuePrimitive, valueType.udt)}
        ).queryString
      """
    }

    override def schema: Tree = q"""$field -> $cassandraType"""

    def mapParser(tpe: InnerType, ref: TermName, term: TermName): Tree = {
      if (tpe.udt) {
        q"$ref.fromRow($term).get"
      } else {
        q"$ref.extract($term.asInstanceOf[${tpe.refType(ref)}])"
      }
    }

    override def isUdtType: Boolean = true

    val keyTerm = TermName("key")
    val valueTerm = TermName("value")

    def clazzType(tpe: InnerType, ref: TermName): Tree = {
      if (tpe.udt) udtPrimitiveClz else q"$ref.clz"
    }

    def parser: Tree = {
      val keyRefTerm = TermName("keyP")
      val valueRefTerm = TermName("valueP")

      val keyClz = clazzType(keyType, keyRefTerm)
      val valueClz = clazzType(valueType, valueRefTerm)

      q"""
        scala.util.Try {
          val $keyRefTerm = ${implicitRef(keyType)}
          val $valueRefTerm = ${implicitRef(valueType)}

          $packagePrefix.Helper.getMap(udt.getMap($field, $keyClz, $valueClz)) map {
            case (
              $keyTerm: ${keyType.refType(keyRefTerm)},
              $valueTerm: ${valueType.refType(valueRefTerm)}
            ) => ${mapParser(keyType, keyRefTerm, keyTerm)} -> ${mapParser(valueType, valueRefTerm, valueTerm)}
          }
        }
       """
    }

    override def serializer: Tree = {
      q"""
        $field -> { $builder.QueryBuilder.Collections.serialize(
          instance.${accessor.name}.map {
            case ($keyTerm, $valueTerm) => $keyPrimitive.asCql($keyTerm) -> $valuePrimitive.asCql($valueTerm)
          }
         ).queryString
        }
       """
    }
  }

  case class UdtCollectionExtractor(
    accessor: Accessor,
    innerType: InnerType,
    collectionString: String,
    collector: TermName,
    collection: TypeName => Tree
  ) extends Extractor {
    val caster: Tree => Tree = tr => q"$tr.fromRow($field, udt)"

    def cassandraType: Tree =
      q""" "frozen" + "<" +
        $collectionString + "<" +
        $udtPackage.UDTPrimitive[${innerType.tpe}].name + ">>"
      """

    override def isUdtType: Boolean = true

    def parser: Tree =
      q"""
        {
          val p = $udtPackage.UDTPrimitive[${innerType.tpe}]
          scala.util.Try($packagePrefix.Helper.$collector(
            udt.$collector($field, $udtPackage.UDTPrimitive.udtClz))
            .flatMap(x => p.fromRow(x).toOption))
        }
       """

    override def serializer: Tree = {
      q"""
        $field -> { $builder.QueryBuilder.Collections.serialize(
          instance.${accessor.name}.map($udtPackage.UDTPrimitive[${innerType.tpe}].asCql(_))
         ).queryString
        }
       """
    }
  }

  case class InnerType(
    tpe: TypeName,
    udt: Boolean
  ) {
    def primitiveType: Tree = {
      if (udt) udtValueTpe else tq"""$primitivePkg.Primitive[$tpe]#PrimitiveType"""
    }

    def refType(term: TermName): Tree = {
      if (udt) udtValueTpe else tq"""$term.PrimitiveType"""
    }
  }

  object InnerType {
    def apply(tpe: Type): InnerType = {
      val sym = tpe.typeSymbol

      InnerType(
        tpe = tpe.typeSymbol.asType.name.toTypeName,
        udt = sym.isClass && sym.asClass.isCaseClass
      )
    }
  }

  /**
    * Derives the full primitive type for a given root type.
    * This will not yield a method call of any kind, it will simply return
    * a reference to the right primitive.
    * @param accessor The root type to compute the primitive type for.
    * @return
    */
  private[this] def derivePrimitive(accessor: Accessor): Extractor = {
    accessor.symbol match {
      case sym if sym.isClass && sym.asClass.isCaseClass =>
        val tpe = sym.asType.name.toTypeName

        RegularExtractor(
          accessor,
          q"$udtPackage.UDTPrimitive[$tpe]",
          isUdtType = true
        )

      case s @ Symbols.listSymbol =>
        accessor.paramType.typeArgs match {
          case headType :: Nil => {
            val refined = InnerType(headType)

            if (refined.udt) {
              UdtCollectionExtractor(
                accessor = accessor,
                innerType = refined,
                collectionString = "list",
                collector = TermName("getList"),
                collection = tp => q"$collections.List.apply[..$tp]()"
              )
            } else {
              CollectionExtractor(
                accessor = accessor,
                innerTypes = refined :: Nil,
                collection = tp => q"$collections.List.apply[..$tp]()"
              )
            }
          }

          case _ => c.abort(c.enclosingPosition, "Expected type argument to be provided for list type")
        }

      case s @ Symbols.setSymbol =>
        accessor.paramType.typeArgs match {
          case headType :: Nil => {
            val refined = InnerType(headType)

            if (refined.udt) {
              UdtCollectionExtractor(
                accessor = accessor,
                innerType = refined,
                collectionString = "set",
                collector = TermName("getSet"),
                collection = tp => q"$collections.Set.apply[..$tp]()"
              )
            } else {
              CollectionExtractor(
                accessor = accessor,
                innerTypes = refined :: Nil,
                collection = tp => q"$collections.Set.apply[..$tp]()"
              )
            }
          }
          case _ => c.abort(c.enclosingPosition, "Expected type argument to be provided for list type")
        }

      case s @ Symbols.mapSymbol =>
        accessor.paramType.typeArgs match {
          case keyType :: valueType :: Nil =>
            MapExtractor(
              accessor = accessor,
              InnerType(keyType),
              InnerType(valueType)
            )
          case _ => c.abort(c.enclosingPosition, "Expected 2 type arguments to be provided for map type")
        }

      case _ => RegularExtractor(accessor, q"$primitivePkg.Primitive[${accessor.tpe}]", isUdtType = false)
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
    udtDeps(params) map (tp =>
      q"""new $udtPackage.query.UDTCreateQuery(
         $udtPackage.UDTPrimitive[$tp].schemaQuery
      )"""
    )
  }


  def makePrimitive(
    typeName: TypeName,
    name: TermName,
    params: Seq[ValDef]
  ): Tree = {
    val stringName = name.decodedName.toString
    val objName = TermName(c.freshName(stringName + "_udt_primitive"))

    val source = accessors(params)
    val fields = source map derivePrimitive

    q"""
        implicit val $objName: $packagePrefix.UDTPrimitive[$typeName] = new $packagePrefix.UDTPrimitive[$typeName] {

          def deps()(implicit space: $keySpaceTpe): $collections.Seq[$udtPackage.UDTPrimitive[_]] = {
            $collections.Seq.apply[$packagePrefix.UDTPrimitive[_]](..${typeDependencies(source)})
          }

          def typeDependencies()(implicit space: $keySpaceTpe): $collections.Seq[$udtPackage.query.UDTCreateQuery] = {
            $collections.Seq.apply[$udtPackage.query.UDTCreateQuery](..${queryDependencies(source)})
          }

          def asCql(instance: $typeName): String = {
            val baseString = $collections.List(..${fields.map(_.serializer)}).map {
              case (name, ext) => name + ": " + ext
            } mkString(", ")

            "{" + baseString + "}"
          }

          def fromRow(udt: $udtValueTpe): scala.util.Try[$typeName] = {
            for (..${fields.map(_.extractor)}) yield $name.apply(..${fields.map(_.extractorName)})
          }

          def schemaQuery()(implicit space: $keySpaceTpe): $cqlQueryTpe = {

            val membersList = $collections.List(..${fields.map(_.schema)}).map {
              case (name, casType) => name + " " + casType
            }

            val base = "CREATE TYPE IF NOT EXISTS " + space.name + "." + ${stringName.toLowerCase} + " (" + membersList.mkString(", ") + ")"

            $builder.query.engine.CQLQuery(base)
          }

          def name: String = $stringName

          def clz: Class[$typeName] = classOf[$typeName]
        }
     """
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
