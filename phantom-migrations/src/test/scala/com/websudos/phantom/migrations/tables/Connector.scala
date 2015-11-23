package com.websudos.phantom.migrations.tables

import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.testkit.suites.SimpleCassandraConnector

trait Connector extends SimpleCassandraConnector {
  implicit val keySpace: KeySpace = KeySpace("phantom_migrations_test")
}