package com.dg.advxml.transform.actions

import cats.{Applicative, Monad, Traverse}

import scala.util.{Failure, Success, Try}
import scala.xml._

sealed trait XmlModifier{def apply(ns: NodeSeq): Try[NodeSeq]}
sealed trait FinalXmlModifier extends XmlModifier
sealed trait ComposableXmlModifier extends XmlModifier{
	def andThen(that: ComposableXmlModifier) : ComposableXmlModifier =
		Modifiers.builders.Composable(xml => this (xml).flatMap(that(_)))
}


private [transform] trait Modifiers {

	import builders._
	import cats.instances.list._
	import cats.instances.try_._

	//STDs
	case class Append(ns: NodeSeq) extends AbstractComposableCollapsed(seq => seq.map{
		case elem: Elem => Success(elem.copy(child = elem.child ++ ns))
		case g: Group => Success(g.copy(nodes = g.nodes ++ ns))
		case _ => UnsupportedException()
	})

	case class Replace(ns: NodeSeq) extends AbstractComposable(_ => Success(ns))

	case class SetAttrs(values: (String, String)*) extends AbstractComposableCollapsed(seq => seq.map {
		case elem: Elem =>
			Success(elem.copy(attributes = values.foldRight(elem.attributes)((value, metadata) =>
				new UnprefixedAttribute(value._1, Text(value._2), metadata))))
		case _ => UnsupportedException()
	})

	case class RemoveAttrs(keys: String*) extends AbstractComposableCollapsed(seq => seq.map {
		case elem: Elem =>
			Success(elem.copy(attributes = elem.attributes.filter(attr => keys.contains(attr.key))))
		case _ => UnsupportedException()
	})

	//FINALS
	case object Remove extends AbstractFinal(_ => Success(Seq.empty))


	private[actions] object builders {

		case class Composable(f: NodeSeq => Try[NodeSeq]) extends AbstractComposable(f)

		case class Final(f: NodeSeq => Try[NodeSeq]) extends AbstractFinal(f)

		private[Modifiers] sealed abstract class AbstractFinal(f: NodeSeq => Try[NodeSeq]) extends FinalXmlModifier {
			override def apply(ns: NodeSeq): Try[NodeSeq] = f(ns)
		}

		private[Modifiers] sealed abstract class AbstractComposable(f: NodeSeq => Try[NodeSeq]) extends ComposableXmlModifier {
			override def apply(ns: NodeSeq): Try[NodeSeq] = f(ns)
		}

		private[Modifiers] sealed abstract class AbstractComposableCollapsed(f: NodeSeq => Seq[Try[NodeSeq]])
			extends AbstractComposable(ns => collapse[Try](f(ns)))

		object UnsupportedException{
			def apply[T](): Failure[T] = Failure(new RuntimeException(""))
		}

		def collapse[G[_] : Applicative : Monad](seq: Seq[G[NodeSeq]]) : G[NodeSeq] =
			Monad[G].map(Traverse[List].sequence(seq.toList))(_.foldLeft(NodeSeq.Empty)(_ ++ _))
	}
}

object Modifiers extends Modifiers