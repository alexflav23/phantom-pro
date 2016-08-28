package com.websudos.phantom.dse.builder

import org.scalatest.{FlatSpec, Matchers}
import com.websudos.phantom.dse._

class KeySpaceSerializerTest extends FlatSpec with Matchers {

  it should "create a simple keyspace creation query" in {
    val query = KeySpaceSerializer("test").ifNotExists()
      .`with`(replication eqs NetworkTopologyStrategy)
      .and(durable_writes eqs true)
      .qb.queryString

    val expected = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'NetworkTopologyStrategy'}" +
      " AND DURABLE_WRITES = true"

    query shouldEqual expected
  }
}
