package com.outworkers.phantom.udt

import com.outworkers.util.testing._
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.tables._
import org.scalatest._

class UdtTest extends FlatSpec with PhantomTest {

  it should "store and retrieve a record that contains UDT columns" in {
    val sample = gen[TestRecord]

    val chain = for {
      store <- database.udtTable.store(sample).future
      get <- database.udtTable.getById(sample.uuid)
    } yield get

    whenReady(chain)(res =>
      res.value shouldEqual sample
    )
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
      store <- database.nestedUdtTable.store(sample).future
      get <- database.nestedUdtTable.findById(sample.id)
    } yield get

    whenReady(chain)(_.value shouldEqual sample)
  }

  it should "store and retrieve a complex record with a nested UDT definition containing a set" in {
    val sample = gen[NestedSetRecord]

    val chain = for {
      store <- database.nestedSetUdtTable.store(sample).future
      get <- database.nestedSetUdtTable.findById(sample.id)
    } yield get

    whenReady(chain)(_.value shouldEqual sample)
  }

  it should "retrieve a complex record with a nested map collection" in {
    val sample = gen[NestedMapRecord]

    Console.println(database.nestedMapUdtTable.store(sample).queryString)

    val chain = for {
      store <- database.nestedMapUdtTable.store(sample).future
      get <- database.nestedMapUdtTable.findById(sample.id)
    } yield get

    whenReady(chain)(_.value shouldEqual sample)
  }
}
