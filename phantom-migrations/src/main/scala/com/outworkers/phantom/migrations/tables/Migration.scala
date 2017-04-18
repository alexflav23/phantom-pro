/*
 * Copyright 2013-2018 Outworkers, Limited.
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
package com.outworkers.phantom.migrations.tables

import com.datastax.driver.core.TableMetadata
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.ExecutableStatementList
import com.outworkers.phantom.connectors.KeySpace
import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.concurrent.ExecutionContext

case class ColumnDiff(
  name: String,
  cassandraType: String,
  isOptional: Boolean,
  isPrimary: Boolean,
  isSecondary: Boolean,
  isStatic: Boolean
)

sealed case class Migration[Table <: CassandraTable[Table, _], R](additions: Set[ColumnDiff], deletions: Set[ColumnDiff]) {

  def additiveQueries(table: Table)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): Set[CQLQuery] = {
    additions map {
      col: ColumnDiff => table.alter.add(col.name, col.cassandraType).qb
    }
  }

  def subtractionQueries(table: Table)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): Set[CQLQuery] = {
    deletions map { col => table.alter.drop(col.name).qb }
  }

  def queryList(table: Table)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): Set[CQLQuery] = {
    additiveQueries(table) ++ subtractionQueries(table)
  }

  def automigrate(table: Table)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): ExecutableStatementList[Seq] = {
    new ExecutableStatementList(queryList(table).toSeq)
  }
}


object Migration {
  def apply[
    Table <: CassandraTable[Table, _],
    Record
  ](metadata: TableMetadata, table: Table)(implicit diffConfig: DiffConfig): Migration[Table, Record] = {

    val dbTable = Diff(metadata)
    val phantomTable = Diff(table)

    Migration(
      phantomTable diff dbTable migrations(),
      dbTable diff phantomTable migrations()
    )
  }

  def apply[
    Table <: CassandraTable[Table, _],
    Record
  ](first: Table, second: Table)(implicit diffConfig: DiffConfig): Migration[Table, Record] = {
    val firstDiff = Diff(first)
    val secondDiff = Diff(second)

    Migration(
      firstDiff diff secondDiff migrations(),
      secondDiff diff firstDiff migrations()
    )
  }
}
