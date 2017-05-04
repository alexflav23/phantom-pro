/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.autotables

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, OptionValues}
import com.outworkers.util.testing._
import com.outworkers.phantom.dsl.context
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
      res.value shouldEqual user
    }
  }

}
