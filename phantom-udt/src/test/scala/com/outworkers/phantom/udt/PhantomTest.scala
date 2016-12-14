package com.outworkers.phantom.udt

import java.util.concurrent.TimeUnit

import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, Suite}
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import com.outworkers.phantom.dsl.context

trait PhantomTest extends Suite
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll
  with TestDatabase.connector.Connector
  with OptionValues
  with TestDbProvider
  with Samplers {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.create()
  }

  protected[this] val defaultScalaTimeoutSeconds = 25

  private[this] val defaultScalaInterval = 50L

  implicit val defaultScalaTimeout = scala.concurrent.duration.Duration(defaultScalaTimeoutSeconds, TimeUnit.SECONDS)

  private[this] val defaultTimeoutSpan = Span(defaultScalaTimeoutSeconds, Seconds)

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(defaultTimeoutSpan)

  override implicit val patienceConfig = PatienceConfig(
    timeout = defaultTimeoutSpan,
    interval = Span(defaultScalaInterval, Millis)
  )

  implicit class CqlConverter[T](val obj: T) {
    def asCql()(implicit primitive: com.outworkers.phantom.builder.primitives.Primitive[T]): String = {
      primitive.asCql(obj)
    }
  }
}
