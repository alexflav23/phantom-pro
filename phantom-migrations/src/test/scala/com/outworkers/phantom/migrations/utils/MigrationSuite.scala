package com.outworkers.phantom.migrations.utils

import cats.scalatest.{ValidatedMatchers, ValidatedValues}
import com.outworkers.phantom.migrations.tables.MigrationDbProvider
import org.scalatest._
import org.scalatest.matchers.should.Matchers

trait MigrationSuite extends Suite
  with BeforeAndAfterAll
  with Matchers
  with ValidatedMatchers
  with ValidatedValues
  with MigrationDbProvider
