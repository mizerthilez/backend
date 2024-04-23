package dev.ironduck.backend
package features.guest

import cats.effect.IO
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import dto.GuestDto
import shared.BackendException

object GuestController:
  private val letEnterAdultGuestEpt = endpoint
    .summary("Let enter adult guest")
    .post
    .in("guests")
    .in(jsonBody[GuestDto]) // ✨ Auto Derivation Magic applied! (Not just simple type but case class this time)
    .out(jsonBody[GuestModel]) // ✨ Auto Derivation Magic applied!
    .errorOut(
      statusCode(StatusCode.BadRequest)
        .and(jsonBody[BackendException]) // This endpoint can throw errors + ✨ Auto Derivation Magic applied!
    )

  val letEnterAdultGuestServerEndpoint =
    letEnterAdultGuestEpt.serverLogicRecoverErrors( // == recover from "BadRequestException" exceptions raised + Encode as JSON and return them
      dto => GuestService.letEnterAdultGuest(dto)
    )

  private val listGuestsEpt = endpoint
    .summary("List guests")
    .get
    .in("guests")
    .out(
      jsonBody[
        Vector[
          GuestModel // /!\ Vector of Scala case class derivable is also derivable == ✨ Auto Derivation Magic applied!
        ]
      ]
    )

  val listGuestsServerEndpoint =
    listGuestsEpt.serverLogicSuccess(_ => GuestService.listGuests())

  val endpoints: List[ServerEndpoint[Any, IO]] =
    List(letEnterAdultGuestServerEndpoint, listGuestsServerEndpoint)
