package com.outworkers.phantom.udt.builder

import com.outworkers.phantom.udt.{SchemaGenerator, Test2}
import org.scalatest.{FlatSpec, Matchers}

class SchemaDerivationTest extends FlatSpec with Matchers {

  it should "derive the type of a schema from a class instance" in {
    val test = Test2(1, "hello")

    val fields = SchemaGenerator.classAccessors[Test2]
    val types = SchemaGenerator.infer(test)

    Console.println(fields)
    Console.println(types)

    val inferred = (fields zip types) map {
      case (name, tp) => s"$name $tp"
    } mkString ", "

    info(s"CREATE TYPE IF NOT EXISTS bla.test2 $inferred")
  }
}
