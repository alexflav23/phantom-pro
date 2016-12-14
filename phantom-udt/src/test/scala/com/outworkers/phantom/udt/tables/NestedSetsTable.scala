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

abstract class NestedUdtSetsTable extends CassandraTable[NestedUdtSetsTable, NestedSetRecord] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object email extends StringColumn(this)
  object address extends UDTColumn[NestedUdtSetsTable, NestedSetRecord, Address](this)
  object col extends UDTColumn[NestedUdtSetsTable, NestedSetRecord, CollectionSetUdt](this)

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
