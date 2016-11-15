package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._
import com.outworkers.phantom.udt.columns.UDTColumn

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

class NestedUdtSetsTable extends CassandraTable[ConcreteNestedUdtSetsTable, NestedSetRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object email extends StringColumn(this)
  object address extends UDTColumn[ConcreteNestedUdtSetsTable, NestedSetRecord, Address](this)
  object col extends UDTColumn[ConcreteNestedUdtSetsTable, NestedSetRecord, CollectionSetUdt](this)

  def fromRow(row: Row): NestedSetRecord = {
    NestedSetRecord(
      id = id(row),
      email = email(row),
      address = address(row),
      col = col(row)
    )
  }
}

abstract class ConcreteNestedUdtSetsTable extends NestedUdtSetsTable with RootConnector {

  def store(rec: NestedSetRecord): Future[ResultSet] = {
    insert
      .value(_.id, rec.id)
      .value(_.email, rec.email)
      .value(_.address, rec.address)
      .value(_.col, rec.col)
      .future()
  }

  def findById(id: UUID): Future[Option[NestedSetRecord]] = {
    select.where(_.id eqs id).one()
  }
}
