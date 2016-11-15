package com.outworkers.phantom.udt.columns

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.column.AbstractMapColumn
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.udt.{Helper, UDTPrimitive}

import scala.util.Try

abstract class UDTMapEntryColumn[
  T <: CassandraTable[T, R],
  R,
  KeyType <: Product with Serializable : UDTPrimitive,
  ValueType <: Product with Serializable : UDTPrimitive
](table: CassandraTable[T, R])
  extends AbstractMapColumn[T, R, KeyType, ValueType](table)
    with SessionAugmenterImplicits {

  val keyPrimitive = implicitly[UDTPrimitive[KeyType]]
  val valuePrimitive = implicitly[UDTPrimitive[ValueType]]

  override def parse(row: Row): Try[Map[KeyType, ValueType]] = {
    Try(Helper.getMap(row.getMap(name, UDTPrimitive.udtClz, UDTPrimitive.udtClz)).map {
      case (key, value) => keyPrimitive.fromRow(key).get -> valuePrimitive.fromRow(value).get
    })
  }

  override def cassandraType: String = {
    QueryBuilder.Collections.mapColumnType(
      keyPrimitive.cassandraType,
      valuePrimitive.cassandraType
    ).queryString
  }
}

