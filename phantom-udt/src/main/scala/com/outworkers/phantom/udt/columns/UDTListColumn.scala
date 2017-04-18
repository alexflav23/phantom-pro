package com.outworkers.phantom.udt.columns

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.column.AbstractListColumn
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.udt.{Helper, UDTPrimitive}

import scala.util.Try

class UnsupportedFeatureException(msg: String) extends UnsupportedOperationException(msg)

class UDTListColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType <: Product with Serializable
](table: CassandraTable[T, R])(implicit primitive: UDTPrimitive[ValueType])
  extends AbstractListColumn[T, R, ValueType](table) with SessionAugmenterImplicits {

  override def parse(row: Row): Try[List[ValueType]] = {
    Try(Helper.getList(row.getList(name, UDTPrimitive.udtClz)).map(primitive.fromRow(_).get))
  }

  override def cassandraType: String = {
    if (shouldFreeze) {
      QueryBuilder.Collections.frozen(
        QueryBuilder.Collections.listType(primitive.cassandraType)
      ).queryString
    } else {
      QueryBuilder.Collections.listType(primitive.cassandraType).queryString
    }
  }

  override def valueAsCql(v: ValueType): String = primitive.asCql(v)

  override def fromString(c: String): ValueType = {
    throw new UnsupportedFeatureException("Cannot re-parse a UDT structure from a string")
  }
}
