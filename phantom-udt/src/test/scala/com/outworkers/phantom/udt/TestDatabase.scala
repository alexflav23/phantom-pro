/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.udt

import com.datastax.driver.core.{HostDistance, PoolingOptions}
import com.outworkers.phantom.builder.query.{CassandraOperations, ExecutableStatementList}
import com.outworkers.phantom.builder.serializers.KeySpaceSerializer
import com.outworkers.phantom.dsl.{context => _, _}
import com.outworkers.phantom.udt.domain.OptionalUdt
import com.outworkers.phantom.udt.tables._

import scala.concurrent.{ExecutionContextExecutor, Future}

class TestDatabase(override val connector: KeySpaceDef) extends Database[TestDatabase](connector) with CassandraOperations {

  object udtTable extends TestTable with Connector

  object nestedUdtTable extends NestedUdtsTable with Connector

  object nestedSetUdtTable extends NestedUdtSetsTable with Connector

  object nestedMapUdtTable extends NestedMapsTable with Connector

  object collectionTable extends UDTCollectionsTable with Connector

  object primaryCollectionTable extends PrimaryUDTCollectionsTable with Connector

  object optionalUdts extends OptionalUDTsTable with Connector

  object policies extends AuthorizerPolicy with Connector

  object derivedUdts extends DerivedEncoderUdtTable with Connector

  def initUds: ExecutableStatementList[Seq] = {
    new ExecutableStatementList[Seq](
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

  override def createAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    initUds.sequentialFuture() flatMap(_ => super.createAsync())
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
