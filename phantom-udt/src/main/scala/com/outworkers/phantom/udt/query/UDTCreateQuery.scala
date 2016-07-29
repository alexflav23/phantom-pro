package com.outworkers.phantom.udt.query

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.query._

class UDTCreateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](
  val table: Table,
  qb: CQLQuery,
  options: QueryOptions = QueryOptions.empty
) extends RootQuery[Table, Record, Status](table, qb, options) {

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    S <: ConsistencyBound
  ] = UDTCreateQuery[T, R, S]

  override protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    S <: ConsistencyBound
  ](t: T, q: CQLQuery, options: QueryOptions): UDTCreateQuery[T, R, S] = {
    new UDTCreateQuery[T, R, S](t, q, options)
  }
}

object UDTCreateQuery {

  type Default[T <: CassandraTable[T, _], R] = UDTCreateQuery[T, R, Unspecified]

  def apply[T <: CassandraTable[T, _], R](table: T, schema: String): UDTCreateQuery[T, R, Unspecified] = {
    apply(table, CQLQuery(schema))
  }

  def apply[T <: CassandraTable[T, _], R](table: T, schema: CQLQuery): UDTCreateQuery[T, R, Unspecified] = {
    new UDTCreateQuery[T, R, Unspecified](table, schema)
  }
}