/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations

import cats.scalatest.ValidatedMatchers
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.migrations.diffs.{Diff, DiffConfig, InvalidAddition}
import com.outworkers.phantom.migrations.utils.MigrationSuite
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.featurespec.AnyFeatureSpec

class DiffTest extends AnyFeatureSpec
  with GivenWhenThen
  with MigrationSuite
  with ValidatedMatchers with ScalaFutures {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.create()
  }

  implicit val diffConfig: DiffConfig = {
    DiffConfig(
      allowNonOptional = true,
      allowSecondaryOverwrites = false
    )
  }


  info("As a developer")
  info("I want to automatically resolve schema discrepancies")
  info("Between existing nodes")

  Feature("The column differ should compute the differences between two tables") {

    Scenario("The table is being diffed against itself") {
      Given("A valid Cassandra table schema is used")

      When("A table is diffed against itself")
      val diff = Diff(database.sampleTableOneDiff) diff Diff(database.sampleTableOneDiff)

      Then("the number of differences found should be 0")
      diff.columns.size shouldEqual 0
    }

    Scenario("The tables diffed have a valid non-primary part difference") {
      Given("A valid Cassandra table schema is used")

      When("A table is diffed against a table with one more string column")
      val diff = Diff(database.sampleTableOneDiff) diff Diff(database.sampleTable)

      Then("The total number of differences found should be 1")
      diff.columns.size shouldEqual 1

      And("The name of the column found to different should be the right one")
      diff.columns.head.name shouldEqual database.sampleTableOneDiff.name2.name
    }

    Scenario("The table on the left hand side of the diff has one more primary key") {
      Given("A valid Cassandra table schema is used")

      When("A table is diffed against a table with one more primary uuid column")
      val diff = Diff(database.sampleTablePrimaryDiff) diff Diff(database.sampleTableOneDiff)

      Then("The total number of differences found should be 1")
      diff.columns.size shouldEqual 1

      And("A primary key part should be found in the diff")
      diff.hasPrimaryPart shouldEqual true

      And("Calling the migrations method should throw an error")

      val migrations = diff.migrations

      migrations shouldBe invalid
      migrations.invalidValue.head shouldBe an [InvalidAddition]

    }

    Scenario("The table on the right adds new indexes") {
      Given("A table with no secondary indexes is used")

      When("A table is diffed against a table with a new secondary index column")
      val diff = Diff(database.missingColumnTableAdded) diff Diff(database.missingColumnTable)

      val migration = database.automigrate().value.diffs

      whenReady(database.automigrate().value.diffs.sequence()) { res =>
        //res.forall(_.wasApplied()) shouldEqual true
      }

    }
  }
}
