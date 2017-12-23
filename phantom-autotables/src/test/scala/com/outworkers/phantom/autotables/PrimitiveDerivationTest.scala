/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.autotables

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, OptionValues}
import com.outworkers.util.samplers._
import com.outworkers.phantom.dsl._
import org.scalatest.concurrent.ScalaFutures

class PrimitiveDerivationTest extends FlatSpec with Matchers with AutoDBProvider with OptionValues with ScalaFutures with BeforeAndAfterAll {

  override def beforeAll: Unit = {
    super.beforeAll()
    db.create()
  }

  it should "store a user and retrieve one from the db" in {
    val user = gen[User]

    val chain = for {
      _ <- db.users.store(user).future()
      one <- db.users.findById(user.id)
    } yield one

    whenReady(chain) { res =>
      res shouldBe defined
      res.value.id shouldEqual user.id
      res.value.email shouldEqual user.email
      res.value.location shouldEqual user.location
      res.value.previousLocations should contain theSameElementsAs user.previousLocations
    }
  }

  it should "allow appending values to a list collection with derived primitives" in {
    val user = gen[User]
    val loc = gen[Location]

    val chain = for {
      _ <- db.users.store(user).future()
      one <- db.users.findById(user.id)
      update <- db.users.update.where(_.id eqs user.id).modify(_.previousLocations add loc).future()
      one2 <- db.users.findById(user.id)
    } yield (one, one2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value shouldEqual user

      afterUpdate.value.id shouldEqual user.id
      afterUpdate.value.email shouldEqual user.email
      afterUpdate.value.location shouldEqual user.location
      afterUpdate.value.previousLocations should contain theSameElementsAs (user.previousLocations + loc)
    }
  }

}
