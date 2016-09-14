package com.outworkers.phantom.udt.builder

import com.outworkers.phantom.udt.{Test, Test2, UDTPrimitive}
import com.outworkers.util.testing._
import com.websudos.phantom.connectors.KeySpace
import org.scalatest.{FlatSpec, Matchers}

class SchemaDerivationTest extends FlatSpec with Matchers {

  implicit object TestGenerator extends Sample[Test] {
    override def sample: Test = Test(
      gen[Int],
      gen[String]
    )
  }

  implicit object Test2Generator extends Sample[Test2] {
    override def sample: Test2 = Test2(
      gen[Int],
      gen[String],
      gen[BigDecimal],
      5
    )
  }

  implicit val keySpace = KeySpace("phantom_udt")

  it should "automatically derive the type of a schema from a class instance" in {
    val schema = implicitly[UDTPrimitive[Test]].schemaQuery()

    schema.queryString shouldEqual "CREATE TYPE IF NOT EXISTS phantom_udt.test id int, name text"
  }

}
