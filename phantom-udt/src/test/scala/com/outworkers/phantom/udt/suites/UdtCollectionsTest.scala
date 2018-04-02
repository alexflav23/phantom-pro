/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt.suites

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.tables.{Address, Person}
import com.outworkers.util.testing._
import org.scalatest.FlatSpec

class UdtCollectionsTest extends FlatSpec with PhantomTest {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.create()
  }

  it should "create and retrieve a list of UDTs from a Cassandra table" in {
    val sample = gen[Person]

    val chain = for {
      _ <- database.collectionTable.store(sample).future()
      retrieve <- database.collectionTable.findById(sample.id)
    } yield retrieve

    whenReady(chain) { res =>
      res.value.id shouldEqual sample.id
      res.value.current_addresses should contain theSameElementsAs sample.current_addresses
      res.value.previous_addresses should contain theSameElementsAs sample.previous_addresses
    }
  }

  it should "append an UDT object to an udt collection" in {
    val sample = gen[Person]
    val address = gen[Address]

    val chain = for {
      _ <- database.collectionTable.store(sample).future()
      beforeUpdate <- database.collectionTable.findById(sample.id)
      _ <- database.collectionTable.update.where(_.id eqs sample.id)
        .modify(_.current_addresses add address).future()
      afterUpdate <- database.collectionTable.findById(sample.id)
    } yield (beforeUpdate, afterUpdate)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>

      beforeUpdate.value.current_addresses should contain theSameElementsAs sample.current_addresses
      beforeUpdate.value.previous_addresses should contain theSameElementsAs sample.previous_addresses

      afterUpdate.value.current_addresses should contain theSameElementsAs (sample.current_addresses + address)
    }
  }

  it should "store an item in a table with an UDT collection" in {
    val sample = gen[Person]

    val chain = for {
      _ <- database.primaryCollectionTable.store(sample).future
      retrieve <- database.primaryCollectionTable.findByIdAndAddresses(sample.id, sample.previous_addresses)
    } yield retrieve

    whenReady(chain) { res =>
      res shouldBe defined
      res.value.current_addresses should contain theSameElementsAs sample.current_addresses
      res.value.previous_addresses should contain theSameElementsAs sample.previous_addresses
    }
  }
}
