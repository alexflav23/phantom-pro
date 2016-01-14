package com.websudos.phantom.enterprise

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.clauses.WhereClause
import com.websudos.phantom.builder.clauses.WhereClause.Condition
import com.websudos.phantom.builder.query.Query
import com.websudos.phantom.enterprise.builder.TopologyStrategies
import shapeless.HList

package object dsl extends TopologyStrategies {

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
    Chain <: WhereBound,
    PS <: HList
  ](val query: Query[Table, Record, Limit, Order, Status, Chain, PS]) extends AnyVal
}
