/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations

sealed case class DiffConfig(
  allowNonOptional: Boolean,
  allowSecondaryOverwrites: Boolean,
  allowPrimaryOverwrites: Boolean = true,
  allowSecondaryIndexOverwrites: Boolean = true,
  allowMaterializedViewOverwrites: Boolean = true,
  allowUdtOverwrites: Boolean = true
)
