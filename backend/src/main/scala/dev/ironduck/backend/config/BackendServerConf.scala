package dev.ironduck.backend
package config

import cats.effect.IO
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter

import features.text.TextController
import features.guest.GuestController
import features.pet.PetController

object BackendServerConf:
  private val apiEndPoints =
    TextController.endpoints ++ GuestController.endpoints ++ PetController.endpoints

  private val docsEndpoint = // Merge all endpoints as a fully usable OpenAPI doc
    SwaggerInterpreter()
      .fromServerEndpoints[IO](
        apiEndPoints,
        "Backend",
        "1.0",
      )

  val allRoutes = // Serve the OpenAPI doc & all the other routes
    Http4sServerInterpreter[IO]().toRoutes(
      docsEndpoint ++ apiEndPoints
    )
