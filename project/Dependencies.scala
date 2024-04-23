import sbt._

object Dependencies {
  object com {
    object eed3si9n {
      object expecty {
        // https://mvnrepository.com/artifact/com.eed3si9n.expecty/expecty
        val expecty = "com.eed3si9n.expecty" %% "expecty" % "0.16.0"
      }
    }

    object softwaremill {
      object sttp {
        object client3 {
          val circe = "com.softwaremill.sttp.client3" %% "circe" % "3.9.5"
        }
        object tapir {
          // Tapir
          // https://mvnrepository.com/artifact/com.softwaremill.sttp.tapir/tapir-http4s-server
          val `tapir-http4s-server` = moduleId("http4s-server")

          // https://mvnrepository.com/artifact/com.softwaremill.sttp.tapir/tapir-json-circe
          val `tapir-json-circe` = moduleId("json-circe")

          // https://mvnrepository.com/artifact/com.softwaremill.sttp.tapir/tapir-swagger-ui-bundle
          val `tapir-swagger-ui-bundle` = moduleId("swagger-ui-bundle")

          // https://mvnrepository.com/artifact/com.softwaremill.sttp.tapir/tapir-sttp-stub-server
          val `tapir-sttp-stub-server` = moduleId("sttp-stub-server")

          private def moduleId(artifact: String): ModuleID =
            "com.softwaremill.sttp.tapir" %% s"tapir-$artifact" % "1.10.0"
        }
      }
    }
  }

  object dev {
    object optics {
      // https://mvnrepository.com/artifact/dev.optics/monocle-core
      val `monocle-core` = "dev.optics" %% "monocle-core" % "3.2.0"
    }
  }

  object io {
    object circe {
      // Circe
      // https://mvnrepository.com/artifact/io.circe/circe-parser
      val `circe-parser` = moduleId("parser")

      // https://mvnrepository.com/artifact/io.circe/circe-generic
      val `circe-generic` = moduleId("generic")

      // https://mvnrepository.com/artifact/io.circe/circe-literal
      val `circe-literal` = moduleId("literal")

      private def moduleId(artifact: String): ModuleID =
        "io.circe" %% s"circe-$artifact" % "0.14.6"
    }
  }

  object org {
    object http4s {
      // Http4s
      // https://mvnrepository.com/artifact/org.http4s/http4s-ember-server
      val `http4s-ember-server` = moduleId("ember-server")

      // https://mvnrepository.com/artifact/org.http4s/http4s-circe
      val `http4s-circe` = moduleId("circe")

      // https://mvnrepository.com/artifact/org.http4s/http4s-dsl
      val `http4s-dsl` = moduleId("dsl")

      // https://mvnrepository.com/artifact/org.http4s/http4s-ember-client
      val `http4s-ember-client` = moduleId("ember-client")

      private def moduleId(artifact: String): ModuleID =
        "org.http4s" %% s"http4s-$artifact" % "0.23.26"
    }

    object scalacheck {
      // https://mvnrepository.com/artifact/org.scalacheck/scalacheck
      val scalacheck = "org.scalacheck" %% "scalacheck" % "1.17.0"
    }

    object scalameta {
      // https://mvnrepository.com/artifact/org.scalameta/munit
      val munit = moduleId("munit")

      // https://mvnrepository.com/artifact/org.scalameta/munit-scalacheck
      val `munit-scalacheck` = moduleId("munit-scalacheck")

      private def moduleId(artifact: String): ModuleID =
        "org.scalameta" %% artifact % "1.0.0-M11"
    }

    object typelevel {
      // https://mvnrepository.com/artifact/org.typelevel/discipline-munit
      val `discipline-munit` = "org.typelevel" %% "discipline-munit" % "1.0.9"

      // https://mvnrepository.com/artifact/org.typelevel/cats-effect
      val `cats-effect` =
        "org.typelevel" %% "cats-effect" % "3.5.4"
    }
  }
}
