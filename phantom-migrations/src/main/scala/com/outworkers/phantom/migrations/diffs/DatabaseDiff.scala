package com.outworkers.phantom.migrations.diffs

import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.database.Database

case class DatabaseDiff[
  DB <: Database[DB],
  DBO <: Database[DBO]
](
  diffs: QueryCollection[Seq]
) {

  def isEmpty: Boolean = diffs.isEmpty

  def nonEmpty: Boolean = diffs.queries.nonEmpty
}
