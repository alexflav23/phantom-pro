package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._
import com.outworkers.phantom.udt.columns.UDTColumn
import com.outworkers.util.testing.sample

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

class NestedMapsTable extends CassandraTable[ConcreteNestedMapsTable, NestedMapRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object people extends ListColumn[String](this)

  object addresses extends UDTColumn[ConcreteNestedMapsTable, NestedMapRecord, NestedMaps](this)

  override def fromRow(r: Row): NestedMapRecord = {
    NestedMapRecord(
      id = id(r),
      people = people(r),
      addresses = addresses(r)
    )
  }
}


abstract class ConcreteNestedMapsTable extends NestedMapsTable with RootConnector {

  def store(rec: NestedMapRecord): Future[ResultSet] = {
    insert
      .value(_.id, rec.id)
      .value(_.people, rec.people)
      .value(_.addresses, rec.addresses)
      .future()
  }

  def findById(id: UUID): Future[Option[NestedMapRecord]] = {
    select.where(_.id eqs id).one()
  }
}