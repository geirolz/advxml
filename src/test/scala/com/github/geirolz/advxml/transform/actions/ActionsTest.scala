package com.github.geirolz.advxml.transform.actions

import org.scalatest.WordSpec

import scala.xml.Text

class ActionsTest extends WordSpec {

  import cats.instances.try_._
  import com.github.geirolz.advxml.AdvXml._

  "Append node action" when {
    "Applied with right data" should {
      "Append new node to XML element" in {

        val xml =
          <Persons>
            <Person Name="David" />
          </Persons>

        val action = Append(<Person Name="Alessandro"/>)
        val result = action(xml)

        assert((result.get \ "Person").length == 2)
        assert(result.get \ "Person" exists attrs("Name" -> "David"))
        assert(result.get \ "Person" exists attrs("Name" -> "Alessandro"))
      }
    }

    "Applied to wrong object" should {
      "Return a failure" in {

        val xml = Text("TEST")

        val action = Append(<Person Name="Alessandro"/>)
        val result = action(xml)

        assert(result.isFailure)
        Console.println(result.failed.get.getMessage)
      }
    }
  }
}
