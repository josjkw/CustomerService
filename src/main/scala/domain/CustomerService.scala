package domain

import domain.CustomerRepository.CustomerRepository

class CustomerService[F[_]](repository: CustomerRepository[F]) {
  def create(customer: Customer): F[Unit]                 = repository.add(customer)
  def createBatch(customers: Set[Customer]): Set[F[Unit]] = customers.map(repository.add)
  def get(id: CustomerId): F[Option[Customer]]            = repository.get(id)
}
