package infrastructure.modules

import cats.MonadThrow
import cats.effect.IO
import domain.{Customer, CustomerId}
import domain.repositories.CustomerRepository.CustomerRepository
import domain.services.{CustomerDetailsService, CustomerService}
import infrastructure.repositories.InMemoryCustomerRepository

object CustomerModule {

  def customerInMemoryRepository[F[_]: MonadThrow](map: Map[CustomerId, Customer]): F[CustomerRepository[F]] =
    MonadThrow[F].pure(new InMemoryCustomerRepository[F](map))

  def customerService[F[_]: MonadThrow](
      customerRepository: CustomerRepository[F],
      customerDetailsService: CustomerDetailsService[F],
  ): F[CustomerService[F]] =
    MonadThrow[F].pure(new CustomerService[F](customerRepository)(customerDetailsService))

}
