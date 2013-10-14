package edu.washington.knowitall.entitymentionfinder.eval

case class NamedEntityCollection(
	val organizations: List[String],
      val locations: List[String],
      val people: List[String])