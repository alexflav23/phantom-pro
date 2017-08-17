package com.outworkers.phantom.udt

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.execution.ExecutableCqlQuery
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}

abstract class UDTPrimitive[
  T
]() extends Primitive[T] with SessionAugmenterImplicits {

  def deps()(implicit space: KeySpace): Seq[UDTPrimitive[_]]

  def typeDependencies()(implicit space: KeySpace): Seq[ExecutableCqlQuery]

  def schemaQuery()(implicit space: KeySpace): ExecutableCqlQuery

  def name: String

  override def dataType: String = name
}

object UDTPrimitive {
  def apply[T]()(implicit ev: UDTPrimitive[T]): UDTPrimitive[T] = ev
}
