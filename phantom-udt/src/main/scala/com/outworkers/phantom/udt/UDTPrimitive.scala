package com.outworkers.phantom.udt

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}
import com.outworkers.phantom.udt.query.UDTCreateQuery

abstract class UDTPrimitive[
  T
]() extends Primitive[T] with SessionAugmenterImplicits {

  def deps()(implicit space: KeySpace): Seq[UDTPrimitive[_]]

  def typeDependencies()(implicit space: KeySpace): Seq[UDTCreateQuery]

  def schemaQuery()(implicit space: KeySpace): CQLQuery

  def name: String

  override def dataType: String = name
}

object UDTPrimitive {
  def apply[T]()(implicit ev: UDTPrimitive[T]): UDTPrimitive[T] = ev
}
