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

@Udt case class Location(
  longitude: Long,
  latitude: Long
)

@Udt case class Address(
  name: String,
  location: Location,
  postcode: String
)

@Udt case class CollectionUdt(
  name: String,
  addresses: List[Address]
)

@Udt case class NestedRecord(
  id: UUID,
  email: String,
  address: Address,
  col: CollectionUdt
)

abstract class NestedUdtsTable extends CassandraTable[NestedUdtsTable, NestedRecord] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object email extends StringColumn(this)
  object address extends UDTColumn[NestedUdtsTable, NestedRecord, Address](this)
  object col extends UDTColumn[NestedUdtsTable, NestedRecord, CollectionUdt](this)

  def findById(id: UUID): Future[Option[NestedRecord]] = select.where(_.id eqs id).one()
}
