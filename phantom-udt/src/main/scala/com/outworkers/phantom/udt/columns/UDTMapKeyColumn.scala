package com.outworkers.phantom.udt.columns

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.column.AbstractMapColumn
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.udt.{Helper, UDTPrimitive}

import scala.util.Try

abstract class UDTMapKeyColumn[
  T <: CassandraTable[T, R],
  R,
  KeyType <: Product with Serializable : UDTPrimitive,
  ValueType : Primitive
](table: CassandraTable[T, R])
  extends AbstractMapColumn[T, R, KeyType, ValueType](table)
    with SessionAugmenterImplicits {

  val keyPrimitive = implicitly[UDTPrimitive[KeyType]]
  val valuePrimitive = implicitly[Primitive[ValueType]]

  override def parse(row: Row): Try[Map[KeyType, ValueType]] = {
    Try(Helper.getMap(row.getMap(name, UDTPrimitive.udtClz, valuePrimitive.clz)).map {
      case (key, value) => keyPrimitive.fromRow(key).get -> valuePrimitive.extract(value)
    })
  }

  override def cassandraType: String = {
    QueryBuilder.Collections.mapColumnType(
      keyPrimitive.cassandraType,
      valuePrimitive.cassandraType
    ).queryString
  }
}
