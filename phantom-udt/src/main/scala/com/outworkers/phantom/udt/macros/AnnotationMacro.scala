package com.outworkers.phantom.udt.macros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

//noinspection ScalaStyle
@macrocompat.bundle
class AnnotationMacro(val c: whitebox.Context) extends UdtRootMacro {

  import c.universe._

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
