package advxml.syntax

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

  import advxml.syntax.traverse.try_._

  feature("XmlTraverseSyntaxTest: Read Attributes") {
    scenario("Read optional attribute") {
      val xml =
        <Employers>
          <Employee Name="David"/>
        </Employers>

      val name: Try[Option[String]] = xml \ "Employee" \@? "Name"
      val age: Try[Option[String]] = xml \ "Employee" \@? "Age"

      assert(name.isSuccess)
      assert(age.isSuccess)

      assert(name.get.isDefined)
      assert(name.get.get == "David")
      assert(age.get.isEmpty)
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

      val cars: Try[Option[NodeSeq]] = xml \ "Employee" \? "Cars"
      val works: Try[Option[NodeSeq]] = xml \ "Employee" \? "Works"

      assert(cars.isSuccess)
      assert(works.isSuccess)

      assert(cars.get.isDefined)
      assert(cars.get.get.length == 1)
      assert(works.get.isEmpty)
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

      val cars: Try[Option[NodeSeq]] = xml \? "Employee" \? "Cars"
      val works: Try[Option[NodeSeq]] = xml \? "Employee" \? "Works"
      val data: Try[Option[NodeSeq]] = xml \? "Other" \? "Data"

      assert(cars.isSuccess)
      assert(works.isSuccess)
      assert(data.isSuccess)

      assert(cars.get.isDefined)
      assert(cars.get.get.length == 1)

      assert(works.get.isEmpty)
      assert(data.get.isEmpty)
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

      val cars: Try[Option[NodeSeq]] = xml \\? "Cars"
      val works: Try[Option[NodeSeq]] = xml \\? "Works"

      assert(cars.isSuccess)
      assert(works.isSuccess)

      assert(cars.get.isDefined)
      assert(cars.get.get.length == 1)
      assert(works.get.isEmpty)
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

      val note: Try[Option[String]] = xml \ "Employee" \ "Note" ?
      val works: Try[Option[String]] = xml \ "Employee" \ "Works" ?

      assert(note.isSuccess)
      assert(works.isSuccess)

      assert(note.get.isDefined)
      assert(note.get.get.trim == noteData)
      assert(works.get.isEmpty)
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

      val note: Try[Option[String]] = xml \? "Employee" \? "Note" ?
      val works: Try[Option[String]] = xml \? "Employee" \? "Works" ?

      assert(note.isSuccess)
      assert(works.isSuccess)

      assert(note.get.isDefined)
      assert(note.get.get.trim == noteData)
      assert(works.get.isEmpty)
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
