package gateway

import cats.MonadThrow
import gateway.models.CustomerDetails
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

object CustomerDetailsRoutes {

  def customerDetailsRoutes[F[_]: MonadThrow]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / id =>
      Ok(CustomerDetails(s"Some data about user $id"))
    }
  }

}
