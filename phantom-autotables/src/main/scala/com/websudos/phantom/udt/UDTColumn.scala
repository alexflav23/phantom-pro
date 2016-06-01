package com.websudos.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.Column

abstract class UDTColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R])(implicit wrapper: UDTType[ValueType])
  extends Column[T, R, ValueType](table) {

  override def apply(row: Row): ValueType = wrapper.fromRow(row)

}
