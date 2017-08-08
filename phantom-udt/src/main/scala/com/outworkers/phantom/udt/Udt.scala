package com.outworkers.phantom.udt

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import com.outworkers.phantom.udt.macros.AnnotationMacro


@compileTimeOnly("enable macro paradise to expand macro annotations")
class Udt extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro AnnotationMacro.impl
}
