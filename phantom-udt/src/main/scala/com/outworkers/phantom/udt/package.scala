package com.outworkers.phantom

package object udt {

  implicit class TableAugmenter[T <: CassandraTable[T, R], R](val table: CassandraTable[T, R]) extends AnyVal {
    type UDTColumn[ValueType <: Product with Serializable] = com.outworkers.phantom.udt.columns.UDTColumn[T, R, ValueType]
  }

  type UDTColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTColumn[Table, Record, ValueType]

  type UDTListColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTListColumn[Table, Record, ValueType]

  type UDTSetColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTSetColumn[Table, Record, ValueType]

  type UDTMapKeyColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    KeyType <: Product with Serializable,
    ValueType
  ] = com.outworkers.phantom.udt.columns.UDTMapKeyColumn[Table, Record, KeyType, ValueType]

  type UDTMapValueColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    KeyType,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTMapValueColumn[Table, Record, KeyType, ValueType]

  type UDTMapEntryColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    KeyType <: Product with Serializable,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTMapEntryColumn[Table, Record, KeyType, ValueType]
}
