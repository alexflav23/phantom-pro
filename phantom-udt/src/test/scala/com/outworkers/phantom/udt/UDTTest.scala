package com.outworkers.phantom.udt

import java.util.UUID

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, OptionValues}
import com.websudos.phantom.dsl._
import com.outworkers.util.testing._

import scala.concurrent.duration._
class UdtTest extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with OptionValues {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.createUdts.block(10.seconds)
    TestDatabase.create()

  }

  ignore should "deserialize row" in {
    val test = Test2(1, "hello")
    val sample = TestRecord(UUID.randomUUID(), test, test)


    //val types = SchemaGenerator.inferSchema(sample)

    Console.println(SchemaGenerator.fields(test))

    //val fields = SchemaGenerator.classAccessors[Test2]

    /*
    val inferred = (fields zip types) map {
      case (name, tp) => s"${CQLQuery.escape(name)}: $tp"
    } mkString ","

    info(s"CREATE TYPE bla.test2 $inferred")

    val chain = for {
      store <-  TestDatabase.udtTable.store(sample)
      get <- TestDatabase.udtTable.getById(sample.uuid)
    } yield get*/

    /*whenReady(chain) {
      res => {
        res.value shouldEqual sample
      }
    }*/
  }


}
