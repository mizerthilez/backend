package dev.ironduck.`macro`

import java.time.temporal.TemporalAccessor

trait PrettyString[A]:
  def prettyString(a: A): String

object PrettyString extends EasyDerive[PrettyString]:
  def styled(color: String, in: Any): String =
    color + in + scala.Console.RESET

  def dotted(in: Any): String =
    scala.Console.MAGENTA + "-" * 20 + in + "-" * 20 + scala.Console.RESET

  override def deriveCaseClass[A](productType: CaseClassType[A]): PrettyString[A] =
    new PrettyString[A]:
      override def prettyString(a: A): String =
        if productType.elements.isEmpty
        then styled(scala.Console.YELLOW, productType.label)
        else
          val pairStrs = productType.elements.map: p =>
            s"${p.label}=${p.typeclass.prettyString(p.getValue(a))}"

          pairStrs.mkString(s"${styled(scala.Console.YELLOW, productType.label)}(", ", ", ")")

  override def deriveSealed[A](sumType: SealedType[A]): PrettyString[A] =
    new PrettyString[A]:
      override def prettyString(a: A): String =
        val elem = sumType.getElement(a)
        elem.typeclass.prettyString(elem.cast(a))

  given PrettyString[String] with
    def prettyString(x: String): String = styled(scala.Console.GREEN, s"\"$x\"")

  given PrettyString[Boolean] with
    def prettyString(x: Boolean): String = styled(scala.Console.GREEN, x.toString)

  given PrettyString[Byte] with
    def prettyString(x: Byte): String = styled(scala.Console.GREEN, x.toString)

  given PrettyString[Short] with
    def prettyString(x: Short): String = styled(scala.Console.GREEN, x.toString)

  given PrettyString[Int] with
    def prettyString(x: Int): String = styled(scala.Console.GREEN, x.toString)

  given PrettyString[Long] with
    def prettyString(x: Long): String = styled(scala.Console.GREEN, x.toString)

  given PrettyString[Float] with
    def prettyString(x: Float): String = styled(scala.Console.GREEN, x.toString)

  given PrettyString[Double] with
    def prettyString(x: Double): String = styled(scala.Console.GREEN, x.toString)

  given temporalPrettyString[A <: TemporalAccessor]: PrettyString[A] with
    def prettyString(x: A): String = styled(scala.Console.GREEN, x.toString)

  given optionPrettyString[A](using ps: PrettyString[A]): PrettyString[Option[A]] with
    def prettyString(x: Option[A]): String = x match
      case Some(a) => s"${styled(scala.Console.YELLOW, "Some")}(${ps.prettyString(a)})"
      case None => "None"

  given tuplePrettyString[A, B](
    using psa: PrettyString[A],
    psb: PrettyString[B],
  ): PrettyString[(A, B)] with
    def prettyString(x: (A, B)): String =
      s"${psa.prettyString(x._1)} -> ${psb.prettyString(x._2)}"

  given iterPrettyString[A, S[A] <: Iterable[A]](using ps: PrettyString[A]): PrettyString[S[A]] with
    def prettyString(iter: S[A]): String = iter.map(ps.prettyString).mkString("[", ", ", "]")

  given mapPrettyString[A, B, S[A, B] <: Map[A, B]](using ps: PrettyString[(A, B)])
    : PrettyString[S[A, B]] with
    def prettyString(m: S[A, B]): String = m.map(ps.prettyString).mkString("{", ", ", "}")

  def prettyPrintln[A](a: A)(using ps: PrettyString[A]) =
    println(ps.prettyString(a))

class PrettyStringSuite extends munit.FunSuite:
  enum Visitor derives PrettyString: // magic happens here via 'derives'
    case User(name: String, age: Int)
    case AnonymousVisitor

  var someVisitors: List[Visitor] = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
    import Visitor.*
    someVisitors = List(
      User("bob", 25),
      AnonymousVisitor,
    )

  test("PrettyString should work"):
    val prettyStrings = someVisitors.map(PrettyString[Visitor].prettyString)

    println(PrettyString.dotted("PrettyString should work"))
    prettyStrings.foreach(println)

    import PrettyString.styled
    assertEquals(
      prettyStrings,
      List(
        s"""${styled(scala.Console.YELLOW, "User")}(name=${styled(scala.Console.GREEN, "\"bob\"")}, age=${styled(scala.Console.GREEN, 25)})""",
        styled(scala.Console.YELLOW, "AnonymousVisitor"),
      ),
    )
