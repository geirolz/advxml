package advxml.syntax

import org.scalatest.FunSuite

import scala.xml.Group

/**
  * Advxml
  * Created by geirolad on 19/07/2019.
  *
  * @author geirolad
  */
class XmlNormalizerSyntaxTest extends FunSuite {

  import advxml.syntax.normalize._

  test("XmlNormalizer - Normalize | On Elem") {

    val v1 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"></Property>
            <Property K="3">TEXT</Property>
          </Properties>
        </Car>
        <Car V1="4" ></Car>
        <Car V1="5" >TEXT</Car>
      </Cars>

    val expected =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"/>
            <Property K="3">TEXT</Property>
          </Properties>
        </Car>
        <Car V1="4" />
        <Car V1="5" >TEXT</Car>
      </Cars>

    assert(v1.normalize |==| expected)
  }

  test("XmlNormalizer - Normalize | On Group | NOT SUPPORTED") {
    val data = <Test></Test>
    val ns: Group = Group(data)

    assert(ns.normalize === data)
  }

  test("XmlNormalizer - Equality") {

    val v1 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"/>
          </Properties>
        </Car>
        <Car V1="3"/>
      </Cars>

    val v2 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"></Property>
          </Properties>
        </Car>
        <Car V1="3" ></Car>
      </Cars>

    assert(v1 |==| v2)
  }

  test("XmlNormalizer - Equality | with Scalatric") {

    val v1 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"/>
          </Properties>
        </Car>
        <Car V1="3"/>
      </Cars>

    val v2 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"></Property>
          </Properties>
        </Car>
        <Car V1="3" ></Car>
      </Cars>

    assert(v1 === v2)
  }

  test("XmlNormalizer - Equality | Not equals") {

    val v1 =
      <Cars>
        <Car V1="1"/>
        <Car V1="3"/>
      </Cars>

    val v2 =
      <Cars>
        <Car V1="3" ></Car>
      </Cars>

    assert(v1 |!=| v2)
  }

  test("XmlNormalizer - Equality | Not equals | with Scalatric") {

    val v1 =
      <Cars>
        <Car V1="1"/>
        <Car V1="3"/>
      </Cars>

    val v2 =
      <Cars>
        <Car V1="3" ></Car>
      </Cars>

    assert(v1 !== v2)
  }
}
