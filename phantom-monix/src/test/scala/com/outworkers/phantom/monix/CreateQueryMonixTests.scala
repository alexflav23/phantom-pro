/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.monix

import com.outworkers.phantom.dse.PhantomSuite
import com.outworkers.phantom.monix.tables.TestDatabaseProvider

class CreateQueryMonixTests extends PhantomSuite with MonixScalaTest with TestDatabaseProvider {

  it should "execute a simple query with secondary indexes with Twitter futures" in {
    whenReady(database.secondaryIndexTable.create.ifNotExists().future()) { res =>
      info("The creation query of secondary indexes should execute successfully")
      res.forall(_.wasApplied() == true) shouldEqual true
    }
  }
}
