package repo

import cats.data.State
import models.Domain.{Customer, CustomerId}

object Repository {

  trait CustomerRepository {
    def add(customer: Customer): Unit
    def get(customerId: CustomerId): Option[Customer]
  }

  class InMemoryCustomerRepository extends CustomerRepository {

    private var inMemoryRepo: Map[CustomerId, Customer] = Map()

    override def add(customer: Customer): Unit =
      inMemoryRepo = inMemoryRepo + (customer.id -> customer)

    override def get(customerId: CustomerId): Option[Customer] = inMemoryRepo.get(customerId)
  }

}
