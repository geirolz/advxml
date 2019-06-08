package com.dg.advxml

import scala.xml._
import scala.xml.transform.{RewriteRule, RuleTransformer}

object XmlAction {

  type Predicate = NodeSeq => Boolean
  type Action = NodeSeq => NodeSeq
  type Zoom = NodeSeq => NodeSeq

  object filters {
    def label(name: String): Predicate = {
      case n: Node => n.label == name
      case _ => false
    }

    def attr(key: String, value: String): Predicate = _ \@ key == value

    def equalsTo(ns: NodeSeq): Predicate = that => (ns, that) match {
      case (e1: Node, e2: Node) => e1 strict_== e2
      case (ns1: NodeSeq, ns2: NodeSeq) => ns1 strict_== ns2
      case _ => false
    }
  }

  object zooms {
    def firstChild: Zoom = _.headOption.getOrElse(Seq.empty)
  }

  object actions {

    def append(ns: NodeSeq) : Action = Action.node {
      case elem: Elem => elem.copy(child = elem.child ++ ns)
      case g: Group => g.copy(nodes = g.nodes ++ ns)
      case other => other
    }
    def replace(ns: NodeSeq) : Action = Action.nodeSeq(_ => ns)
    def remove: Action = Action.nodeSeq(_ => Seq.empty)


    def setAttr(key: String, value: String): Action = Action.node {
      case elem: Elem =>
        elem.copy(attributes = new UnprefixedAttribute(key, Text(value), elem.attributes))
      case other => other
    }

  }

  object Action {
    def node(f: Node => NodeSeq): Action = nodeSeq(seq => seq.flatMap(f))
    def nodeSeq(f: NodeSeq => NodeSeq): Action = f
  }

  sealed abstract case class Rule(zoom: Zoom, actions: Seq[Action]) {

    def toRewriteRule: NodeSeq => RewriteRule = root => {
      val target = zoom(root)
      val action = actions.reduce((a1, a2) => a1.compose(a2))
      val updated = action(target)

      new RewriteRule {
        override def transform(ns: Seq[Node]): Seq[Node] =
          if(ns == root || filters.equalsTo(target)(ns)) updated else ns
      }
    }
  }

  def $(z: Zoom)(actions: Action*): Rule = new Rule(z, actions){}
  def current(actions: Action*): Rule = $(r => r)(actions: _*)

  implicit class AddXmlAction(rootElem: NodeSeq) {
    def transform(rules: Rule*): NodeSeq =
      new RuleTransformer(rules.map(_.toRewriteRule(rootElem)): _*)
        .transform(rootElem)

    def transform(actions: Action*): NodeSeq = transform(current(actions: _*))
  }
}
