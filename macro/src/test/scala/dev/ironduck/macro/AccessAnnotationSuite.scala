package dev.ironduck.`macro`

import AccessAnnotation.*

class SqlName(val sqlName: String) extends StaticAnnotation
class CqlName(val cqlName: String) extends StaticAnnotation

class AccessAnnotationSuite extends munit.FunSuite:
  test("test annosFor"):
    @CqlName("app_user_c")
    @SqlName("app_user")
    case class AppUser(
      id: Long,
      firstName: Option[String],
      lastName: String,
    )

    val annos = annosFor[AppUser].map:
      case sn: SqlName => sn.sqlName
      case cn: CqlName => cn.cqlName

    assertEquals(
      annos,
      List("app_user", "app_user_c"),
    )

  test("test fieldAnnosFor"):
    case class AppUser(
      id: Long,
      @SqlName("firstName")
      firstName: Option[String],
      @SqlName("app_user")
      @CqlName("app_user_c")
      lastName: String,
    )

    val annosMap = fieldAnnosFor[AppUser]
      .filter(_._2.nonEmpty)
      .mapValues: list =>
        list.map:
          case sn: SqlName => sn.sqlName
          case cn: CqlName => cn.cqlName
      .toMap

    assertEquals(
      annosMap,
      Map("firstName" -> List("firstName"), "lastName" -> List("app_user_c", "app_user")),
    )
