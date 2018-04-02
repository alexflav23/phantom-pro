package com.outworkers.phantom.migrations.diffs

trait DiffConflict

case class TypeMismatch(
  column: String,
  phantomType: String,
  cassandraType: String
) extends DiffConflict


case class InvalidAddition(
  column: String,
  cassandraType: String,
  reason: String
) extends DiffConflict
