package com.outworkers.phantom.udt

import scala.concurrent.Future
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.columns.UDTColumn

@Udt case class Test(id: Int, name: String)

@Udt case class Test2(id: Int, name: String, dec: BigDecimal, sh: Short)

@Udt case class ListCollectionUdt(id: UUID, name: String, items: List[String])

case class TestRecord(
  uuid: UUID,
  udt: Test,
  udt2: Test2,
  col: ListCollectionUdt
)

abstract class TestTable extends CassandraTable[TestTable, TestRecord] with RootConnector {

  object uuid extends UUIDColumn(this) with PartitionKey

  object udt extends UDTColumn[TestTable, TestRecord, Test](this)

  object udt2 extends UDTColumn[TestTable, TestRecord, Test2](this)

  object col extends UDTColumn[TestTable, TestRecord, ListCollectionUdt](this)

  def store(record: TestRecord): Future[ResultSet] = {
    insert
      .value(_.uuid, record.uuid)
      .value(_.udt, record.udt)
      .value(_.udt2, record.udt2)
      .value(_.col, record.col)
      .future()
  }

  def getById(id: UUID): Future[Option[TestRecord]] = {
    select.where(_.uuid eqs id).one
  }
}

