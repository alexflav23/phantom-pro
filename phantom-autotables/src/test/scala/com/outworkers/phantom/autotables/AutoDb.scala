/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
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