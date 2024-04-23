package dev.ironduck.backend
package features.pet

import cats.effect.IO
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import dto.PetDto
import shared.BackendException

object PetController:
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

  val petContestServerEndpoint =
    petContestEndpoint.serverLogicRecoverErrors(PetService.petContest(_))

  val endpoints: List[ServerEndpoint[Any, IO]] = List(petContestServerEndpoint)
