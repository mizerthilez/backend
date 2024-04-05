package dev.ironduck.backend

import cats.effect.*
import config.BackendServerConf

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    BackendServerConf.start >> IO.never
