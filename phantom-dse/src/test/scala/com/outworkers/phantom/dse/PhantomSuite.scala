/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.dse

import java.util.concurrent.TimeUnit

import com.outworkers.util.samplers._
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalatest._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.duration.{Duration => ScalaDuration, FiniteDuration}
import scala.concurrent.{Await, Future}

trait PhantomBaseSuite extends Suite with Matchers
  with BeforeAndAfterAll
  with ScalaFutures
  with OptionValues {

  protected[this] val defaultScalaTimeoutSeconds = 3

  private[this] val defaultScalaInterval = 50L

  implicit val defaultScalaTimeout: FiniteDuration = ScalaDuration(defaultScalaTimeoutSeconds, TimeUnit.SECONDS)

  private[this] val defaultTimeoutSpan = Span(defaultScalaTimeoutSeconds, Seconds)

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(defaultTimeoutSpan)

  implicit object JodaTimeSampler extends Sample[DateTime] {
    override def sample: DateTime = DateTime.now(DateTimeZone.UTC)
  }

  implicit object JodaLocalDateSampler extends Sample[LocalDate] {
    override def sample: LocalDate = LocalDate.now(DateTimeZone.UTC)
  }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = defaultTimeoutSpan,
    interval = Span(defaultScalaInterval, Millis)
  )

  implicit class CqlConverter[T](val obj: T) {
    def asCql()(implicit primitive: com.outworkers.phantom.builder.primitives.Primitive[T]): String = {
      primitive.asCql(obj)
    }
  }

  implicit class BlockHelper[T](val f: Future[T]) {
    def block(timeout: ScalaDuration): T = Await.result(f, timeout)
  }
}

trait PhantomSuite extends FlatSpec with PhantomBaseSuite

trait PhantomFreeSuite extends FreeSpec with PhantomBaseSuite
