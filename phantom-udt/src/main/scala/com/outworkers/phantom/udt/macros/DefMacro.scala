package com.outworkers.phantom.udt.macros

import scala.reflect.macros.whitebox

@macrocompat.bundle
class DefMacro(override val c: whitebox.Context) extends UdtRootMacro {

  import c.universe._

  def materialize[T : c.WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    if (isCaseClass(tpe) && !tpe.typeSymbol.fullName.startsWith("scala.Tuple")) {
      val primitive = udtPrimitive(
        tpe.typeSymbol.name.toTypeName,
        tpe.typeSymbol.name.toTermName.toString,
        Accessors(caseFields(tpe))
      )

      if (showTrees) {
        echo(s"Deriving UDT encoding for ${printType(tpe)} \n ${showCode(primitive)}")
      }
      primitive
    } else {
      error("UDT encoding can only be derived for case classes")
      abort("UDT encoding can only be derived for case classes")
    }
  }

}
