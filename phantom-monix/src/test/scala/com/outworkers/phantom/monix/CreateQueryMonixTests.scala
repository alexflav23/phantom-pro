/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
