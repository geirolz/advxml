package com.dg.advxml.transform.actions

import org.scalatest.WordSpec

class ActionsTest extends WordSpec {

  import com.dg.advxml.AdvXml._

  "Append node action" when {
    "Applied with right data" should {
      "Append new node to XML element" +
        "" in {

        val xml =
          <Persons>
            <Person Name="David" />
          </Persons>

        val action = Append(<Person Name="Alessandro"/>)
        val result = action(xml)

        assert((result \ "Person").length == 2)
        assert(result \ "Person" exists attrs("Name" -> "David"))
        assert(result \ "Person" exists attrs("Name" -> "Alessandro"))
      }
    }
  }
}
