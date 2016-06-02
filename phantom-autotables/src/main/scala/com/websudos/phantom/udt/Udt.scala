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
          val objectClass: c.universe.TermName = TypeName(s"$tpname").toTermName
          val obfuscatedObject: c.universe.TermName = TypeName("UDTMacroImplementor" + tpname.toString()).toTermName
          val implicitCompanionName: c.universe.TermName = TypeName("MacroImplicit" + tpname.toString()).toTermName

          val wrapperClass: c.universe.TypeName = TypeName(s"${tpname}UDT").toTypeName

          q"""
              object $objectClass {

                implicit object $implicitCompanionName extends UDTType[$className] {

                  def cassandraType: String = ${className.toString()}

                  def asCql(udt: $className): String = ${className.toString}

                  def fromRow(row: com.datastax.driver.core.Row): $className = {
                      new $className(
                      ${
                        paramss.flatten.map {
                          case q"$mods val $name: $tpe" => q"implicitly[Primitive[$tpe]].fromRow(row)"
                          case p => c.abort(c.enclosingPosition, s"Expected case class value, found $p")
                        } mkString ", "
                      }
                      )
                  }
                }

              }
          """
        }
        case _ => c.abort(c.enclosingPosition, "Annotation @Udt can be used only with case classes")
      }
    }

    println(s"$result")
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
