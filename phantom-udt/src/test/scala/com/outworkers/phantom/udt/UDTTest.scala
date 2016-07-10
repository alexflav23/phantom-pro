package com.outworkers.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import org.scalatest.{FlatSpec, Matchers}

class UdtTest extends FlatSpec with Matchers {

  case class TestRecord(uuid: UUID, udt: Test2, udt2: Test2)

  class TestTable extends CassandraTable[TestTable, TestRecord] {

    object uuid extends UUIDColumn(this)

    object udt extends UDTColumn[TestTable, TestRecord, Test2](this)

    object udt2 extends UDTColumn[TestTable, TestRecord, Test2](this)

    override def fromRow(r: Row): TestRecord = {
      TestRecord(uuid(r), udt(r), udt2(r))
    }
  }

  it should "deserialize row" in {
    val test = Test(1, "hello")
  }

}
