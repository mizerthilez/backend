package dev.ironduck.`macro`

import scala.deriving.Mirror
import scala.compiletime.{ constValue, erasedValue, summonInline }

trait EasyDerive[TC[_]]:
  final def apply[A](using tc: TC[A]): TC[A] = tc

  case class CaseClassElement[A, B](
    label: String,
    typeclass: TC[B],
    getValue: A => B,
    idx: Int,
  )
  case class CaseClassType[A](
    label: String,
    elements: List[CaseClassElement[A, ?]],
    fromElements: List[Any] => A,
  )

  case class SealedElement[A, B](
    label: String,
    typeclass: TC[B],
    idx: Int,
    cast: A => B,
  )
  case class SealedType[A](
    label: String,
    elements: List[SealedElement[A, ?]],
    getElement: A => SealedElement[A, ?],
  )

  inline def getInstances[A <: Tuple]: List[TC[Any]] = inline erasedValue[A] match
    case _: EmptyTuple => Nil
    case _: (t *: ts) => summonInline[TC[t]].asInstanceOf[TC[Any]] :: getInstances[ts]

  inline def getElemLabels[A <: Tuple]: List[String] = inline erasedValue[A] match
    case _: EmptyTuple => Nil
    case _: (t *: ts) => constValue[t].toString :: getElemLabels[ts]

  def deriveCaseClass[A](caseClassType: CaseClassType[A]): TC[A]

  def deriveSealed[A](sealedType: SealedType[A]): TC[A]

  inline given derived[A](using m: Mirror.Of[A]): TC[A] =
    val label = constValue[m.MirroredLabel]
    val elemInstances = getInstances[m.MirroredElemTypes]
    val elemLabels = getElemLabels[m.MirroredElemLabels]

    inline m match
      case s: Mirror.SumOf[A] =>
        val elements = elemInstances
          .zip(elemLabels)
          .zipWithIndex
          .map:
            case ((inst, lbl), idx) =>
              SealedElement[A, Any](lbl, inst.asInstanceOf[TC[Any]], idx, identity)
        val getElement = (a: A) => elements(s.ordinal(a))
        deriveSealed(SealedType[A](label, elements, getElement))

      case p: Mirror.ProductOf[A] =>
        val caseClassElements = elemInstances
          .zip(elemLabels)
          .zipWithIndex
          .map:
            case ((inst, lbl), idx) =>
              CaseClassElement[A, Any](
                lbl,
                inst.asInstanceOf[TC[Any]],
                (x: Any) => x.asInstanceOf[Product].productElement(idx),
                idx,
              )

        val fromElements: List[Any] => A = elements =>
          p.fromProduct(new FromProduct(caseClassElements.size, elements))

        deriveCaseClass(CaseClassType[A](label, caseClassElements, fromElements))

  final class FromProduct(size: Int, elements: List[Any]) extends Product:
    override def productArity: Int = size

    override def productElement(n: Int): Any = elements(n)

    override def canEqual(that: Any): Boolean = false
