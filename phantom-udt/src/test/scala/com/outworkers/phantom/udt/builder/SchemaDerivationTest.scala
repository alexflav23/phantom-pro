package com.outworkers.phantom.udt.builder

import com.outworkers.phantom.udt.{Samplers, Test, TestDatabase, UDTPrimitive}
import com.outworkers.util.testing._
import com.outworkers.phantom.dsl._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class SchemaDerivationTest extends FlatSpec with Matchers with BeforeAndAfterAll with Samplers {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.create()
  }

  implicit val keySpace = KeySpace("phantom_udt")

  it should "automatically derive the type of a schema from a class instance" in {
    val p = implicitly[UDTPrimitive[Test]]
    val schema = p.schemaQuery()

    schema.queryString shouldEqual "CREATE TYPE IF NOT EXISTS phantom_udt.test (id int, name text)"
  }
}
