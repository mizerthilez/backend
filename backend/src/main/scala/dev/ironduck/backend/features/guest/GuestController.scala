package dev.ironduck.backend
package features.guest

import cats.effect.IO
import cats.implicits.*
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.generic.auto.*

import dto.GuestDto
import shared.BackendException

object GuestController:
  def endpoints: List[AnyEndpoint] = List(letEnterAdultGuestEpt, listGuestsEpt)
  def routes: HttpRoutes[IO] = letEnterAdultGuestRts <+> listGuestsRts

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
  private val letEnterAdultGuestRts = Http4sServerInterpreter[IO]().toRoutes(
    letEnterAdultGuestEpt
      .serverLogicRecoverErrors( // == recover from "BadRequestException" exceptions raised + Encode as JSON and return them
        dto => GuestService.letEnterAdultGuest(dto)
      )
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
  private val listGuestsRts =
    Http4sServerInterpreter[IO]().toRoutes(
      listGuestsEpt.serverLogicSuccess(_ => GuestService.listGuests())
    )
