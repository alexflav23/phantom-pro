/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.udt.suites

import java.util.concurrent.TimeUnit

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.TestDbProvider
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, Suite}

import scala.concurrent.duration.FiniteDuration

trait PhantomTest extends Suite
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll
  with OptionValues
  with Samplers
  with TestDbProvider {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.create()
  }

  protected[this] val defaultScalaTimeoutSeconds = 25

  private[this] val defaultScalaInterval = 50L

  implicit val defaultScalaTimeout: FiniteDuration = scala.concurrent.duration.Duration(
    defaultScalaTimeoutSeconds,
    TimeUnit.SECONDS
  )

  private[this] val defaultTimeoutSpan = Span(defaultScalaTimeoutSeconds, Seconds)

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(defaultTimeoutSpan)

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = defaultTimeoutSpan,
    interval = Span(defaultScalaInterval, Millis)
  )

  implicit class CqlConverter[T](val obj: T) {
    def asCql()(implicit primitive: com.outworkers.phantom.builder.primitives.Primitive[T]): String = {
      primitive.asCql(obj)
    }
  }
}
