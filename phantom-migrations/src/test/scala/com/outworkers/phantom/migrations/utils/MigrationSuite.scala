package com.outworkers.phantom.migrations.utils

import cats.scalatest.{ValidatedMatchers, ValidatedValues}
import com.outworkers.phantom.migrations.tables.MigrationDbProvider
import org.scalatest._

trait MigrationSuite extends Suite
  with BeforeAndAfterAll
  with Matchers
  with ValidatedMatchers
  with ValidatedValues
  with MigrationDbProvider
