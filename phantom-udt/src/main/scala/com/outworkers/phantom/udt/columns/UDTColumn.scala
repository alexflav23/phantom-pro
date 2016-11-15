package com.outworkers.phantom.udt.columns

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.column.Column
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.udt.UDTPrimitive

import scala.util.Try

abstract class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType <: Product with Serializable : UDTPrimitive
](table: CassandraTable[T, R]) extends Column[T, R, ValueType](table) with SessionAugmenterImplicits {

  val primitive = implicitly[UDTPrimitive[ValueType]]

  override def parse(row: Row): Try[ValueType] = {
    primitive.parse(row.getUDTValue(name))
  }

  override def asCql(v: ValueType): String = primitive.asCql(v)

  override def cassandraType: String = s"frozen <${primitive.name}>"
}
