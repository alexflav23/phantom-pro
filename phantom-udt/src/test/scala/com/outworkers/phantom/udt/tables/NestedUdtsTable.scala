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

class NestedUdtsTable extends CassandraTable[ConcreteNestedUdtsTable, NestedRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object email extends StringColumn(this)
  object address extends UDTColumn[ConcreteNestedUdtsTable, NestedRecord, Address](this)
  object col extends UDTColumn[ConcreteNestedUdtsTable, NestedRecord, CollectionUdt](this)

  def fromRow(row: Row): NestedRecord = {
    NestedRecord(
      id = id(row),
      email = email(row),
      address = address(row),
      col = col(row)
    )
  }
}

abstract class ConcreteNestedUdtsTable extends NestedUdtsTable with RootConnector {

  def store(rec: NestedRecord): Future[ResultSet] = {
    insert
      .value(_.id, rec.id)
      .value(_.email, rec.email)
      .value(_.address, rec.address)
      .value(_.col, rec.col)
      .future()
  }

  def findById(id: UUID): Future[Option[NestedRecord]] = {
    select.where(_.id eqs id).one()
  }
}