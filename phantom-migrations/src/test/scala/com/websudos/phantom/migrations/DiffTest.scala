package com.websudos.phantom.migrations

import com.websudos.phantom.migrations.tables.{SampleTablePrimaryDiff, SampleTableOneDiff, SampleTable}
import org.scalatest.{Matchers, BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

class DiffTest extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with Matchers {

  info("As a developer")
  info("I want to automatically resolve schema discrepancies")
  info("Between existing nodes")

  feature("The column differ should compute the differences between two tables") {
    scenario("The table is being diffed against itself") {
      Given("A valid Cassandra table schema is used")

      When("A table is diffed against itself")
      val diff = Diff(SampleTable) diff Diff(SampleTable)

      Then("the number of differences found should be 0")
      diff.columns.size shouldEqual 0
    }

    scenario("The tables diffed have a valid non-primary part difference") {
      Given("A valid Cassandra table schema is used")

      When("A table is diffed against a table with one more string column")
      val diff = Diff(SampleTableOneDiff) diff Diff(SampleTable)

      Then("The total number of differences found should be 1")
      diff.columns.size shouldEqual 1

      And("The name of the column found to different should be the right one")
      diff.columns.head.name shouldEqual SampleTableOneDiff.name2.name
    }

    scenario("The table on the left hand side of the diff has one more primary key") {
      Given("A valid Cassandra table schema is used")

      When("A table is diffed against a table with one more primary uuid column")
      val diff = Diff(SampleTablePrimaryDiff) diff Diff(SampleTable)

      Then("The total number of differences found should be 1")
      diff.columns.size shouldEqual 1

      And("A primary key part should be found in the diff")
      diff.hasPrimaryPart shouldEqual true

      And("Calling the migrations method should throw an error")

      intercept[Exception] {
        diff.migrations()
      }
    }
  }
}
