package advxml.syntax

import org.scalatest.wordspec.AnyWordSpec

import scala.util.{Success, Try}
import scala.xml.{NodeSeq, Text}

class ModifiersTest extends AnyWordSpec {

  import advxml.instances._
  import advxml.instances.transform.modifiers._
  import cats.instances.try_._

  "Append node modifier" when {
    "Applied with right data" should {
      "Append new node to XML element" in {

        val xml =
          <Persons>
            <Person Name="David" />
          </Persons>

        val modifier = Append(<Person Name="Alessandro"/>)
        val result: Try[NodeSeq] = modifier(xml)

        assert(
          result
            .map(_ \ "Person")
            .map(_.length) == Success(2)
        )

        assert(result.map(_ \ "Person" exists (_ \@ "Name" == "David")) == Success(true))
        assert(result.map(_ \ "Person" exists (_ \@ "Name" == "Alessandro")) == Success(true))
      }
    }

    "Applied to wrong object" should {
      "Return a failure" in {

        val xml = Text("TEST")

        val modifier = Append(<Person Name="Alessandro"/>)
        val result: Try[NodeSeq] = modifier(xml)

        assert(result.isFailure)
      }
    }
  }

  "Replace node modifier" when {
    "Applied with right data" should {
      "Replace old node with new XML element" in {

        val xml =
          <Persons>
            <Person Name="David"/>
          </Persons>

        val modifier = Replace(_ => <Person Name="Alessandro"/>)
        val result: Try[NodeSeq] = modifier(xml \ "Person")

        assert(result.map(_.length) == Success(1))
        assert(result.map(_.exists(_ \@ "Name" == "Alessandro")) == Success(true))
      }
    }
  }

  "Remove node modifier" when {
    "Applied with right data" should {
      "Remove XML element" in {

        val xml =
          <Persons>
            <Person Name="David"/>
          </Persons>

        val modifier = Remove
        val result: Try[NodeSeq] = modifier(xml \ "Person")

        assert(result.get.length == 0)
        assert(!(result.get exists (_ \@ "Name" == "David")))
      }
    }
  }

  "SetAttrs modifier" when {
    "Applied with strings data" should {
      "Set specified attrs to XML element" in {

        val xml = <Root/>

        val modifier = SetAttrs(
          k"T1" := "1",
          k"T2" := "2",
          k"T3" := "3"
        )
        val result: Try[NodeSeq] = modifier(xml)

        assert(result.map(_.exists(_ \@ "T1" == "1")) == Success(true))
        assert(result.map(_.exists(_ \@ "T2" == "2")) == Success(true))
        assert(result.map(_.exists(_ \@ "T3" == "3")) == Success(true))
      }
    }

    "Applied with ints data" should {
      "Set specified attrs to XML element" in {

        val xml = <Root/>

        val modifier = SetAttrs(
          k"T1" := 1,
          k"T2" := 2,
          k"T3" := 3
        )

        val result: Try[NodeSeq] = modifier(xml)

        assert(result.map(_.exists(_ \@ "T1" == "1")) == Success(true))
        assert(result.map(_.exists(_ \@ "T2" == "2")) == Success(true))
        assert(result.map(_.exists(_ \@ "T3" == "3")) == Success(true))
      }
    }

    "Applied to wrong object" should {
      "Return a failure" in {
        val xml = Text("TEST")

        val modifier = SetAttrs(
          k"T1" := "1",
          k"T2" := "2",
          k"T3" := "3"
        )
        val result = modifier(xml)

        assert(result.isFailure)
      }
    }
  }

  "RemoveAttrs modifier" when {
    "Applied with right data" should {
      "Remove specified attrs to XML element" in {

        val xml = <Root T1="1" T2="2" T3="3"/>

        val modifier = RemoveAttrs(_.key == k"T1", _.key == k"T2", _.key == k"T3")
        val result = modifier(xml)

        assert(result.map(_.exists(_ \@ "T1" == "1")) == Success(false))
        assert(result.map(_.exists(_ \@ "T2" == "2")) == Success(false))
        assert(result.map(_.exists(_ \@ "T3" == "3")) == Success(false))
      }
    }

    "Applied to wrong object" should {
      "Return a failure" in {
        val xml = Text("TEST")

        //TODO TO FIX THAT
        val modifier = RemoveAttrs(_.key == k"T1", _.key == k"T2", _.key == k"T3")
        val result: Try[NodeSeq] = modifier(xml)

        assert(result.isFailure)
      }
    }
  }
}
