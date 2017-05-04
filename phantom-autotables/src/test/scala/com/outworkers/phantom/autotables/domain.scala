/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.autotables

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.dsl.UUID

final case class Location(
  country: String,
  city: String,
  formatted_address: String,
  google_places_id: Option[String]
)

object Location {
  implicit val location: Primitive[Location] = Primitive.tupled[Location].derive
}

case class User(
  id: UUID,
  email: String,
  location: Location
)