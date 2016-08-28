package com.outworkers.phantom.udt

private[phantom] object Helper {
  import scala.reflect.runtime.universe._

  def classAccessors[T : TypeTag]: List[String] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m.name.decodedName.toString
  }.toList.reverse

}
