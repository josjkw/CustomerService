package domain

import cats.MonadThrow
import domain.CustomerRepository.CustomerRepository

class CustomerService[F[_]: MonadThrow](repository: CustomerRepository[F]) {
  def create(customer: Customer): F[Unit]            = repository.add(customer)
  def createBatch(customers: Set[Customer]): F[Unit] = repository.addBatch(customers)
  def get(id: CustomerId): F[Option[Customer]]       = repository.get(id)
}
