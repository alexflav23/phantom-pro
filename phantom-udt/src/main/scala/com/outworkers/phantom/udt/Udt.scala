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


  def impl(c: blackbox.Context)(annottees: Seq[c.Expr[Any]]): c.Expr[Any] = {
    import c.universe._

    /*
    val result = {
      annottees.map(_.tree).toList match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$parents { $self => ..$stats }" :: Nil => {
          val className = tq"$tpname"
          val tq"${objectClass: TermName}" = tq"$tpname"
          val obfuscatedObject: c.universe.TermName = TypeName("UDTMacroImplementor" + tpname.toString()).toTermName

          val implicitCompanionNameString = "MacroImplicit" + tpname.toString
          val implicitCompanionName: c.universe.TermName = TypeName("MacroImplicit" + tpname.toString()).toTermName

          val wrapperClass: c.universe.TypeName = TypeName(s"${tpname}UDT").toTypeName

          val decoded: List[c.universe.Tree] = paramss.flatten.map {
            case q"$mods val $name: $tpe" => q"implicitly[Primitive[$tpe]].fromRow(row)"
            case p => c.abort(c.enclosingPosition, s"Expected case class value, found $p")
          } toList

          val body = q"""
              object $objectClass {

                implicit object $implicitCompanionName extends UDTType[$className] {

                  def cassandraType: String = ${className.toString()}

                  def asCql(udt: $className): String = ${asCQL(c)}

                  def fromRow(row: com.datastax.driver.core.Row): $className = {
                      new $className(..$decoded)
                  }
                }

              }
          """

          val ctor = DefDef(
            Modifiers(),
            nme.CONSTRUCTOR,
            Nil,
            Nil :: Nil,
            TypeTree(),
            Block(
              Apply(
                Select(Super(This(tpnme.EMPTY), tpnme.EMPTY), nme.CONSTRUCTOR),
                Nil
              )
            )
          )

          c.Expr[Unit](
            new ModuleDef(
              Modifiers(Flag.IMPLICIT),
              TypeName(implicitCompanionNameString),
              Nil,
              noSelfType,
              ctor,
              TypeDef(Modifiers(), TypeName(lit), Nil, TypeTree(typeOf[Int])),
              body
            )
          )
        }
        case _ => c.abort(c.enclosingPosition, "Annotation @Udt can be used only with case classes")
      }
    }*/
    annottees.map(_.tree) match {
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: Nil if mods.hasFlag(Flag.CASE) =>
        val name = tpname.toTermName
        q"""
         $classDef
         object $name {
           ..${lensDefs(tpname, tparams, paramss.head)}
         }
         """
    }
  }

  private def asCQL(c: CrossVersionContext): c.universe.Tree = {
    import c.universe._
    q"""
       "{" +
       s" 'id'   : $${Primitive[Int].asCql(udt.id)}," +
       s" 'name' : $${Primitive[String].asCql(udt.name)}" +
       "}"
    """
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
