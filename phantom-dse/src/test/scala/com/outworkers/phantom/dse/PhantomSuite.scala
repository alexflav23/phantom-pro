package com.outworkers.phantom.dse

/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

import java.util.concurrent.TimeUnit

import com.datastax.driver.core.VersionNumber
import com.outworkers.phantom.database.DatabaseProvider
import com.outworkers.util.samplers._
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalatest._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait PhantomBaseSuite extends Suite with Matchers
  with BeforeAndAfterAll
  with ScalaFutures
  with OptionValues {

  protected[this] val defaultScalaTimeoutSeconds = 3

  private[this] val defaultScalaInterval = 50L

  implicit val defaultScalaTimeout = scala.concurrent.duration.Duration(defaultScalaTimeoutSeconds, TimeUnit.SECONDS)

  private[this] val defaultTimeoutSpan = Span(defaultScalaTimeoutSeconds, Seconds)

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(defaultTimeoutSpan)

  implicit object JodaTimeSampler extends Sample[DateTime] {
    override def sample: DateTime = DateTime.now(DateTimeZone.UTC)
  }

  implicit object JodaLocalDateSampler extends Sample[LocalDate] {
    override def sample: LocalDate = LocalDate.now(DateTimeZone.UTC)
  }

  override implicit val patienceConfig = PatienceConfig(
    timeout = defaultTimeoutSpan,
    interval = Span(defaultScalaInterval, Millis)
  )

  implicit class CqlConverter[T](val obj: T) {
    def asCql()(implicit primitive: com.outworkers.phantom.builder.primitives.Primitive[T]): String = {
      primitive.asCql(obj)
    }
  }

  implicit class BlockHelper[T](val f: Future[T]) {
    def block(timeout: Duration): T = Await.result(f, timeout)
  }
}

trait PhantomSuite extends FlatSpec with PhantomBaseSuite

trait PhantomFreeSuite extends FreeSpec with PhantomBaseSuite
