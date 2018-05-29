/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt.suites

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.Date

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.domain.OptionalUdt
import com.outworkers.phantom.udt.tables.OptionUDTRecord
import com.outworkers.util.samplers._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{FlatSpec, Inside}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.concurrent.Future

class OptionalUdtsTest extends FlatSpec with PhantomTest with GeneratorDrivenPropertyChecks with Inside {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 50)
  }

  val optionalUdtGen: Gen[OptionalUdt] = for {
    uuid <- Gen.uuid
    optionalDate <- Gen.option(Sample.generator[Date])
    optionalBigDecimal <- Gen.option(Sample.generator[BigDecimal])
    optionalBigInt <- Gen.option(Sample.generator[BigInt])
    optionalBoolean <- Gen.option(Arbitrary.arbBool.arbitrary)
    optionalUUID <- Gen.option(Gen.uuid)
    optionalDouble <- Gen.option(Sample.generator[Double])
    optionalInt <- Gen.option(Sample.generator[Int])
    optionalShort <- Gen.option(Sample.generator[Short])
    optionalFloat <- Gen.option(Sample.generator[Float])
    optionalLong <- Gen.option(Sample.generator[Long])
    optionalString <- Gen.option(Gen.alphaNumStr)
    optionalByteBuffer <- Gen.option(Sample.generator[ByteBuffer])
    optionalInet <- Gen.option(Sample.generator[InetAddress])
  } yield OptionalUdt(
    uuid,
    optionalDate,
    optionalBigDecimal,
    optionalBigInt,
    optionalBoolean,
    optionalUUID,
    optionalDouble,
    optionalInt,
    optionalShort,
    optionalFloat,
    optionalLong,
    optionalString,
    optionalByteBuffer,
    optionalInet
  )

  val tpGen = for {
    str <- Gen.alphaNumStr
    opt <- optionalUdtGen
  } yield str -> opt

  val optionalRecordGen = for {
    id <- Gen.uuid
    optUdt <- optionalUdtGen
    optList <- Gen.listOfN(defaultGeneration, optionalUdtGen)
    optSet <- Gen.listOfN(defaultGeneration, optionalUdtGen).map(_.toSet)
    map <- Gen.mapOfN(defaultGeneration, tpGen)
  } yield OptionUDTRecord(id, optUdt, optList, optSet, map)

  it should "insert the value of an optional UDT record" in {
    val sample = gen[OptionUDTRecord]

    val chain = for {
      store <- database.optionalUdts.storeRecord(sample)
      res <- database.optionalUdts.findById(sample.id)
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "allow updating the value of an optional UDT record" in {
    val sample = gen[OptionUDTRecord]
    val updated = gen[OptionalUdt]

    val chain = for {
      store <- database.optionalUdts.storeRecord(sample)
      res <- database.optionalUdts.findById(sample.id)
      update <- database.optionalUdts
        .update.where(_.id eqs sample.id)
        .modify(_.opt setTo updated)
        .future()
      res2 <- database.optionalUdts.findById(sample.id)
    } yield (res, res2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value shouldEqual sample

      afterUpdate shouldBe defined
      afterUpdate.value.opt shouldEqual updated
    }
  }

  it should "store and retrieve a series of UDT records" in {
    val samples = genList[OptionUDTRecord]()

    val chain = for {
      store <- database.optionalUdts.storeRecords(samples)
      retrieve <- Future.sequence(samples.map(item => database.optionalUdts.findById(item.id)))
    } yield retrieve

    whenReady(chain) { results =>
      results.forall(_.isDefined) shouldEqual true
      results.flatten should contain theSameElementsAs samples
    }
  }


  it should "store and retrieve a large generation size of optionally defined primitives inside UDTs" in {
    forAll(optionalRecordGen) { record =>
      val chain = for {
        store <- database.optionalUdts.storeRecord(record)
        res <- database.optionalUdts.findById(record.id)
      } yield res

      whenReady(chain) { res =>
        res shouldBe defined
        res.value.id shouldEqual record.id
        inside(res.value.opt) { case OptionalUdt(
          id,
          optionalDate,
          optionalBigDecimal,
          optionalBigInt,
          optionalBoolean,
          optionalUUID,
          optionalDouble,
          optionalInt,
          optionalShort,
          optionalFloat,
          optionalLong,
          optionalString,
          optionalByteBuffer,
          optionalInet
        ) =>
          id shouldEqual record.opt.id
          optionalDate shouldEqual record.opt.optionalDate
          optionalBigDecimal shouldEqual record.opt.optionalBigDecimal
          optionalBigInt shouldEqual record.opt.optionalBigInt
          optionalBoolean shouldEqual record.opt.optionalBoolean
          optionalUUID shouldEqual record.opt.optionalUUID
          optionalDouble shouldEqual record.opt.optionalDouble
          optionalInt shouldEqual record.opt.optionalInt
          optionalShort shouldEqual record.opt.optionalShort
          optionalFloat shouldEqual record.opt.optionalFloat
          optionalLong shouldEqual record.opt.optionalLong
          optionalString shouldEqual record.opt.optionalString
          optionalByteBuffer shouldEqual record.opt.optionalByteBuffer
          optionalInet shouldEqual record.opt.optionalInet
        }

        res.value.col should contain theSameElementsAs record.col
        res.value.colSet should contain theSameElementsAs record.colSet
        //res.value.map shouldEqual record.map
      }
    }
  }
}
