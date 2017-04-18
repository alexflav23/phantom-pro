package com.outworkers.phantom.udt

import com.datastax.driver.core.{HostDistance, PoolingOptions}
import com.outworkers.phantom.builder.query.{CassandraOperations, ExecutableStatementList}
import com.outworkers.phantom.builder.serializers.KeySpaceSerializer
import com.outworkers.phantom.dsl.{context => _, _}
import com.outworkers.phantom.udt.tables._

import scala.concurrent.{ExecutionContextExecutor, Future}

class TestDatabase(override val connector: KeySpaceDef) extends Database[TestDatabase](connector) with CassandraOperations {

  object udtTable extends TestTable with Connector

  object nestedUdtTable extends NestedUdtsTable with Connector

  object nestedSetUdtTable extends NestedUdtSetsTable with Connector

  object nestedMapUdtTable extends NestedMapsTable with Connector

  object collectionTable extends UDTCollectionsTable with Connector

  object primaryCollectionTable extends PrimaryUDTCollectionsTable with Connector

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
        UDTPrimitive[ListCollectionUdt].schemaQuery()
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
    ).keySpace(space.name, KeySpaceSerializer(space.name).ifNotExists()
      .`with`(replication eqs SimpleStrategy.replication_factor(1))
      .and(durable_writes eqs true)
    )
}

object TestDatabase extends TestDatabase(TestConnector.connector)

trait TestDbProvider extends DatabaseProvider[TestDatabase] {
  override def database: TestDatabase = TestDatabase
}
