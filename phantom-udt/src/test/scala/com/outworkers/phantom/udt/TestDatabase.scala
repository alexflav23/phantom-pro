/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt

import com.datastax.driver.core.{HostDistance, PoolingOptions}
import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.builder.serializers.KeySpaceSerializer
import com.outworkers.phantom.dsl.{context => _, _}
import com.outworkers.phantom.udt.domain.OptionalUdt
import com.outworkers.phantom.udt.tables._
import shapeless.HNil

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

class TestDatabase(override val connector: KeySpaceDef) extends Database[TestDatabase](connector) { outer =>

  object udtTable extends TestTable with Connector

  object nestedUdtTable extends NestedUdtsTable with Connector

  object nestedSetUdtTable extends NestedUdtSetsTable with Connector

  object nestedMapUdtTable extends NestedMapsTable with Connector

  object collectionTable extends UDTCollectionsTable with Connector

  object primaryCollectionTable extends PrimaryUDTCollectionsTable with Connector

  object optionalUdts extends OptionalUDTsTable with Connector

  object policies extends AuthorizerPolicy with Connector

  object derivedUdts extends DerivedEncoderUdtTable with Connector

  def initUds: QueryCollection[Seq] = {
    new QueryCollection[Seq](
      Seq(
        UDTPrimitive[Location].schemaQuery(),
        UDTPrimitive[Address].schemaQuery(),
        UDTPrimitive[CollectionUdt].schemaQuery(),
        UDTPrimitive[NestedRecord].schemaQuery(),
        UDTPrimitive[NestedMaps].schemaQuery(),
        UDTPrimitive[CollectionSetUdt].schemaQuery(),
        UDTPrimitive[NestedSetRecord].schemaQuery(),
        UDTPrimitive[Test].schemaQuery(),
        UDTPrimitive[Test2].schemaQuery(),
        UDTPrimitive[ListCollectionUdt].schemaQuery(),
        UDTPrimitive[OptionalUdt].schemaQuery(),
        UDTPrimitive[ChargeDuration].schemaQuery(),
        UDTPrimitive[MaxDuration].schemaQuery(),
        UDTPrimitive[ParkingCharge].schemaQuery(),
        UDTPrimitive[DerivedAddress].schemaQuery()
      )
    )
  }

  def create()(implicit ex: ExecutionContextExecutor): Seq[Seq[ResultSet]] = {
    val chain = for {
      udts <- executeStatements(initUds).sequence()
      db <- outer.createAsync()
    } yield db

    Await.result(chain, 20.seconds)
  }
}

object TestConnector {

  val space = KeySpace("phantom_pro")

  val connector = ContactPoint.local
    .noHeartbeat()
    .withClusterBuilder(_.withoutJMXReporting()
      .withoutMetrics().withPoolingOptions(new PoolingOptions().setMaxConnectionsPerHost(HostDistance.LOCAL, 5))
    ).keySpace(KeySpaceSerializer(space).ifNotExists()
      .`with`(replication eqs SimpleStrategy.replication_factor(1))
      .and(durable_writes eqs true)
    )
}

object TestDatabase extends TestDatabase(TestConnector.connector)

trait TestDbProvider extends DatabaseProvider[TestDatabase] {
  override def database: TestDatabase = TestDatabase
}
