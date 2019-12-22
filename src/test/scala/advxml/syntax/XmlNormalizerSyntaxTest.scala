package advxml.syntax

import org.scalatest.FunSuite

/**
  * Advxml
  * Created by geirolad on 19/07/2019.
  *
  * @author geirolad
  */
class XmlNormalizerSyntaxTest extends FunSuite {

  import advxml.syntax.normalize._

  test("Xml Normalizer") {

    val v1 =
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

    val expected =
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

    assert(v1.normalize |==| expected)
  }

  test("Xml Equality") {

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

  test("Xml Equality - with Scalatric") {

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

  test("Xml Equality - Not equals") {

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

  test("Xml Equality - Not equals - with Scalatric") {

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
