package com.github.geirolz.advxml.traverse

import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
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

  feature("XmlTraverseSyntaxTest: Read Attributes") {
    scenario("Read optional attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name: ValidatedRes[Option[String]] = xml \ "Employee" \@? "Name"
      val age = xml \ "Employee" \@? "Age"

      assert(name.exists(_.isDefined))
      assert(name.exists(_.get == "David"))
      assert(age.exists(_.isEmpty))
    }

    scenario("Read required attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name = xml \ "Employee" \@! "Name"
      val age = xml \ "Employee" \@! "Age"

      assert(name.isValid)
      assert(name.toEither.exists(_ == "David"))
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

      assert(cars.exists(_.isDefined))
      assert(cars.exists(_.get.length == 1))
      assert(works.exists(_.isEmpty))
    }

    scenario("Read optional immediate nodes waterfall") {
      val xml =
        <Employers>
          <Employee Name="David">
            <Cars>
              <Car Model="Fiat"/>
            </Cars>
          </Employee>
        </Employers>

      val cars = xml \? "Employee" \? "Cars"
      val works = xml \? "Employee" \? "Works"
      val data = xml \? "Other" \? "Data"

      assert(cars.exists(_.isDefined))
      assert(cars.exists(_.get.length == 1))
      assert(works.exists(_.isEmpty))
      assert(data.exists(_.isEmpty))
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
      assert(cars.toEither.exists(_.length == 1))
      assert(works.isInvalid)
    }

    scenario("Read required immediate nodes waterfall") {
      val xml =
        <Employers>
          <Employee Name="David">
            <Cars>
              <Car Model="Fiat"/>
            </Cars>
          </Employee>
        </Employers>

      val cars = xml \! "Employee" \! "Cars"
      val works = xml \! "Employee" \! "Works"
      val data = xml \! "Other" \! "Data"

      assert(cars.isValid)
      assert(cars.toEither.exists(_.length == 1))

      //missing Nodes
      assert(works.isInvalid)
      assert(data.isInvalid)
      assert(data.toEither.swap.getOrElse(null).size == 1)
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

      assert(cars.exists(_.isDefined))
      assert(cars.exists(_.get.length == 1))
      assert(works.exists(_.isEmpty))
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
      assert(cars.toEither.exists(_.length == 1))
      assert(works.isInvalid)
    }
  }

  feature("XmlTraverseSyntaxTest: Read content") {
    scenario("Read optional text") {
      val noteData = "This is a test"
      val xml =
        <Employers>
          <Employee Name="David">
            <Note>
              {noteData}
            </Note>
          </Employee>
        </Employers>

      val note = xml \ "Employee" \ "Note" ?
      val works = xml \ "Employee" \ "Works" ?

      assert(note.isValid)
      assert(note.exists(_.get.trim == noteData))
      assert(works.isValid)
      assert(works.exists(_.isEmpty))
    }

    scenario("Read optional text waterfall") {
      val noteData = "This is a test"
      val xml =
        <Employers>
          <Employee Name="David">
            <Note>
              {noteData}
            </Note>
          </Employee>
        </Employers>

      val note = xml \? "Employee" \? "Note" ?
      val works = xml \? "Employee" \? "Works" ?

      assert(note.isValid)
      assert(note.exists(_.get.trim == noteData))
      assert(works.isValid)
      assert(works.exists(_.isEmpty))
    }

    scenario("Read required text") {
      val noteData = "This is a test"
      val xml =
        <Employers>
          <Employee Name="David">
            <Note>
              {noteData}
            </Note>
          </Employee>
        </Employers>

      val note: ValidatedRes[String] = xml \ "Employee" \ "Note" !
      val works = xml \ "Employee" \ "Works" !

      assert(note.isValid)
      assert(note.toEither.exists(_.trim == noteData))
      assert(works.isInvalid)
    }

    scenario("Read required text waterfall") {
      val noteData = "This is a test"
      val xml =
        <Employers>
          <Employee Name="David">
            <Note>
              {noteData}
            </Note>
          </Employee>
        </Employers>

      val note: ValidatedRes[String] = xml \ "Employee" \ "Note" !
      val works = xml \! "Employee" \! "Works" !

      assert(note.isValid)
      assert(note.toEither.exists(_.trim == noteData))
      assert(works.isInvalid)
    }
  }
}
