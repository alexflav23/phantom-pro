package com.outworkers.phantom.udt.builder

import com.outworkers.phantom.udt.{SchemaGenerator, Test2}
import com.outworkers.util.testing._
import com.websudos.phantom.connectors.KeySpace
import org.scalatest.{FlatSpec, Matchers}

import scala.reflect._
import shapeless._
import shapeless.record._
import shapeless.labelled._
import shapeless.ops.record._

class SchemaDerivationTest extends FlatSpec with Matchers {

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
    val test = gen[Test2]

    //val schema = SchemaGenerator.schema(test)

    //schema shouldEqual "CREATE TYPE IF NOT EXISTS phantom_udt.test2 id int, name text, dec decimal, sh smallint"
  }

}
