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

  def store(person: Person): Future[ResultSet] = {
    insert.value(_.id, person.id)
      .value(_.previous_addresses, person.previous_addresses)
      .value(_.current_addresses, person.current_addresses)
      .future()
  }

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