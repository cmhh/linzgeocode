package org.cmhh.linzgeocode

import collection.mutable.Stack
import org.scalatest._
import org.scalatest.TryValues._
import flatspec._
import matchers._

class ExampleSpec extends AnyFlatSpec with should.Matchers {

  "An address string" should "parse correctly" in {
    val address = "1/45A Memorial Avenue, Ilam, Christchurch 8053"
    val parsed = parse(address)
    
    parsed.isSuccess should be (true)  

    val parts = parsed.success.value

    parts.unitValue should be (Some("1"))
    parts.addressNumberSuffix should be (Some("A"))
    parts.addressNumber should be (Some(45))
    parts.roadName should be (Some("Memorial"))
    parts.roadTypeName should be (Some("Avenue"))
    parts.suburbLocality should be (Some("Ilam"))
    parts.townCity should be (Some("Christchurch"))
    parts.postcode should be (Some("8053"))
  }

  it should "parse correctly if postcode is missing" in {
    val address = "1/45A Memorial Avenue, Ilam, Christchurch"
    val parsed = parse(address)
    
    parsed.isSuccess should be (true)  

    val parts = parsed.success.value

    parts.unitValue should be (Some("1"))
    parts.addressNumberSuffix should be (Some("A"))
    parts.addressNumber should be (Some(45))
    parts.roadName should be (Some("Memorial"))
    parts.roadTypeName should be (Some("Avenue"))
    parts.suburbLocality should be (Some("Ilam"))
    parts.townCity should be (Some("Christchurch"))
    parts.postcode should be (None)
  }

  it should "parse correctly if town / city is missing" in {
    val address = "1/45A Memorial Avenue, Ilam"
    val parsed = parse(address)
    
    parsed.isSuccess should be (true)  

    val parts = parsed.success.value

    parts.unitValue should be (Some("1"))
    parts.addressNumberSuffix should be (Some("A"))
    parts.addressNumber should be (Some(45))
    parts.roadName should be (Some("Memorial"))
    parts.roadTypeName should be (Some("Avenue"))
    parts.suburbLocality should be (Some("Ilam"))
    parts.townCity should be (None)
    parts.postcode should be (None)
  }

  it should "parse correcly if both suburb and town are missing" in {
    val address = "1/45A Memorial Avenue"
    val parsed = parse(address)
    
    parsed.isSuccess should be (true)  

    val parts = parsed.success.value

    parts.unitValue should be (Some("1"))
    parts.addressNumberSuffix should be (Some("A"))
    parts.addressNumber should be (Some(45))
    parts.roadName should be (Some("Memorial"))
    parts.roadTypeName should be (Some("Avenue"))
    parts.suburbLocality should be (None)
    parts.townCity should be (None)
    parts.postcode should be (None)
  }

  /*
  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    } 
  }
  */
}