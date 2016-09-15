package com.outworkers.phantom.udt

import java.util.UUID

import com.outworkers.util.testing.{Sample, _}

trait Samplers {

  implicit object TestGenerator extends Sample[Test] {
    override def sample: Test = Test(
      gen[Int],
      gen[String]
    )
  }

  implicit object Test2Generator extends Sample[Test2] {
    override def sample: Test2 = Test2(
      gen[Int],
      gen[String],
      gen[BigDecimal],
      5
    )
  }

  implicit object TestRecordSampler extends Sample[TestRecord] {
    override def sample: TestRecord = {
      TestRecord(
        gen[UUID],
        gen[Test],
        gen[Test2]
      )
    }
  }

}
