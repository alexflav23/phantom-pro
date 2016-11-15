package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.connectors.ContactPoint

object Defaults {
  val connector = ContactPoint.local.keySpace("phantom_migrations_test")
}

trait Connector extends Defaults.connector.Connector
