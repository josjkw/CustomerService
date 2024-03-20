package domain

import cats.effect.Async
import cats.effect.kernel.{Concurrent, Resource}
import cats.implicits.toFunctorOps
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

import scala.concurrent.ExecutionContext.global

class HttpClient[F[_]: Async](port: Int, host: String)(client: Client[F]) {

  private val root = Uri.unsafeFromString(s"http://$host:$port/internal")

  def getClientDetails(id: String) = client.expect[CustomerDetails](root / id).map { a =>
    val _ = println("ke sios là connard")
    a
  }

}

object HttpClient {

  def apply[F[_]: Async](port: Int, host: String): Resource[F, HttpClient[F]] =
    BlazeClientBuilder[F].resource.map(client => new HttpClient[F](port, host)(client))

}
