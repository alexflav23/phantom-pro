package com.websudos.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.dsl.{CassandraTable, UUID, UUIDColumn}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try


class UdtTest extends FlatSpec with Matchers {


  @Udt
  case class Test(id: Int, name: String)

  case class TestRecord(uuid: UUID, udt: Test)

  class TestTable extends CassandraTable[TestTable, TestRecord] {

    object uuid extends UUIDColumn(this)

    object udt extends UDTColumn[TestTable, TestRecord, Test](this)

    override def fromRow(r: Row): TestRecord = {
      TestRecord(uuid(r), udt(r))
    }
  }

  it should "deserialize row" in {
    val test = Test(1, "hello")
  }

}
