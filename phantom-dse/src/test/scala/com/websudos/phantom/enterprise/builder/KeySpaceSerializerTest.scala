package com.websudos.phantom.enterprise.builder

import org.scalatest.{FlatSpec, Matchers}

class KeySpaceSerializerTest extends FlatSpec with Matchers {

  it should "create a simple keyspace creation query" in {
    val query = KeySpaceSerializer("test").ifNotExists()
      .strategy(NetworkTopologyStrategy)
      .durable_writes(flag = false)
      .qb.queryString

    Console.println(query)

    query shouldEqual "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'NetworkTopologyStrategy'}"

  }
}
