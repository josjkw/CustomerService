package infrastructure.modules

import cats.effect.{Async, IO}
import org.http4s.client.JavaNetClientBuilder

object HttpClientModule {

  def getHttpClient[F[_]: Async] = Async[F].pure(JavaNetClientBuilder[F].create)

}
