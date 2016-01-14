package com.websudos.phantom

import com.datastax.driver.core.Session
import com.websudos.phantom.connectors.KeySpace

import scala.concurrent.ExecutionContext

package object migrations {

  implicit class TableMigrations[T <: CassandraTable[T, R], R](val table: CassandraTable[T, R]) extends AnyVal {

    def automigrate()(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext, diffConfig: DiffConfig) = {
      Differ.automigrate(table)
    }

    def automigrate(diffConfig: DiffConfig)(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext) = {
      Differ.automigrate(table)(session, keySpace, ec, diffConfig)
    }
  }
}
