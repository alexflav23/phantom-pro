package com.outworkers.phantom.udt

import com.outworkers.util.testing._
import com.websudos.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, OptionValues}

class UdtTest extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with OptionValues with Samplers {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.create()
  }

  it should "store and retrieve a record that contains UDT columns" in {
    val sample = gen[TestRecord]

    val chain = for {
      store <- TestDatabase.udtTable.store(sample)
      get <- TestDatabase.udtTable.getById(sample.uuid)
    } yield get

    whenReady(chain)(res => {
      res.value shouldEqual sample
    })
  }

}
