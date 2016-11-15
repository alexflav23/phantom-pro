package com.outworkers.phantom.udt.columns

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.column.AbstractSetColumn
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.udt.{Helper, UDTPrimitive}

import scala.util.Try

class UDTSetColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType <: Product with Serializable : UDTPrimitive
](table: CassandraTable[T, R]) extends AbstractSetColumn[T, R, ValueType](table) with SessionAugmenterImplicits {

  val primitive = implicitly[UDTPrimitive[ValueType]]

  override def parse(row: Row): Try[Set[ValueType]] = {
    Try(Helper.getSet(row.getSet(name, UDTPrimitive.udtClz)).map(primitive.fromRow(_).get))
  }

  override def cassandraType: String = {
    QueryBuilder.Collections.setType(primitive.cassandraType).queryString
  }

  override def valueAsCql(v: ValueType): String = primitive.asCql(v)

  override def fromString(c: String): ValueType = {
    throw new UnsupportedFeatureException("Cannot re-parse a UDT structure from a string")
  }
}