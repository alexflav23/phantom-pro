/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 21/11/2017.
 */
package com.outworkers.phantom.dse.mv

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query.{CQLQueryPart, LightweightPart, SelectQuery}
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import shapeless.HList

sealed class IfNotExists(
  override val queries: Seq[CQLQuery] = Seq.empty
) extends CQLQueryPart[IfNotExists](queries) {
  override def qb: CQLQuery = CQLQuery(CQLSyntax.ifNotExists)

  override def instance(l: Seq[CQLQuery]): LightweightPart = new LightweightPart(l)
}

class MaterializedView(
  query: CQLQuery,
  ligthweightPart: LightweightPart
) {

  override def qb: CQLQuery = CQLQuery


  def ifNotExists()

}


object MaterializedView {
  def apply[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](select: SelectQuery[Table, Record, LimitBound, Order, Status, Chain, PS]): MaterializedView = {
    new MaterializedView(select.qb)
  }
}