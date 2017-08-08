/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._

import scala.concurrent.Future

@Udt case class CollectionSetUdt(
  name: String,
  addresses: Set[Address]
)

@Udt case class NestedSetRecord(
  id: UUID,
  email: String,
  address: Address,
  col: CollectionSetUdt
)

abstract class NestedUdtSetsTable extends Table[
  NestedUdtSetsTable,
  NestedSetRecord
] {
  object id extends UUIDColumn with PartitionKey
  object email extends StringColumn
  object address extends Col[Address]
  object col extends Col[CollectionSetUdt]

  def findById(id: UUID): Future[Option[NestedSetRecord]] = {
    select.where(_.id eqs id).one()
  }
}
