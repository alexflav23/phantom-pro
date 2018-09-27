/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.monix

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.dse.PhantomSuite
import com.outworkers.phantom.monix.tables.{PrimitiveRecord, Recipe, TestDatabaseProvider, TimeUUIDRecord}
import org.joda.time.DateTimeZone
import com.outworkers.util.samplers._

import scala.util.Try

class SelectFunctionsTesting extends PhantomSuite with MonixScalaTest with TestDatabaseProvider {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.createSchema()
    database.timeuuidTable.createSchema()
    database.primitives.createSchema()
  }

  it should "retrieve the writetime of a field from Cassandra" in {
    val record = gen[Recipe]

    val chain = for {
      _ <- database.recipes.store(record).future()
      timestamp <- database.recipes.select
        .function(t => writetime(t.description))
        .where(_.url eqs record.url).one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
      Try(new DateTime(res.value._1 / 1000, DateTimeZone.UTC)).isSuccess shouldEqual true
    }
  }

  it should "retrieve the dateOf of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord].copy(id = UUIDs.timeBased())

    val chain = for {
      _ <- database.timeuuidTable.store(record).future()
      timestamp <- database.timeuuidTable.select.function(t => dateOf(t.id)).where(_.user eqs record.user)
        .and(_.id eqs record.id).one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the unixTimestamp of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord].copy(id = UUIDs.timeBased())

    val chain = for {
      _ <- database.timeuuidTable.store(record).future()
      timestamp <- database.timeuuidTable.select.function(t => unixTimestampOf(t.id)).where(_.user eqs record.user)
        .and(_.id eqs record.id).one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the TTL of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord].copy(id = UUIDs.timeBased())
    val timeToLive = 20

    val potentialList = List(timeToLive - 2, timeToLive - 1, timeToLive)

    val chain = for {
      _ <- database.timeuuidTable.store(record).ttl(timeToLive).future()
      timestamp <- database.timeuuidTable.select.function(t => ttl(t.name))
        .where(_.user eqs record.user)
        .and(_.id eqs record.id)
        .one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
      potentialList should contain (res.value._1.value)
    }
  }

  // SUM function
  it should "retrieve the sum of an int field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => sum(t.int)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the sum of a double field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => sum(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the sum of a Float field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => sum(t.float)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the sum of a BigDecimal field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => sum(t.bDecimal)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the sum of a BigInt field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => sum(t.bi)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the sum of a long field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => sum(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  // MIN Aggregation
  it should "retrieve the MIN of an int field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.int)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the MIN of a double field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the MIN of a Float field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.float)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the MIN of a BigDecimal field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.bDecimal)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the MIN of a BigInt field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.bi)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the MIN of a Long field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  // MAX Aggregation
  it should "retrieve the MAX of an int field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.int)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the MAX of a double field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the MAX of a Float field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.float)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the MAX of a BigDecimal field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.bDecimal)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the MAX of a BigInt field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.bi)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the MAX of a Long field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  // AVG Aggregation
  it should "retrieve the average of an int field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => avg(t.int)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the average of a double field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => avg(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the average of a Float field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => avg(t.float)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the average of a BigDecimal field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => min(t.bDecimal)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }


  it should "retrieve the average of a BigInt field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => avg(t.bi)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the average of a Long field from Cassandra" in {
    val record = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(record).future()
      res <- database.primitives.select.function(t => avg(t.long)).where(_.pkey eqs record.pkey).aggregate()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }
}
