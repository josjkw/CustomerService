package infrastructure.modules

import cats.{Monad, MonadThrow}
import cats.effect.Async
import domain.repositories.CustomerDetailsRepository
import domain.services.CustomerDetailsService
import infrastructure.config.Configs.CustomerDetailsServiceConfig
import infrastructure.repositories.ClientDetailsHttpRepository
import org.http4s.client.Client

object CustomerDetailsModule {

  def customerDetailsRepository[F[_]: Async](
                                              httpConfig: CustomerDetailsServiceConfig,
                                              httpClient: Client[F],
  ): F[ClientDetailsHttpRepository[F]] =
    Async[F].pure(new ClientDetailsHttpRepository[F](httpConfig, httpClient))

  def customerDetails[F[_]: MonadThrow](repository: CustomerDetailsRepository[F]): F[CustomerDetailsService[F]] =
    MonadThrow[F].pure(new CustomerDetailsService[F](repository))

}
