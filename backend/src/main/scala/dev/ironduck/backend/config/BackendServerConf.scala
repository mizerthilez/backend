package dev.ironduck.backend
package config

import cats.effect.IO
import cats.implicits.*
import com.comcast.ip4s.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.ember.server.EmberServerBuilder

import features.text.TextController
import features.guest.GuestController
import features.pet.PetController
import shared.BackendException.ServerInternalErrorException

object BackendServerConf:
  def start: IO[Unit] =
    for
      port <- IO.fromOption(Port.fromInt(EnvLoaderConf.backendPort))(
        ServerInternalErrorException(s"Not processable port number ${EnvLoaderConf.backendPort}.")
      )
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0") // Accept connections from any available network interface
        .withPort(port) // On port 8080
        .withHttpApp(allRoutes.orNotFound)
        .build
        .use(_ => IO.never)
        .start
        .void
    yield ()

  private val docsEndpoint = // Merge all endpoints as a fully usable OpenAPI doc
    SwaggerInterpreter()
      .fromEndpoints[IO](
        TextController.endpoints ++ GuestController.endpoints ++ PetController.endpoints,
        "Backend",
        "1.0",
      )

  private val allRoutes = // Serve the OpenAPI doc & all the other routes
    Http4sServerInterpreter[IO]().toRoutes(
      docsEndpoint
    ) <+> TextController.routes <+> GuestController.routes <+> PetController.routes
