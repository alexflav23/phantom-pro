package com.outworkers.phantom.udt

import com.datastax.driver.core.UDTValue
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}
import com.outworkers.phantom.udt.query.UDTCreateQuery
import scala.util.{Failure, Try}

abstract class UDTPrimitive[
  T <: Product with Serializable
]() extends SessionAugmenterImplicits {

  def deps()(implicit space: KeySpace): Seq[UDTPrimitive[_]]

  def typeDependencies()(implicit space: KeySpace): Seq[UDTCreateQuery]

  def schemaQuery()(implicit space: KeySpace): CQLQuery

  def fromRow(row: UDTValue): Try[T]

  def parse(row: UDTValue): Try[T] = {
    if (row == null) {
      Failure(new RuntimeException(s"""The source data for row with type $cassandraType was null"""))
    } else {
      fromRow(row)
    }
  }

  def name: String

  def cassandraType: String = Helper.frozen(name)

  def asCql(udt: T): String

  def clz: Class[T]
}

object UDTPrimitive {
  def apply[T <: Product with Serializable]()(implicit ev: UDTPrimitive[T]): UDTPrimitive[T] = ev

  val udtClz: Class[UDTValue] = classOf[UDTValue]
}
