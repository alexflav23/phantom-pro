package com.outworkers.phantom.migrations

import com.datastax.driver.core.Session
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.connectors.KeySpace

import scala.concurrent.ExecutionContext

package object tables {

  implicit class TableMigrations[T <: CassandraTable[T, R], R](val table: T) extends AnyVal {

    def automigrate()(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext, diffConfig: DiffConfig) = {
      Differ.automigrate[T, R](table)
    }

    def automigrate(diffConfig: DiffConfig)(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext) = {
      Differ.automigrate[T, R](table)(session, keySpace, ec, diffConfig)
    }
  }
}
