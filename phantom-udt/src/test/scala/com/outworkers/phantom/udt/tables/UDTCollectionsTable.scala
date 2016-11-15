package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._

import scala.concurrent.Future


case class Person(
  id: UUID,
  current_addresses: Set[Address],
  previous_addresses: List[Address]
)

class UDTCollectionsTable extends CassandraTable[ConcreteUDTCollectionsTable, Person] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object previous_addresses extends UDTListColumn[ConcreteUDTCollectionsTable, Person, Address](this)

  object current_addresses extends UDTSetColumn[ConcreteUDTCollectionsTable, Person, Address](this)

  override def fromRow(r: Row): Person = Person(id(r), current_addresses(r), previous_addresses(r))
}


abstract class ConcreteUDTCollectionsTable extends UDTCollectionsTable with RootConnector {
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