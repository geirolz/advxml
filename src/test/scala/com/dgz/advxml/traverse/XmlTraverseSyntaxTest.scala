package com.dgz.advxml.traverse

import org.scalatest.FeatureSpec

import scala.language.postfixOps

/**
  * Adxml
  * Created by geirolad on 19/06/2019.
  *
  * @author geirolad
  */
class XmlTraverseSyntaxTest extends FeatureSpec {

  import XmlTraverseSyntax._

  feature("XmlTraverseSyntaxTest: Read Attributes") {
    scenario("Read optional attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name = xml \ "Employee" \@? "Name"
      val age = xml \ "Employee" \@? "Age"

      assert(name.isDefined)
      assert(name.get == "David")
      assert(age.isEmpty)
    }

    scenario("Read required attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name = xml \ "Employee" \@! "Name"
      val age = xml \ "Employee" \@! "Age"

      assert(name.isSuccess)
      assert(name.get == "David")
      assert(age.isFailure)
    }
  }

  feature("XmlTraverseSyntaxTest: Read Immediate Nodes") {
    scenario("Read optional immediate nodes") {
      val xml =
        <Employers>
          <Employee Name="David">
            <Cars>
              <Car Model="Fiat"/>
            </Cars>
          </Employee>
        </Employers>

      val cars = xml \ "Employee" \? "Cars"
      val works = xml \ "Employee" \? "Works"

      assert(cars.isDefined)
      assert(cars.get.length == 1)
      assert(works.isEmpty)
    }

    scenario("Read required immediate nodes") {
      val xml =
        <Employers>
          <Employee Name="David">
            <Cars>
              <Car Model="Fiat"/>
            </Cars>
          </Employee>
        </Employers>

      val cars = xml \ "Employee" \! "Cars"
      val works = xml \ "Employee" \! "Works"

      assert(cars.isSuccess)
      assert(cars.get.length == 1)
      assert(works.isFailure)
    }
  }

  feature("XmlTraverseSyntaxTest: Read nested Nodes") {
    scenario("Read optional nested nodes") {
      val xml =
        <Employers>
          <Employee Name="David">
            <Cars>
              <Car Model="Fiat"/>
            </Cars>
          </Employee>
        </Employers>

      val cars = xml \\? "Cars"
      val works = xml \\? "Works"

      assert(cars.isDefined)
      assert(cars.get.length == 1)
      assert(works.isEmpty)
    }

    scenario("Read required nested nodes") {
      val xml =
        <Employers>
          <Employee Name="David">
            <Cars>
              <Car Model="Fiat"/>
            </Cars>
          </Employee>
        </Employers>

      val cars = xml \\! "Cars"
      val works = xml \\! "Works"

      assert(cars.isSuccess)
      assert(cars.get.length == 1)
      assert(works.isFailure)
    }
  }

  feature("XmlTraverseSyntaxTest: Read content") {
    scenario("Read required content") {
      val noteData = "This is a test"
      val xml =
        <Employers>
          <Employee Name="David">
            <Note>
              {noteData}
            </Note>
          </Employee>
        </Employers>

      val note = xml \ "Employee" \ "Note" content
      val works = xml \ "Employee" \ "Works" content

      assert(note.isSuccess)
      assert(note.get.trim == noteData)
      assert(works.isFailure)
    }
  }
}
