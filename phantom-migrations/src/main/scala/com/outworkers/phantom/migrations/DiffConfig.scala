package com.outworkers.phantom.migrations

sealed case class DiffConfig(
  allowNonOptional: Boolean,
  allowSecondaryOverwrites: Boolean,
  allowPrimaryOverwrites: Boolean = true,
  allowSecondaryIndexOverwrites: Boolean = true,
  allowMaterializedViewOverwrites: Boolean = true,
  allowUdtOverwrites: Boolean = true
)
