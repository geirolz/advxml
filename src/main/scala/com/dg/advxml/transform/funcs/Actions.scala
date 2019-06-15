package com.dg.advxml.transform.funcs

import scala.xml._

trait XmlAction extends (NodeSeq => NodeSeq){
	def ++(that: XmlAction) : XmlAction = andThen(that)
	def andThen(that: NodeSeq => NodeSeq) : XmlAction = xml => that(this(xml))
}

object Actions extends Actions{
	def node(f: Node => NodeSeq): XmlAction = nodeSeq(seq => seq.flatMap(f))
	def nodeSeq(f: NodeSeq => NodeSeq): XmlAction = f(_)
}

private [advxml] trait Actions {

	def append(ns: NodeSeq) : XmlAction = Actions.node {
		case elem: Elem => elem.copy(child = elem.child ++ ns)
		case g: Group => g.copy(nodes = g.nodes ++ ns)
		case other => other
	}

	def replace(ns: NodeSeq) : XmlAction = Actions.nodeSeq(_ => ns)

	def remove: XmlAction = Actions.nodeSeq(_ => Seq.empty)


	def setAttrs(values: (String, String)*): XmlAction = Actions.node {
		case elem: Elem =>
			elem.copy(attributes = values.foldRight(elem.attributes)((value, metadata) =>
				new UnprefixedAttribute(value._1, Text(value._2), metadata)))
		case other => other
	}

	def removeAttrs(keys: String*): XmlAction = Actions.node {
		case elem: Elem =>
			elem.copy(attributes = elem.attributes.filter(attr => keys.contains(attr.key)))
		case other => other
	}
}

