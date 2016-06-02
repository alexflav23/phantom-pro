package com.websudos.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.dsl.{CassandraTable, UUID, UUIDColumn}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

@Udt
case class Test(id: Int, name: String)

case class Test2(id: Int, name: String)

object Test2 {
  implicit object Test2UdtPrimitive extends UDTType[Test2] {
    override def fromRow(row: Row): Try[Test2] = {
      for {
        id <- Primitive[Int].fromRow("id", row)
        str <- Primitive[String].fromRow("name", row)
      } yield Test2(id, str)
    }

    override def name: String = "Test2"

    override def asCql(udt: Test2): String =
      s"""{
         |'id': "${Primitive[Int].asCql(udt.id)},
         |'name': ${Primitive[String].asCql(udt.name)},
         |}""".stripMargin
  }
}

class UdtTest extends FlatSpec with Matchers {

  case class TestRecord(uuid: UUID, udt: Test, udt2: Test2)

  class TestTable extends CassandraTable[TestTable, TestRecord] {

    object uuid extends UUIDColumn(this)

    object udt extends UDTColumn[TestTable, TestRecord, Test](this)

    object udt2 extends UDTColumn[TestTable, TestRecord, Test2](this)

    override def fromRow(r: Row): TestRecord = {
      TestRecord(uuid(r), udt(r), udt2(r))
    }
  }

  it should "deserialize row" in {
    val test = Test(1, "hello")
  }

}
