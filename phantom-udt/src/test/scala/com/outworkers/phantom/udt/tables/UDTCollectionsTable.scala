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

case class Person(
  id: UUID,
  previous_addresses: List[Address],
  current_addresses: Set[Address]
)

abstract class UDTCollectionsTable extends CassandraTable[UDTCollectionsTable, Person] with RootConnector {

  object id extends UUIDColumn(this) with PartitionKey

  object previous_addresses extends UDTListColumn[UDTCollectionsTable, Person, Address](this)

  object current_addresses extends UDTSetColumn[UDTCollectionsTable, Person, Address](this)

  def findById(id: UUID): Future[Option[Person]] = {
    select.where(_.id eqs id).one()
  }
}


abstract class PrimaryUDTCollectionsTable extends CassandraTable[PrimaryUDTCollectionsTable, Person] with RootConnector {

  object id extends UUIDColumn(this) with PartitionKey

  object previous_addresses extends UDTListColumn[PrimaryUDTCollectionsTable, Person, Address](this) with PrimaryKey

  object current_addresses extends UDTSetColumn[PrimaryUDTCollectionsTable, Person, Address](this)

  def store(person: Person): Future[ResultSet] = {
    insert.value(_.id, person.id)
      .value(_.previous_addresses, person.previous_addresses)
      .value(_.current_addresses, person.current_addresses)
      .future()
  }

  def findByIdAndAddresses(id: UUID, addresses: List[Address]): Future[Option[Person]] = {
    select.where(_.id eqs id).one()
  }
}
