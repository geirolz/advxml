package com.github.geirolz.advxml.transform.actions

import org.scalatest.WordSpec

import scala.xml.Text

class ModifiersTest extends WordSpec {

  import Modifiers._
  import cats.instances.try_._

  "Append node modifier" when {
    "Applied with right data" should {
      "Append new node to XML element" in {

        val xml =
          <Persons>
            <Person Name="David" />
          </Persons>

        val modifier = Append(<Person Name="Alessandro"/>)
        val result = modifier(xml)

        assert((result.get \ "Person").length == 2)
        assert(result.get \ "Person" exists (_ \@ "Name" == "David"))
        assert(result.get \ "Person" exists (_ \@ "Name" == "Alessandro"))
      }
    }

    "Applied to wrong object" should {
      "Return a failure" in {

        val xml = Text("TEST")

        val modifier = Append(<Person Name="Alessandro"/>)
        val result = modifier(xml)

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

        val modifier = Replace(<Person Name="Alessandro"/>)
        val result = modifier(xml \ "Person")

        assert(result.get.length == 1)
        assert(result.get exists (_ \@ "Name" == "Alessandro"))
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
        val result = modifier(xml \ "Person")

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
          "T1" -> "1",
          "T2" -> "2",
          "T3" -> "3"
        )
        val result = modifier(xml)

        assert(result.get exists (_ \@ "T1" == "1"))
        assert(result.get exists (_ \@ "T2" == "2"))
        assert(result.get exists (_ \@ "T3" == "3"))
      }
    }

    "Applied with ints data" should {
      "Set specified attrs to XML element" in {

        val xml = <Root/>

        val modifier = SetAttrs(
          "T1" -> 1,
          "T2" -> 2,
          "T3" -> 3
        )(_.toString)

        val result = modifier(xml)

        assert(result.get exists (_ \@ "T1" == "1"))
        assert(result.get exists (_ \@ "T2" == "2"))
        assert(result.get exists (_ \@ "T3" == "3"))
      }
    }

    "Applied to wrong object" should {
      "Return a failure" in {
        val xml = Text("TEST")

        val modifier = SetAttrs(
          "T1" -> "1",
          "T2" -> "2",
          "T3" -> "3"
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

        val modifier = RemoveAttrs("T1", "T2", "T3")
        val result = modifier(xml)

        assert(!(result.get exists (_ \@ "T1" == "1")))
        assert(!(result.get exists (_ \@ "T2" == "2")))
        assert(!(result.get exists (_ \@ "T3" == "3")))
      }
    }

    "Applied to wrong object" should {
      "Return a failure" in {
        val xml = Text("TEST")

        val modifier = RemoveAttrs("T1", "T2", "T3")
        val result = modifier(xml)

        assert(result.isFailure)
      }
    }
  }
}
