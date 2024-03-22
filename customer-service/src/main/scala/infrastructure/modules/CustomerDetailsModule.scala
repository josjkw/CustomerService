package infrastructure.modules

import cats.MonadThrow
import cats.effect.Async
import domain.repositories.CustomerDetailsRepository
import domain.services.CustomerDetailsService
import infrastructure.config.Configs.CustomerDetailsServiceConfig
import infrastructure.repositories.{CustomerDetailsHttpRepository, CustomerDetailsLegacyHttpRepository}
import org.http4s.client.Client

object CustomerDetailsModule {

  def customerDetailsRepository[F[_]: Async](
      httpConfig: CustomerDetailsServiceConfig,
      httpClient: Client[F],
  ): F[CustomerDetailsHttpRepository[F]] =
    Async[F].pure(new CustomerDetailsHttpRepository[F](httpConfig, httpClient))

  def customerDetailsLegacyRepository[F[_]: Async](
      httpConfig: CustomerDetailsServiceConfig,
      httpClient: Client[F],
  ): F[CustomerDetailsLegacyHttpRepository[F]] =
    Async[F].pure(new CustomerDetailsLegacyHttpRepository[F](httpConfig, httpClient))

  def customerDetails[F[_]: MonadThrow](
      executionPriority: Int,
      repository: CustomerDetailsRepository[F],
  ): F[CustomerDetailsService[F]] =
    MonadThrow[F].pure(new CustomerDetailsService[F](executionPriority, repository))

}
