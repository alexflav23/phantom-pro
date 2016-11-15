package com.outworkers.phantom.udt.columns

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.column.AbstractMapColumn
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.udt.{Helper, UDTPrimitive}

import scala.util.Try

abstract class UDTMapValueColumn[
  T <: CassandraTable[T, R],
  R,
  KeyType : Primitive,
  ValueType <: Product with Serializable : UDTPrimitive
](table: CassandraTable[T, R])
  extends AbstractMapColumn[T, R, KeyType, ValueType](table)
  with SessionAugmenterImplicits {

  val keyPrimitive = implicitly[Primitive[KeyType]]
  val valuePrimitive = implicitly[UDTPrimitive[ValueType]]

  override def parse(row: Row): Try[Map[KeyType, ValueType]] = {
    Try(Helper.getMap(row.getMap(name, keyPrimitive.clz, UDTPrimitive.udtClz)).map {
      case (key, value) => keyPrimitive.extract(key) -> valuePrimitive.fromRow(value).get
    })
  }

  override def cassandraType: String = {
    QueryBuilder.Collections.mapColumnType(
      keyPrimitive.cassandraType,
      valuePrimitive.cassandraType
    ).queryString
  }
}
