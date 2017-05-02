/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._
import com.outworkers.phantom.udt.columns.UDTColumn

import scala.concurrent.Future

@Udt case class NestedMaps(
  postcode: String,
  people: Map[String, Address]
)

case class NestedMapRecord(
  id: UUID,
  people: List[String],
  addresses: NestedMaps
)

abstract class NestedMapsTable extends CassandraTable[NestedMapsTable, NestedMapRecord] with RootConnector {

  object id extends UUIDColumn(this) with PartitionKey

  object people extends ListColumn[String](this)

  object addresses extends UDTColumn[NestedMapsTable, NestedMapRecord, NestedMaps](this)

  def findById(id: UUID): Future[Option[NestedMapRecord]] = {
    select.where(_.id eqs id).one()
  }
}
