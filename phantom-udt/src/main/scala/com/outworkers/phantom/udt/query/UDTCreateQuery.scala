package com.outworkers.phantom.udt.query

import com.outworkers.phantom.builder.query._

class UDTCreateQuery(
  val qb: CQLQuery
) extends ExecutableStatement {
  override def options: QueryOptions = QueryOptions.empty
}

object UDTCreateQuery {
  def apply(schema: String): UDTCreateQuery = new UDTCreateQuery(CQLQuery(schema))
}