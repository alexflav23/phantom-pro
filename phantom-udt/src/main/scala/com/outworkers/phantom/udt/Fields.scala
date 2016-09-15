package com.outworkers.phantom.udt

/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
import java.net.InetAddress
import java.util.{Date, UUID}

import org.joda.time.DateTime
import com.datastax.driver.core.{LocalDate, UDTValue}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.primitives.Primitive

import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.util.{DynamicVariable, Try}

sealed trait Extractor[T] {
  def apply(name: String, udt: UDTValue): Try[T]
}

object Extractor {
  def apply[T : Extractor]: Extractor[T] = implicitly[Extractor[T]]
}

private[udt] trait Extractors {
  implicit object BooleanExtractor extends Extractor[Boolean] {
    def apply(name: String, udt: UDTValue): Try[Boolean] = Try(udt.getBool(name))
  }

  implicit object StringExtractor extends Extractor[String] {
    def apply(name: String, udt: UDTValue): Try[String] = Try(udt.getString(name))
  }

  implicit object InetExtractor extends Extractor[InetAddress] {
    def apply(name: String, udt: UDTValue): Try[InetAddress] = Try(udt.getInet(name))
  }

  implicit object IntExtractor extends Extractor[Int] {
    def apply(name: String, udt: UDTValue): Try[Int] = Try(udt.getInt(name))
  }

  implicit object BigDecimalExtractor extends Extractor[BigDecimal] {
    def apply(name: String, udt: UDTValue): Try[BigDecimal] = Try(udt.getDecimal(name)).map(BigDecimal(_))
  }

  implicit object ShortExtractor extends Extractor[Short] {
    def apply(name: String, udt: UDTValue): Try[Short] = Try(udt.getShort(name))
  }
}


