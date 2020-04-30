/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt.builder

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.tables.Test
import com.outworkers.phantom.udt.{TestDbProvider, UDTPrimitive}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers

class SchemaDerivationTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll with TestDbProvider {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = db.create()
  }

  implicit val keySpace: KeySpace = KeySpace("phantom_udt")

  it should "automatically derive the type of a schema from a class instance" in {
    val p = implicitly[UDTPrimitive[Test]]
    val schema = p.schemaQuery()

    schema.qb.queryString shouldEqual "CREATE TYPE IF NOT EXISTS phantom_udt.test (id int, name text)"
  }

  it should "automatically freeze a primary udt collection column" in {
    val cType = database.primaryCollectionTable.previous_addresses.cassandraType
    cType.indexOf("frozen") shouldNot equal (-1)
  }
}
