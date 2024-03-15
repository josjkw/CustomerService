package domain

object CustomerRepository {

  trait CustomerRepository[F[_]] {
    def add(customer: Customer): F[Unit]
    def get(customerId: CustomerId): F[Option[Customer]]
  }

}
