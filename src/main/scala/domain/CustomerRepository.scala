package domain

import cats.MonadThrow

object CustomerRepository {

  trait CustomerRepository[F[_]] {
    def add(customer: Customer): F[Unit]
    def addBatch(customers: Set[Customer]): F[Unit]
    def get(customerId: CustomerId): F[Option[Customer]]
  }

}
