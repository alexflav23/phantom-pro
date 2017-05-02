/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.connectors.ContactPoint

object Defaults {
  lazy val connector = ContactPoint.local.keySpace("phantom_migrations_test")
}
