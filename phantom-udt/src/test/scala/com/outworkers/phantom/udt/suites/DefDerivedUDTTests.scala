package com.outworkers.phantom.udt.suites

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.tables.DerivedEncoderRecord
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class DefDerivedUDTTests extends FlatSpec with PhantomTest with ScalaCheckDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 25)
  }

  it should "store and retrieve a UDT primitive generates with def macros" in {
    val sample = gen[DerivedEncoderRecord]

    val chain = for {
      store <- database.derivedUdts.storeRecord(sample)
      find <- database.derivedUdts.findById(sample.id)
    } yield find

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }


  it should "store and retrieve a sequence of UDT primitives generated with def macros" in {
    forAll(Sample.generator[DerivedEncoderRecord]) { sample =>
      val chain = for {
        store <- database.derivedUdts.storeRecord(sample)
        find <- database.derivedUdts.findById(sample.id)
      } yield find

      whenReady(chain) { res =>
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  }
}
