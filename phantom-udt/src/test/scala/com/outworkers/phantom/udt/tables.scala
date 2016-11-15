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

class TestTable extends CassandraTable[ConcreteTestTable, TestRecord] {

  object uuid extends UUIDColumn(this) with PartitionKey[UUID]

  object udt extends UDTColumn[ConcreteTestTable, TestRecord, Test](this)

  object udt2 extends UDTColumn[ConcreteTestTable, TestRecord, Test2](this)

  object col extends UDTColumn[ConcreteTestTable, TestRecord, ListCollectionUdt](this)

  override def fromRow(r: Row): TestRecord = {
    TestRecord(uuid(r), udt(r), udt2(r), col(r))
  }
}

abstract class ConcreteTestTable extends TestTable with RootConnector {

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

