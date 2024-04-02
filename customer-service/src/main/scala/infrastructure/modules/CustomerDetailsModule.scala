package infrastructure.modules

import cats.MonadThrow
import cats.effect.Async
import domain.repositories.CustomerDetailsRepository
import domain.services.CustomerDetailsService
import infrastructure.config.Configs.CustomerDetailsServiceConfig
import infrastructure.repositories.CustomerDetailsHttpRepository
import org.http4s.client.Client

object CustomerDetailsModule {

  def customerDetailsRepository[F[_]: Async](
      httpConfig: CustomerDetailsServiceConfig,
      httpClient: Client[F],
  ): F[CustomerDetailsHttpRepository[F]] =
    Async[F].pure(new CustomerDetailsHttpRepository[F](httpConfig, httpClient))

  def customerDetails[F[_]: MonadThrow](
      repository: CustomerDetailsRepository[F]
  ): F[CustomerDetailsService[F]] =
    MonadThrow[F].pure(new CustomerDetailsService[F](repository))

}
