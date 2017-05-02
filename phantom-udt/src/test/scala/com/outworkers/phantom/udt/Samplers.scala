/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.udt

import java.util.UUID

import com.outworkers.phantom.udt.tables._
import com.outworkers.util.samplers._

trait Samplers {

  implicit object TestRecordSampler extends Sample[TestRecord] {
    override def sample: TestRecord = {
      TestRecord(
        gen[UUID],
        gen[Test],
        gen[Test2],
        gen[ListCollectionUdt]
      )
    }
  }

  implicit object LocationGenerator extends Sample[Location] {
    override def sample: Location = Location(
      gen[Long],
      gen[Long]
    )
  }

  implicit object AddressSampler extends Sample[Address] {
    override def sample: Address = Address(
      gen[String],
      gen[Location],
      gen[ShortString].value
    )
  }

  implicit object CollectionUdtSampler extends Sample[CollectionUdt] {
    override def sample: CollectionUdt = CollectionUdt(
      gen[String],
      genList[Address]()
    )
  }

  implicit object CollectionSetUdtSampler extends Sample[CollectionSetUdt] {
    override def sample: CollectionSetUdt = CollectionSetUdt(
      gen[String],
      genList[Address]().toSet
    )
  }

  implicit object NestedRecordSampler extends Sample[NestedRecord] {
    override def sample: NestedRecord = NestedRecord(
      gen[UUID],
      gen[EmailAddress].value,
      gen[Address],
      gen[CollectionUdt]
    )
  }

  implicit object NestedSetRecordSampler extends Sample[NestedSetRecord] {
    override def sample: NestedSetRecord = NestedSetRecord(
      gen[UUID],
      gen[EmailAddress].value,
      gen[Address],
      gen[CollectionSetUdt]
    )
  }

  implicit object NestedMapsSampler extends Sample[NestedMaps] {
    override def sample: NestedMaps = NestedMaps(
      gen[ShortString].value,
      genMap[String, Address](5)
    )
  }

  implicit object NestedMapsRecordSampler extends Sample[NestedMapRecord] {
    override def sample: NestedMapRecord = NestedMapRecord(
      gen[UUID],
      genList[String](),
      gen[NestedMaps]
    )
  }

  implicit object PersonSampler extends Sample[Person] {
    override def sample: Person = Person(
      gen[UUID],
      genList[Address](),
      genList[Address]().toSet
    )
  }


}
