package com.github.geirolz.advxml.traverse

import com.github.geirolz.advxml.convert.Validation.ValidationRes
import org.scalatest.FeatureSpec

import scala.language.postfixOps

/**
  * Advxml
  * Created by geirolad on 19/06/2019.
  *
  * @author geirolad
  */
class XmlTraverserSyntaxTest extends FeatureSpec {

  import com.github.geirolz.advxml.implicits.traverser._
  import com.github.geirolz.advxml.implicits.validation._

  feature("XmlTraverseSyntaxTest: Read Attributes") {
    scenario("Read optional attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name: ValidationRes[Option[String]] = xml \ "Employee" \@? "Name"
      val age = xml \ "Employee" \@? "Age"

      assert(name.toFlatOption.isDefined)
      assert(name.toFlatOption.get == "David")
      assert(age.toFlatOption.isEmpty)
    }

    scenario("Read required attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name = xml \ "Employee" \@! "Name"
      val age = xml \ "Employee" \@! "Age"

      assert(name.isValid)
      assert(name.toEither.right.get == "David")
      assert(age.isInvalid)
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

      assert(cars.toFlatOption.isDefined)
      assert(cars.toFlatOption.get.length == 1)
      assert(works.toFlatOption.isEmpty)
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

      assert(cars.isValid)
      assert(cars.toEither.right.get.length == 1)
      assert(works.isInvalid)
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

      assert(cars.toFlatOption.isDefined)
      assert(cars.toFlatOption.get.length == 1)
      assert(works.toFlatOption.isEmpty)
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

      assert(cars.isValid)
      assert(cars.toEither.right.get.length == 1)
      assert(works.isInvalid)
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

      assert(note.isValid)
      assert(note.toEither.right.get.trim == noteData)
      assert(works.isInvalid)
    }
  }
}
