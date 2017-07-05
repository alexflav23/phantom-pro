/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.udt.builder

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.{Test, TestDbProvider, UDTPrimitive}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class SchemaDerivationTest extends FlatSpec with Matchers with BeforeAndAfterAll with TestDbProvider {

  override def beforeAll(): Unit = {
    super.beforeAll()
    db.create()
  }

  implicit val keySpace = KeySpace("phantom_udt")

  it should "automatically derive the type of a schema from a class instance" in {
    val p = implicitly[UDTPrimitive[Test]]
    val schema = p.schemaQuery()

    schema.queryString shouldEqual "CREATE TYPE IF NOT EXISTS phantom_udt.test (id int, name text)"
  }

  it should "automatically freeze a primary udt collection column" in {
    val cType = database.primaryCollectionTable.previous_addresses.cassandraType
    cType.indexOf("frozen") shouldNot equal (-1)
  }
}
