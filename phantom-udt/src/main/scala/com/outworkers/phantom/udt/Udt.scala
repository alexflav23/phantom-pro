package com.outworkers.phantom.udt

import com.outworkers.phantom.udt.CrossVersionDefs.CrossVersionContext

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class Udt extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Udt.impl
}

//noinspection ScalaStyle
object Udt {

  def accessors(c: blackbox.Context)(
    params: Seq[c.universe.ValDef]
  ): Iterable[(c.universe.TermName, c.universe.TypeName)] = {
    import c.universe._

    params.map {
      case ValDef(mods: Modifiers, name: TermName, tpt: Tree, rhs: Tree) => {
        name -> TypeName(tpt.toString)
      }
    }
  }

  def makePrimitive(c: blackbox.Context)(
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
      tpe => q"""Primitive[$tpe].cassandraType"""
    }

    c.Expr[Any](
      q"""
          implicit object $objName extends UDTPrimitive[$typeName] {
            def asCql(instance: $typeName): String = ""

            def fromRow(row: com.datastax.driver.core.UDTValue): Option[$typeName] = None

            def schemaQuery()(
              implicit space: com.websudos.phantom.dsl.KeySpace
            ): com.websudos.phantom.builder.query.CQLQuery = {

              val membersList = List(..${nameDeclarations.map(_.toString()) zip declarations}).map {
                case (name, casType) => name + " " + casType
              }

              val base = "CREATE TYPE IF NOT EXISTS " + space.name + "." + ${stringName.toLowerCase} + " " + membersList.mkString(", ")

              com.websudos.phantom.builder.query.CQLQuery(base)
            }

            def name: String = $stringName
          }
       """
    )
  }

  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
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
