/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.deletions

import cats.scalatest.{ValidatedMatchers, ValidatedValues}
import com.outworkers.phantom.migrations.diffs.{Diff, DiffConfig}
import com.outworkers.phantom.migrations.utils.MigrationSuite
import org.scalatest.{FeatureSpec, GivenWhenThen}
import com.outworkers.phantom.dsl._

class DropCaseSensitiveColumnsTest extends FeatureSpec with GivenWhenThen with MigrationSuite with ValidatedValues {

  implicit val diffConfig: DiffConfig = {
    DiffConfig(
      allowNonOptional = true,
      allowSecondaryOverwrites = false
    )
  }

  override def beforeAll(): Unit = {
    super.beforeAll
    db.create()
  }

  feature("The column differ should compute the differences between two tables") {
    scenario("A single camel quoted column is dropped") {

      Given("A valid Cassandra table schema is used, and the Naming strategy is case sensitive")

      When("A table is diffed against a table with the case sensitive column being removed")
      //val diff = Diff(database.dropQuotedTable) diff Diff(database.droppedQuotedTable)
      val migrations = database.dropQuotedTable.automigrate()

      Console.println(database.dropQuotedTable.alter().drop(_.name))

      Then("The total number of differences found should be 1")
      //diff.columns.size shouldEqual 1
      //Console.println(diff.columns)

      Console.println(s"Found ${migrations.value.queries.size} migration queries required")
      Console.println(migrations.value.queries.mkString("\n"))

      And("A primary key part should be found in the diff")

      And("Calling the migrations method should throw an error")
    }
  }
}
