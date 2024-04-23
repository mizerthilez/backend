package dev.ironduck.backend
package features.text

import cats.effect.IO
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

object TextController:
  private val countCharactersEndpoint = endpoint // The endpoint and it is used to generate the OpenAPI
    .summary("Count characters")
    .get
    .in("count-characters" / query[String]("text"))
    .out(jsonBody[Int])

  val countCharactersServerEndpoint =
    countCharactersEndpoint.serverLogicSuccess(TextService.countCharacters(_))

  val endpoints: List[ServerEndpoint[Any, IO]] = List(countCharactersServerEndpoint)
