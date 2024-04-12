package dev.ironduck.backend

import java.time.YearMonth
import dev.ironduck.`macro`.PrettyString
import monocle.Traversal

case class User(name: String, debitCards: Vector[DebitCard])
case class DebitCard(cardNumber: String, expirationDate: YearMonth, securityCode: Int)

case class Lecturer(firstName: String, lastName: String, salary: Int)
case class Department(budget: Int, lecturers: List[Lecturer])
case class University(name: String, departments: Map[String, Department])

class MonocleSuite extends munit.FunSuite:
  lazy val anna = User(
    "Anna",
    Vector(
      DebitCard("4568 5794 3109 3087", YearMonth.of(2022, 4), 361),
      DebitCard("5566 2337 3022 2470", YearMonth.of(2024, 8), 990),
    ),
  )

  lazy val bob = User("Bob", Vector())

  test("index should work"):
    import monocle.syntax.all.*

    val anna_new = anna
      .focus(_.debitCards.index(0).expirationDate)
      .replace(YearMonth.of(2026, 3))

    PrettyString.prettyPrintln(anna_new)
    assertEquals(anna_new.debitCards(0).expirationDate, YearMonth.of(2026, 3))

    val bob_new = bob
      .focus(_.debitCards.index(0).expirationDate)
      .replace(YearMonth.of(2026, 3))

    PrettyString.prettyPrintln(bob_new)
    assertEquals(bob_new, bob)

  lazy val uni = University(
    "oxford",
    Map(
      "Computer Science" -> Department(
        45,
        List(
          Lecturer("john", "doe", 10),
          Lecturer("robert", "johnson", 16),
        ),
      ),
      "History" -> Department(
        30,
        List(
          Lecturer("arnold", "stones", 20)
        ),
      ),
    ),
  )
  end uni

  import monocle.Focus
  lazy val departments = Focus[University](_.departments)

  test("university - remove a department"):
    val uni_new = departments.at("History").replace(None)(uni)
    PrettyString.prettyPrintln(uni_new)
    assertEquals(uni_new.departments.get("History"), None)

  test("university - add a department"):
    val physics = Department(
      36,
      List(
        Lecturer("daniel", "jones", 12),
        Lecturer("roger", "smith", 14),
      ),
    )
    end physics

    val uni_new = departments.at("Physics").replace(Some(physics))(uni)
    PrettyString.prettyPrintln(uni_new)
    assertEquals(uni_new.departments.get("Physics"), Some(physics))

  test("university - all lectures get a salary increase"):
    import monocle.syntax.all.*
    import com.eed3si9n.expecty.Expecty.expect

    val uni_new = uni.focus(_.departments.each.lecturers.each.salary).modify(_ + 2)
    PrettyString.prettyPrintln(uni_new)
    expect(
      uni_new.departments("Computer Science").lecturers(0).salary == 12,
      uni_new.departments("Computer Science").lecturers(1).salary == 18,
      uni_new.departments("History").lecturers(0).salary == 22,
    )

  lazy val lecturers = Focus[Department](_.lecturers)
  lazy val firstName = Focus[Lecturer](_.firstName)
  lazy val lastName = Focus[Lecturer](_.lastName)
  lazy val all = departments.each.andThen(lecturers).each

  lazy val names = Traversal.apply2[Lecturer, String](_.firstName, _.lastName):
    case (fn, ln, l) => l.copy(firstName = fn, lastName = ln)

  test("university - all lectures's first names and last names get uppered"):
    import monocle.syntax.all.*
    import com.eed3si9n.expecty.Expecty.expect

    val uni_new = all.andThen(names).index(0).modify(_.toUpper)(uni)
    PrettyString.prettyPrintln(uni_new)
    expect(
      uni_new.departments("Computer Science").lecturers(0).firstName == "John",
      uni_new.departments("Computer Science").lecturers(1).lastName == "Johnson",
      uni_new.departments("History").lecturers(0).firstName == "Arnold",
    )
