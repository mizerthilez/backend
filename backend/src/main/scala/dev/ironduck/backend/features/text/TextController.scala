package dev.ironduck.backend
package features.text

import cats.effect.IO
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.HttpRoutes

object TextController:
  def endpoints: List[AnyEndpoint] = List(countCharactersEndpoint)

  def routes: HttpRoutes[IO] = countCharactersRoutes

  private val countCharactersEndpoint = endpoint // The endpoint and it is used to generate the OpenAPI
    .summary("Count characters")
    .get
    .in("count-characters" / query[String]("text"))
    .out(jsonBody[Int])

  private val countCharactersRoutes = // convert the endpoint to actual http4s route
    Http4sServerInterpreter[IO]().toRoutes(
      countCharactersEndpoint.serverLogicSuccess(TextService.countCharacters(_))
    )
