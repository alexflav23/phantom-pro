/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._
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

abstract class NestedMapsTable extends Table[
  NestedMapsTable,
  NestedMapRecord
] {

  object id extends UUIDColumn with PartitionKey

  object people extends ListColumn[String]

  object addresses extends Col[NestedMaps]

  def findById(id: UUID): Future[Option[NestedMapRecord]] = {
    select.where(_.id eqs id).one()
  }
}
