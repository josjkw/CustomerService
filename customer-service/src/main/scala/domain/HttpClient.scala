package domain

import cats.effect.Async
import io.circe.generic.auto._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.{Client, JavaNetClientBuilder}

class HttpClient[F[_]: Async](port: Int, host: String)(client: Client[F]) {

  private val root                                     = Uri.unsafeFromString(s"http://$host:$port/internal")
  def getClientDetails(id: String): F[CustomerDetails] = client.expect[CustomerDetails](root / id)

}

object HttpClient {

  def apply[F[_]: Async](port: Int, host: String): HttpClient[F] =
    new HttpClient(port, host)(JavaNetClientBuilder[F].create)

}
