package domain.repositories

import cats.data.OptionT
import domain.{Customer, CustomerId}

object CustomerRepository {

  trait CustomerRepository[F[_]] {
    def add(customer: Customer): F[Unit]
    def addBatch(customers: Set[Customer]): F[Unit]
    def get(customerId: CustomerId): OptionT[F, Customer]
  }

}
