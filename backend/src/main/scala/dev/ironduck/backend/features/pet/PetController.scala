package dev.ironduck.backend
package features.pet

import cats.effect.IO
import cats.implicits.*
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.generic.auto.*

import dto.PetDto
import shared.BackendException

object PetController:
  def endpoints: List[AnyEndpoint] = List(petContestEndpoint)

  def routes: HttpRoutes[IO] = petContestRoutes

  private val petContestEndpoint = endpoint // The endpoint and it is used to generate the OpenAPI
    .summary("Pet Contest")
    .post
    .in("pets" / "contest")
    .in(jsonBody[Vector[PetDto]])
    .out(jsonBody[PetDto])
    .errorOut(
      statusCode(StatusCode.BadRequest)
        .and(jsonBody[BackendException])
    )

  private val petContestRoutes = // convert the endpoint to actual http4s route
    Http4sServerInterpreter[IO]().toRoutes(
      petContestEndpoint.serverLogicRecoverErrors(PetService.petContest(_))
    )
