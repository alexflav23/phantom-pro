package com.outworkers.phantom.monix.tables

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.connectors
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.{Database, DatabaseProvider}
import com.outworkers.phantom.monix._

object Connector {
  val default: CassandraConnection = connectors.ContactPoint.local
    .withClusterBuilder(_.withSocketOptions(
      new SocketOptions()
        .setConnectTimeoutMillis(20000)
        .setReadTimeoutMillis(20000)
    )
    ).noHeartbeat().keySpace(
    KeySpace("phantom").ifNotExists().`with`(
      replication eqs SimpleStrategy.replication_factor(1)
    )
  )
}

class TestDatabase(override val connector: CassandraConnection) extends Database[TestDatabase](connector) {
  object recipes extends Recipes with Connector
  object primitives extends PrimitivesTable with Connector
  object secondaryIndexTable extends SecondaryIndexTable with Connector
  object timeuuidTable extends TimeUUIDTable with Connector
}

object TestDatabase extends TestDatabase(Connector.default)

trait TestDatabaseProvider extends DatabaseProvider[TestDatabase] {
  override val database: TestDatabase = TestDatabase
}
