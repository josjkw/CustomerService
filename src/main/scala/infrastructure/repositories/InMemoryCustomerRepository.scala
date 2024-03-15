package infrastructure.repositories

import cats.effect.IO
import domain.{Customer, CustomerId}
import domain.CustomerRepository.CustomerRepository

class InMemoryCustomerRepository extends CustomerRepository[IO] {

  private var inMemoryRepo: Map[CustomerId, Customer] = Map()

  override def add(customer: Customer): IO[Unit] = {
    if (inMemoryRepo.contains(customer.id)) {
      IO.raiseError(new Exception("Customer already exists"))
    } else {
      inMemoryRepo = inMemoryRepo + (customer.id -> customer)
      IO.unit
    }

  }

  override def get(customerId: CustomerId): IO[Option[Customer]] = IO.pure(inMemoryRepo.get(customerId))
}
