package com.dg.advxml.transform.actions

import scala.xml._

sealed trait XmlModifier{def apply(v1: NodeSeq): NodeSeq}
sealed trait FinalXmlModifier extends XmlModifier
sealed trait ComposableXmlModifier extends XmlModifier{
	def andThen(that: ComposableXmlModifier) : ComposableXmlModifier =
		Modifiers.builders.Composable(xml => that(this(xml)))
}


private [transform] trait Modifiers {

	import builders._

	//STDs
	case class Append(ns: NodeSeq) extends AbstractComposable(seq => seq.flatMap{
		case elem: Elem => elem.copy(child = elem.child ++ ns)
		case g: Group => g.copy(nodes = g.nodes ++ ns)
		case other => other
	})

	case class Replace(ns: NodeSeq) extends AbstractComposable(_ => ns)

	case class SetAttrs(values: (String, String)*) extends AbstractComposable(seq => seq.flatMap {
		case elem: Elem =>
			elem.copy(attributes = values.foldRight(elem.attributes)((value, metadata) =>
				new UnprefixedAttribute(value._1, Text(value._2), metadata)))
		case other => other
	})

	case class RemoveAttrs(keys: String*) extends AbstractComposable(seq => seq.flatMap {
		case elem: Elem =>
			elem.copy(attributes = elem.attributes.filter(attr => keys.contains(attr.key)))
		case other => other
	})

	//FINALS
	case object Remove extends AbstractFinal(_ => Seq.empty)


	private[actions] object builders {

		case class Composable(f: NodeSeq => NodeSeq) extends AbstractComposable(f)

		case class Final(f: NodeSeq => NodeSeq) extends AbstractFinal(f)

		private[Modifiers] sealed abstract class AbstractComposable(f: NodeSeq => NodeSeq) extends ComposableXmlModifier {
			override def apply(ns: NodeSeq): NodeSeq = f(ns)
		}

		private[Modifiers] sealed abstract class AbstractFinal(f: NodeSeq => NodeSeq) extends FinalXmlModifier {
			override def apply(ns: NodeSeq): NodeSeq = f(ns)
		}
	}
}

object Modifiers extends Modifiers