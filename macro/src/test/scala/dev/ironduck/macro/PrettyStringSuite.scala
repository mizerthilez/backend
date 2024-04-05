package dev.ironduck.`macro`

trait PrettyString[A]:
  def prettyString(a: A): String

object PrettyString extends EasyDerive[PrettyString]:
  override def deriveCaseClass[A](productType: CaseClassType[A]): PrettyString[A] =
    new PrettyString[A]:
      override def prettyString(a: A): String =
        if productType.elements.isEmpty
        then productType.label
        else
          val pairStrs = productType.elements.map: p =>
            s"${p.label}=${p.typeclass.prettyString(p.getValue(a))}"

          pairStrs.mkString(s"${productType.label}(", ", ", ")")

  override def deriveSealed[A](sumType: SealedType[A]): PrettyString[A] =
    new PrettyString[A]:
      override def prettyString(a: A): String =
        val elem = sumType.getElement(a)
        elem.typeclass.prettyString(elem.cast(a))

  given PrettyString[String] with
    def prettyString(x: String): String = s"\"$x\""

  given PrettyString[Int] with
    def prettyString(x: Int): String = x.toString

  given PrettyString[Long] with
    def prettyString(x: Long): String = x.toString

  given PrettyString[Double] with
    def prettyString(x: Double): String = x.toString

  given PrettyString[Boolean] with
    def prettyString(x: Boolean): String = x.toString

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

  test("PrettyString should function well"):
    val prettyStrings = someVisitors.map:
      PrettyString[Visitor].prettyString

    assertEquals(
      prettyStrings,
      List("""User(name="bob", age=25)""", "AnonymousVisitor"),
    )
