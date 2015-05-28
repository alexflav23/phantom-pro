package com.websudos.phantom.migrations

import com.websudos.phantom.migrations.tables.SampleTable
import org.scalatest.{Matchers, BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

class DiffTest extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll with Matchers {

  info("As a developer")
  info("I want to automatically resolve schema discrepancies")
  info("Between existing nodes")

  feature("The column differ should compute the differences between two tables") {
    scenario("The table is being diffed against itself") {
      Given("A valid Cassandra table schema is used")

      When("A table is diffed against itself")
      val diff = Migration(SampleTable, SampleTable)

      diff.queryList.size shouldEqual 0
    }
  }
}
