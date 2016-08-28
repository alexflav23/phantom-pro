package com.outworkers.phantom.udt

import java.util.UUID

import com.datastax.driver.core.{Row, UDTValue}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.dsl._

import scala.concurrent.Future

case class Test(id: Int, name: String)

case class Test2(id: Int, name: String, dec: BigDecimal, sh: Short)

object Test2 {

  implicit object Test2UdtPrimitive extends UDTPrimitive[Test2] {

    override def name: String = "Test2"

    override def fromRow(row: UDTValue): Option[Test2] = {
      val accessors = Helper.classAccessors[Test2]
      // UdtExtractor.extractor[Test2](accessors).toOption
      None
    }

    override def asCql(udt: Test2): String = {
      s"""{
          |'id': "${Primitive[Int].asCql(udt.id)},
          |'name': ${Primitive[String].asCql(udt.name)}
          |}""".stripMargin
    }

    override def instance: Test2 = Test2(5, "", BigDecimal(0), 5)

    override def schemaQuery()(implicit space: KeySpace): CQLQuery = {
      CQLQuery(SchemaGenerator.schema(instance))
    }
  }
}

case class TestRecord(uuid: UUID, udt: Test2, udt2: Test2)

class TestTable extends CassandraTable[ConcreteTestTable, TestRecord] {

  object uuid extends UUIDColumn(this) with PartitionKey[UUID]

  object udt extends UDTColumn[ConcreteTestTable, TestRecord, Test2](this)

  object udt2 extends UDTColumn[ConcreteTestTable, TestRecord, Test2](this)

  override def fromRow(r: Row): TestRecord = {
    TestRecord(uuid(r), udt(r), udt2(r))
  }
}

abstract class ConcreteTestTable extends TestTable with RootConnector {
  def store(record: TestRecord): Future[ResultSet] = {
    insert.value(_.uuid, record.uuid)
      .value(_.udt, record.udt)
      .value(_.udt2, record.udt2)
      .future()
  }

  def getById(id: UUID): Future[Option[TestRecord]] = {
    select.where(_.uuid eqs id).one
  }
}

