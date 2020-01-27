package com.outworkers.phantom.udt

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import com.outworkers.phantom.udt.macros.AnnotationMacro

trait UdtRoot

@compileTimeOnly("Enable macro paradise to expand macro annotations and allow Phantom UDT support to work for case classes")
class Udt extends StaticAnnotation with UdtRoot {
  def macroTransform(annottees: Any*): Any = macro AnnotationMacro.impl
}
