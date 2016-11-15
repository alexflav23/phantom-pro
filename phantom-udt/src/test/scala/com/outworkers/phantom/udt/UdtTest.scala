package com.outworkers.phantom.udt

import com.outworkers.util.testing._
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.tables._
import org.scalatest._

class UdtTest extends FlatSpec with PhantomTest {

  it should "store and retrieve a record that contains UDT columns" in {
    val sample = gen[TestRecord]

    val chain = for {
      store <- TestDatabase.udtTable.store(sample)
      get <- TestDatabase.udtTable.getById(sample.uuid)
    } yield get

    whenReady(chain)(res => {
      res.value shouldEqual sample
    })
  }

  it should "generate the schema of a nested UDT column" in {
    val query = UDTPrimitive[Address].asCql(Address(
      "test",
      Location(5L, 10L),
      "SW1X 7QT"
    ))

    info(query)

    query shouldEqual """{name: 'test', location: {longitude: 5, latitude: 10}, postcode: 'SW1X 7QT'}"""
  }

  it should "store and retrieve a complex record with a nested UDT definition containing a list" in {
    val sample = gen[NestedRecord]

    val chain = for {
      store <- TestDatabase.nestedUdtTable.store(sample)
      get <- TestDatabase.nestedUdtTable.findById(sample.id)
    } yield get

    whenReady(chain)(_.value shouldEqual sample)
  }

  it should "store and retrieve a complex record with a nested UDT definition containing a set" in {
    val sample = gen[NestedSetRecord]

    val chain = for {
      store <- TestDatabase.nestedSetUdtTable.store(sample)
      get <- TestDatabase.nestedSetUdtTable.findById(sample.id)
    } yield get

    whenReady(chain)(_.value shouldEqual sample)
  }

  it should "retrieve a complex record with a nested map collection" in {
    val sample = gen[NestedMapRecord]

    val chain = for {
      store <- TestDatabase.nestedMapUdtTable.store(sample)
      get <- TestDatabase.nestedMapUdtTable.findById(sample.id)
    } yield get

    whenReady(chain)(_.value shouldEqual sample)
  }
}
