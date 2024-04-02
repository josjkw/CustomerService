package gateway

import cats.effect.Temporal
import gateway.models.CustomerDetailsLegacy
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

import scala.concurrent.duration.DurationInt

object CustomerDetailsLegacyRoutes {

  def customerDetailsLegacyRoutes[F[_]: Temporal]: HttpRoutes[F] = {
    val dsl         = new Http4sDsl[F] {}
    var failCounter = 2
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / id =>
      Temporal[F].delayBy(
        id.toInt match {
          case id if id <= 2 && id >= 1 =>
            if (failCounter == 0) {
              failCounter = 2
              Ok(CustomerDetailsLegacy(s"Some legacy data about user $id"))
            } else {
              failCounter = failCounter - 1
              NotFound()
            }
          case _ => NotFound()
        },
        3.seconds,
      )

    }
  }

}
