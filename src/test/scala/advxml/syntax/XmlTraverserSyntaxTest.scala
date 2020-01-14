package advxml.syntax

import org.scalatest.featurespec.AnyFeatureSpec

import scala.language.postfixOps
import scala.util.Try
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 19/06/2019.
  *
  * @author geirolad
  */
class XmlTraverserSyntaxTest extends AnyFeatureSpec {

  import advxml.syntax.traverse.try_._

  Feature("XmlTraverseSyntaxTest: Read Attributes") {
    Scenario("Read optional attribute") {
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

    Scenario("Read required attribute") {
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

  Feature("XmlTraverseSyntaxTest: Read Immediate Nodes") {
    Scenario("Read optional immediate nodes") {
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

    Scenario("Read optional immediate nodes waterfall") {
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

    Scenario("Read required immediate nodes") {
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

    Scenario("Read required immediate nodes waterfall") {
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

  Feature("XmlTraverseSyntaxTest: Read nested Nodes") {
    Scenario("Read optional nested nodes") {
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

    Scenario("Read required nested nodes") {
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

  Feature("XmlTraverseSyntaxTest: Read content") {
    Scenario("Read optional text") {
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

    Scenario("Read optional text waterfall") {
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

    Scenario("Read required text") {
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

    Scenario("Read required text waterfall") {
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
