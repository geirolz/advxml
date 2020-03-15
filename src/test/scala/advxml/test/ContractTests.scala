package advxml.test

import advxml.test.ContractTests.ContractTest
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.funsuite.AnyFunSuite

abstract class ContractTests(mainDesc: String, subDesc: String = "") {

  private var _allTests: List[ContractTest] = List()

  def getAllTests: List[ContractTest] = _allTests

  protected final def test(testName: String)(testFun: => Unit): Unit =
    registerTest(new ContractTest(testName, mainDesc, subDesc, () => testFun))

  private def registerTest(ct: ContractTest): Unit =
    _allTests = _allTests :+ ct
}

object ContractTests {
  class ContractTest private[ContractTests] (
    val testName: String,
    val mainDesc: String,
    val subDesc: String = "",
    private[test] val testFun: () => Unit
  ) {
    val fullDesc: String = List(mainDesc, subDesc).filter(_.nonEmpty).mkString(".")
  }
}

trait ContractTestsSyntax { $this: ContractTestsRunner =>

  implicit class contractTestRunnerOps(ct: ContractTest) {
    def run(): Unit = $this.run(ct)
  }

  implicit class contractTestRunnerSeqOps(ctSeq: Seq[ContractTest]) {
    def runAll(): Seq[Unit] = $this.runAll(ctSeq)
  }

  implicit class contractTestsSuiteRunnerOps(suite: ContractTests) {
    def runAll(): Seq[Unit] = suite.getAllTests.runAll()
  }
}

trait ContractTestsRunner {
  def run(ct: ContractTest): Unit
  def runAll(cts: Seq[ContractTest]): Seq[Unit] = cts.map(run)
}

trait FunSuiteContract extends ContractTestsRunner with ContractTestsSyntax { this: AnyFunSuite =>
  def run(ct: ContractTest): Unit = {
    test(testName = s"[${ct.fullDesc}] - ${ct.testName}")(ct.testFun.apply())
  }
}

trait FeatureSpecContract extends ContractTestsRunner with ContractTestsSyntax { this: AnyFeatureSpec =>

  def run(ct: ContractTest): Unit = {
    Scenario(ct.testName)(ct.testFun.apply())
  }

  override def runAll(cts: Seq[ContractTest]): Seq[Unit] = {
    cts
      .groupBy(_.fullDesc)
      .map {
        case (featureName, tests) =>
          Feature(featureName) {
            tests.map(_.run()).reduce((_, _) => ())
          }
      }
      .toSeq
  }
}
