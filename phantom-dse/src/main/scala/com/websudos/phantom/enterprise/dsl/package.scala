package com.websudos.phantom.enterprise

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.clauses.WhereClause
import com.websudos.phantom.builder.clauses.WhereClause.Condition
import com.websudos.phantom.builder.query.Query
import com.websudos.phantom.builder._

package object dsl {


  object solr_query {
    def eqs(str: String): WhereClause.Condition = {
      new Condition(QueryBuilder.Where.eqs("solr_query", str))
    }
  }

  implicit class QueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound
  ](val query: Query[Table, Record, Limit, Order, Status, Chain]) extends AnyVal {


  }
}
