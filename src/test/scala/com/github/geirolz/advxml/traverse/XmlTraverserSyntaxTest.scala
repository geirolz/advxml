package com.github.geirolz.advxml.traverse

import org.scalatest.FeatureSpec

import scala.language.postfixOps
import scala.util.Try
import scala.xml.NodeSeq

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

      val name: Option[String] = xml \ "Employee" \@? "Name"
      val age: Option[String] = xml \ "Employee" \@? "Age"

      assert(name.isDefined)
      assert(name.get == "David")
      assert(age.isEmpty)
    }

    scenario("Read required attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name: Try[String] = xml \ "Employee" \@! "Name"
      val age: Try[String] = xml \ "Employee" \@! "Age"

      assert(name.isSuccess)
      assert(name.toEither.exists(_ == "David"))
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

      val cars: Option[NodeSeq] = xml \ "Employee" \? "Cars"
      val works: Option[NodeSeq] = xml \ "Employee" \? "Works"

      assert(cars.isDefined)
      assert(cars.get.length == 1)
      assert(works.isEmpty)
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

      val cars: Option[NodeSeq] = xml \? "Employee" \? "Cars"
      val works: Option[NodeSeq] = xml \? "Employee" \? "Works"
      val data: Option[NodeSeq] = xml \? "Other" \? "Data"

      assert(cars.isDefined)
      assert(cars.get.length == 1)
      assert(works.isEmpty)
      assert(data.isEmpty)
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

      val cars: Try[NodeSeq] = xml \ "Employee" \! "Cars"
      val works: Try[NodeSeq] = xml \ "Employee" \! "Works"

      assert(cars.isSuccess)
      assert(cars.toEither.exists(_.length == 1))
      assert(works.isFailure)
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

      val cars: Try[NodeSeq] = xml \! "Employee" \! "Cars"
      val works: Try[NodeSeq] = xml \! "Employee" \! "Works"
      val data: Try[NodeSeq] = xml \! "Other" \! "Data"

      assert(cars.isSuccess)
      assert(cars.toEither.exists(_.length == 1))

      //missing Nodes
      assert(works.isFailure)
      assert(data.isFailure)
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

      val cars: Option[NodeSeq] = xml \\? "Cars"
      val works: Option[NodeSeq] = xml \\? "Works"

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

      val cars: Try[NodeSeq] = xml \\! "Cars"
      val works: Try[NodeSeq] = xml \\! "Works"

      assert(cars.isSuccess)
      assert(cars.toEither.exists(_.length == 1))
      assert(works.isFailure)
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

      val note: Option[String] = xml \ "Employee" \ "Note" ?
      val works: Option[String] = xml \ "Employee" \ "Works" ?

      assert(note.isDefined)
      assert(note.get.trim == noteData)
      assert(works.isEmpty)
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

      val note: Option[String] = xml \? "Employee" \? "Note" ?
      val works: Option[String] = xml \? "Employee" \? "Works" ?

      assert(note.isDefined)
      assert(note.get.trim == noteData)
      assert(works.isEmpty)
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

      val note: Try[String] = xml \ "Employee" \ "Note" !
      val works: Try[String] = xml \ "Employee" \ "Works" !

      assert(note.isSuccess)
      assert(note.toEither.exists(_.trim == noteData))
      assert(works.isFailure)
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

      val note: Try[String] = xml \ "Employee" \ "Note" !
      val works: Try[String] = xml \! "Employee" \! "Works" !

      assert(note.isSuccess)
      assert(note.toEither.exists(_.trim == noteData))
      assert(works.isFailure)
    }
  }
}
