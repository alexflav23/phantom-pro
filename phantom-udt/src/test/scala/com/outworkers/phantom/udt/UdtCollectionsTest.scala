package com.outworkers.phantom.udt

import com.outworkers.phantom.udt.tables.Person
import org.scalatest.FlatSpec
import com.outworkers.util.testing._
import com.outworkers.phantom.dsl._

import scala.concurrent.Await

class UdtCollectionsTest extends FlatSpec with PhantomTest {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(TestDatabase.createUdts.future() flatMap(_ => TestDatabase.createAsync()), defaultScalaTimeout)
  }

  it should "create and retrieve a list of UDTs from a Cassandra table" in {
    val sample = gen[Person]

    val chain = for {
      store <- TestDatabase.collectionTable.store(sample)
      retrieve <- TestDatabase.collectionTable.findById(sample.id)
    } yield retrieve

    whenReady(chain) { res =>
      res.value.id shouldEqual sample.id
      res.value.current_addresses should contain theSameElementsAs sample.current_addresses
      res.value.previous_addresses should contain theSameElementsAs sample.previous_addresses
    }
  }
}
