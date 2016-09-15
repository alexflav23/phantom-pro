package com.outworkers.phantom.udt

import com.datastax.driver.core.{Row, UDTValue}
import com.outworkers.phantom.udt.query.UDTCreateQuery
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.column.Column
import com.websudos.phantom.connectors.SessionAugmenterImplicits
import com.websudos.phantom.dsl.KeySpace
import shapeless.HList

import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

abstract class UDTPrimitive[
  T <: Product with Serializable : TypeTag
]() extends SessionAugmenterImplicits {

  def schemaQuery()(implicit space: KeySpace): CQLQuery

  def fromRow(row: UDTValue): Option[T]

  def name: String

  def asCql(udt: T): String
}

abstract class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType <: Product with Serializable : UDTPrimitive : TypeTag
](table: CassandraTable[T, R]) extends Column[T, R, ValueType](table) with SessionAugmenterImplicits {

  val primitive = implicitly[UDTPrimitive[ValueType]]

  override def parse(row: Row): Try[ValueType] = primitive.fromRow(row.getUDTValue(name)) match {
    case Some(value) => Success(value)
    case None => Failure(new RuntimeException(s"Couldn't parse UDT value from ${row.getUDTValue(name)}"))
  }

  override def asCql(v: ValueType): String = primitive.asCql(v)

  override def cassandraType: String = primitive.name

  def create()(implicit keySpace: KeySpace): UDTCreateQuery.Default[T, R] = {
    UDTCreateQuery(table.asInstanceOf[T], primitive.schemaQuery)
  }
}
