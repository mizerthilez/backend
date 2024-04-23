package dev.ironduck.backend

import cats.effect.*
import config.BackendServerConf
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

import dev.ironduck.backend.config.EnvLoaderConf.*
import dev.ironduck.backend.shared.BackendException

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    for
      port <- IO.fromOption(backendPort.toIntOption.flatMap(Port.fromInt))(
        BackendException.ServerInternalErrorException(
          s"Not processable port number ${backendPort}."
        )
      )
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0") // Accept connections from any available network interface
        .withPort(port) // default port 8080
        .withHttpApp(Router("/" -> BackendServerConf.allRoutes).orNotFound)
        .build
        .use: server =>
          for
            _ <- IO.println(
              s"Go to http://localhost:${server.address.getPort}/docs to open SwaggerUI. Press ENTER key to exit."
            )
            _ <- IO.readLine
          yield ()
    yield ()
