package com.outworkers.phantom.autotables

import com.outworkers.phantom.autotables.tables.Users
import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoint}
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl.DatabaseProvider

object Defaults {
  lazy val connector = ContactPoint.local.keySpace("phantom_pro")
}

class AutoDb(
  override val connector: CassandraConnection
) extends Database[AutoDb](connector) {
  object users extends Users with Connector
}


object AutoDb extends AutoDb(Defaults.connector)

trait AutoDBProvider extends DatabaseProvider[AutoDb] {
  override def database: AutoDb = AutoDb
}