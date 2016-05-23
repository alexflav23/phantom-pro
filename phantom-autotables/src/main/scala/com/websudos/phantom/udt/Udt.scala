package com.websudos.phantom.udt

import com.websudos.phantom.CrossVersionDefs._

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.api.Trees

class Udt extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Udt.impl

}

//noinspection ScalaStyle
object Udt {

  def impl(c: CrossVersionContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val result = {
      annottees.map(_.tree).toList match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$parents { $self => ..$stats }" :: Nil => {
          val className = tq"$tpname"
          val objectName = TypeName(s"${tpname}Table").toTermName

          val columns = paramss.flatten.map(param => generateTable(c)(param)).toList

          q"""
              $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$parents { $self => ..$stats
                def fromRow: $className = {
                  new $className(666,"abc")
                }
              }

              object $objectName {
                def abc: String = "foo"

                ..$columns
              }
          """
        }
        case _ => c.abort(c.enclosingPosition, "Annotation @Udt can be used only with case classes")
      }
    }
    c.Expr[Any](result)
  }

  private def generateTable(c: CrossVersionContext)(param: Trees#Tree): c.universe.Tree = {
    import c.universe._

    param match {
      case q"$mods val $name: $tpe" =>
        q"object $name"
      case _ =>
        c.abort(c.enclosingPosition, "[Phantom-pro]: Invalid val parameter in case class")
    }
  }
}
