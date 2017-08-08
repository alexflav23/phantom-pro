package com.outworkers.phantom.udt.suites

import com.outworkers.phantom.dsl.context
import com.outworkers.phantom.udt.tables.{ParkingCharge, Policy}
import org.scalatest.FlatSpec
import com.outworkers.util.samplers._
import org.scalatest.prop.Configuration.PropertyCheckConfiguration
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class AuthorizerPolicyTests extends FlatSpec with PhantomTest with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 50)
  }

  it should "store and retrieve a policy" in {
    val sample = gen[Policy]

    val chain = for {
      store <- database.policies.storeRecord(sample)
      res <- database.policies.findById(sample.uid)
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "update the value of a stored policy inside the database" in {
    val policy = gen[Policy]
    val charges = genSet[ParkingCharge]()

    val chain = for {
      store <- database.policies.storeRecord(policy)
      res <- database.policies.findById(policy.uid)
      update <- database.policies.update.where(_.uid eqs policy.uid).modify(_.parkingCharge addAll charges).future()
      res2 <- database.policies.findById(policy.uid)
    } yield (res, res2)

    whenReady(chain) { case (res, res2) =>
      res shouldBe defined
      res.value shouldEqual policy

      res2 shouldBe defined
      res2.value.parkingCharge should contain theSameElementsAs (policy.parkingCharge ++ charges)
    }
  }

  it should "store and retrieve a sequence of policy records" in {
    forAll(Sample.generator[Policy]) { sample =>
      val chain = for {
        store <- database.policies.storeRecord(sample)
        res <- database.policies.findById(sample.uid)
      } yield res

      whenReady(chain) { res =>
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  }
}
