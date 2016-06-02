package com.websudos.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.Column

import scala.util.Try

class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType
](table: CassandraTable[T, R])(implicit wrapper: UDTType[ValueType])
  extends Column[T, R, ValueType](table) {

  override def optional(row: Row): Try[ValueType] = wrapper.fromRow(row)

  override def asCql(v: ValueType): String = wrapper.asCql(v)

  override def cassandraType: String = wrapper.name
}
