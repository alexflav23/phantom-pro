/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.Udt

import scala.concurrent.Future

@Udt case class Test(id: Int, name: String)

@Udt case class Test2(id: Int, name: String, dec: BigDecimal, sh: Short)

@Udt case class ListCollectionUdt(id: UUID, name: String, items: List[String])

case class TestRecord(
  uuid: UUID,
  udt: Test,
  udt2: Test2,
  col: ListCollectionUdt
)

abstract class TestTable extends Table[TestTable, TestRecord] {

  object uuid extends UUIDColumn with PartitionKey

  object udt extends Col[Test]

  object udt2 extends Col[Test2]

  object col extends Col[ListCollectionUdt]

  def getById(id: UUID): Future[Option[TestRecord]] = {
    select.where(_.uuid eqs id).one
  }
}
