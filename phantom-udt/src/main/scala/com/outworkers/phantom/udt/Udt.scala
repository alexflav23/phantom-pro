package com.outworkers.phantom.udt

import com.outworkers.phantom.udt.CrossVersionDefs.CrossVersionContext

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

class Udt extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Udt.impl
}

//noinspection ScalaStyle
object Udt {

  def accessors(c: CrossVersionContext)(
    params: Seq[c.universe.ValDef]
  ): Iterable[(c.universe.TermName, c.universe.TypeName)] = {
    import c.universe._

    params.map {
      case ValDef(mods: Modifiers, name: TermName, tpt: Tree, rhs: Tree) => {
        name -> TypeName(tpt.toString)
      }
    }
  }

  def makePrimitive(c: CrossVersionContext)(
    typeName: c.TypeName,
    name: c.TermName,
    params: Seq[c.universe.ValDef]
  ): c.Expr[Any] = {
    import c.universe._

    val objName = TermName("_udt_primitive")
    val stringName = name.decodedName.toString

    val (names, types) = accessors(c)(params).unzip

    val nameDeclarations = names map (nm => q"$nm")

    val declarations = types map {
      tpe => q"""com.websudos.phantom.builder.primitives.Primitive[$tpe].cassandraType"""
    }

    val serializers = accessors(c)(params) map {
      case (nm, tpe) => q"${nm.toString} -> com.websudos.phantom.builder.primitives.Primitive[$tpe].asCql(instance.$nm)"
    }

    val extractors = accessors(c)(params) map {
      case (nm, tpe) => {
        val newTerm = TermName(nm.toString + "Opt")
        fq"""$newTerm <- Extractor[$tpe].apply(${nm.toString}, udt).toOption"""
      }
    }

    val extractorNames = accessors(c)(params) map {
      case (nm, tpe) => TermName(nm.toString + "Opt")
    }

    c.Expr[Any](
      q"""
          implicit object $objName extends UDTPrimitive[$typeName] {

            def asCql(instance: $typeName): String = {
              val baseString = List(..$serializers).map {
                case (name, ext) => name + ": " + ext
              } mkString(", ")

              "{" + baseString + "}"
            }

            def fromRow(udt: com.datastax.driver.core.UDTValue): Option[$typeName] = {
              for (..$extractors) yield $name.apply(..$extractorNames)
            }

            def schemaQuery()(
              implicit space: com.websudos.phantom.dsl.KeySpace
            ): com.websudos.phantom.builder.query.CQLQuery = {

              val membersList = scala.collection.immutable.List(..${nameDeclarations.map(_.toString()) zip declarations}).map {
                case (name, casType) => name + " " + casType
              }

              val base = "CREATE TYPE IF NOT EXISTS " + space.name + "." + ${stringName.toLowerCase} + " (" + membersList.mkString(", ") + ")"

              com.websudos.phantom.builder.query.CQLQuery(base)
            }

            def name: String = $stringName
          }
       """
    )
  }

  def impl(c: CrossVersionContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    annottees.map(_.tree) match {
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$params) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: Nil if mods.hasFlag(Flag.CASE) =>
        val name = tpname.toTermName

        val res = q"""
         $classDef
         object $name {
           ..${makePrimitive(c)(tpname.toTypeName, name, params.head)}
         }
         """
        println(showCode(res))
        c.Expr[Any](res)

      case _ => c.abort(c.enclosingPosition, "Invalid annotation target, UDTs must be a case classes")
    }
  }
}
