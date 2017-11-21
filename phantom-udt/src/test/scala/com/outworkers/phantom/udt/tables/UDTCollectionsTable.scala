/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._

import scala.concurrent.Future

case class Person(
  id: UUID,
  previous_addresses: List[Address],
  current_addresses: Set[Address]
)

abstract class UDTCollectionsTable extends Table[
  UDTCollectionsTable,
  Person
] {

  object id extends UUIDColumn with PartitionKey

  object previous_addresses extends ListColumn[Address]

  object current_addresses extends SetColumn[Address]

  def findById(id: UUID): Future[Option[Person]] = {
    select.where(_.id eqs id).one()
  }
}


abstract class PrimaryUDTCollectionsTable extends Table[
  PrimaryUDTCollectionsTable,
  Person
] {

  object id extends UUIDColumn with PartitionKey

  object previous_addresses extends ListColumn[Address] with PrimaryKey

  object current_addresses extends SetColumn[Address]

  def findByIdAndAddresses(id: UUID, addresses: List[Address]): Future[Option[Person]] = {
    select.where(_.id eqs id).one()
  }
}
