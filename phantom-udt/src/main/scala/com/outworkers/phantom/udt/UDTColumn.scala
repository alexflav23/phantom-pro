package com.outworkers.phantom.udt

import com.datastax.driver.core.Row
import com.outworkers.phantom.udt.SchemaGenerator.Schema
import com.outworkers.phantom.udt.query.UDTCreateQuery
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.column.Column
import com.websudos.phantom.connectors.SessionAugmenterImplicits
import com.websudos.phantom.dsl.{KeySpace, Session}
import shapeless.ops.hlist.{Mapper, ToList}
import shapeless.{Generic, HList}

import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

abstract class UDTPrimitive[
  T <: Product with Serializable : TypeTag
]() extends SessionAugmenterImplicits {

  def instance: T

  def schemaQuery()(implicit space: KeySpace): CQLQuery

  def fromRow(row: Row): Option[T]

  def name: String

  def asCql(udt: T): String
}

abstract class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType <: Product with Serializable : UDTPrimitive : TypeTag
](table: CassandraTable[T, R]) extends Column[T, R, ValueType](table) with SessionAugmenterImplicits {

  val primitive = implicitly[UDTPrimitive[ValueType]]

  def instance: ValueType = primitive.instance

  override def parse(row: Row): Try[ValueType] = primitive.fromRow(row) match {
    case Some(value) => Success(value)
    case None => Failure(new Exception("Couldn't parse UDT"))
  }

  override def asCql(v: ValueType): String = primitive.asCql(v)

  override def cassandraType: String = primitive.name

  def create[
    Out <: HList,
    MapperOut <: HList
  ]()(implicit keySpace: KeySpace): UDTCreateQuery.Default[T, R] = {
    UDTCreateQuery(table.asInstanceOf[T], primitive.schemaQuery)
  }
}
