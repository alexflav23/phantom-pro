/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.websudos.phantom.migrations

import com.datastax.driver.core.TableMetadata
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.{CQLQuery, ExecutableStatementList}
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.dsl.Session

import scala.concurrent.ExecutionContext


sealed case class ColumnDiff(
  name: String,
  cassandraType: String,
  isPrimary: Boolean,
  isSecondary: Boolean,
  isStatic: Boolean
)

sealed case class Migration(additions: Set[ColumnDiff], deletions: Set[ColumnDiff]) {

  def additiveQueries(table: CassandraTable[_, _])
      (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): Set[CQLQuery] = {
    additions map {
      col: ColumnDiff => {
        table.alter.add(col.name, col.cassandraType).qb
      }
    }
  }

  def subtractionQueries(table: CassandraTable[_, _])
      (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): Set[CQLQuery] = {
    deletions map {
      col: ColumnDiff => {
        table.alter.drop(col.name).qb
      }
    }
  }

  def queryList(table: CassandraTable[_, _])
      (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): Set[CQLQuery] = {
    additiveQueries(table) ++ subtractionQueries(table)
  }

  def automigrate(table: CassandraTable[_, _])
      (implicit session: Session, keySpace: KeySpace, executionContext: ExecutionContext): ExecutableStatementList = {
    new ExecutableStatementList(queryList(table).toSeq)
  }
}


object Migration {
  def apply(metadata: TableMetadata, table: CassandraTable[_, _]): Migration = {

    val dbTable = Diff(metadata)
    val phantomTable = Diff(table)

    Migration(
      phantomTable diff dbTable migrations(),
      dbTable diff phantomTable migrations()
    )

  }
}

