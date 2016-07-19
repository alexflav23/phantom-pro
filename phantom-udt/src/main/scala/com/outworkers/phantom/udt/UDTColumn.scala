package com.outworkers.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.primitives.Primitive

import scala.concurrent.Future
import scala.util.Try

abstract class UDTPrimitive[T] {

  def schema: String

  def fromRow(row: Row): Try[T]

  def name: String

  def asCql(udt: T): String
}


import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.Column

import scala.util.Try

class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType
](table: CassandraTable[T, R])(implicit primitive: UDTPrimitive[ValueType])
  extends Column[T, R, ValueType](table) {

  override def parse(row: Row): Try[ValueType] = primitive.fromRow(row)

  override def asCql(v: ValueType): String = primitive.asCql(v)

  override def cassandraType: String = primitive.name
}
