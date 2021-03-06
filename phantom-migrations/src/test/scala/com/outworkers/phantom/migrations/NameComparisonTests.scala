package com.outworkers.phantom.migrations

import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.migrations.diffs.{ColumnDiff, Comparison}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class NameComparisonTests extends AnyFlatSpec with Matchers {

  it should "correctly compare identical column diffs" in {
    val c1 = ColumnDiff(
      "columnName",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    Comparison.NameComparison(c1, c1) shouldEqual true
  }

  it should "correctly compare column diffs with different name cases" in {
    val c1 = ColumnDiff(
      "columnName",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    val c2 = ColumnDiff(
      "COLuMNName",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    Comparison.NameComparison(c1, c2) shouldEqual true
  }


  it should "correctly compare column diffs with different name case sensitivity" in {
    val c1 = ColumnDiff(
      "columnName",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    val c2 = ColumnDiff(
      "'COLuMNName'",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    Comparison.NameComparison(c1, c2) shouldEqual true
  }

  it should "correctly compare column diffs with reverse different name case sensitivity" in {
    val c1 = ColumnDiff(
      "'columnName'",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    val c2 = ColumnDiff(
      "'COLuMNName'",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    Comparison.NameComparison(c1, c2) shouldEqual true
  }

  it should "correctly compare column diffs with reverse different name case sensitivity and double quotes" in {
    val c1 = ColumnDiff(
      "'columnName'",
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    val c2 = ColumnDiff(
      "\"'COLuMNName'\""  ,
      CQLSyntax.Types.TimeUUID,
      isOptional = false,
      isPrimary = false,
      isSecondary = false,
      isStatic = false
    )

    Comparison.NameComparison(c1, c2) shouldEqual true
  }
}
