package infrastructure.repositories

import cats.effect.IO
import cats.implicits.{catsSyntaxNestedFoldable, catsSyntaxParallelSequence_, toTraverseOps}
import domain.{Customer, CustomerId}
import domain.CustomerRepository.CustomerRepository

class InMemoryCustomerRepository extends CustomerRepository[IO] {

  private var inMemoryRepo: Map[CustomerId, Customer] = Map()

  override def add(customer: Customer): IO[Unit] = {
    if (inMemoryRepo.contains(customer.id)) {
      IO.raiseError(new Exception(s"Customer ${customer.name} already exists"))
    } else {
      inMemoryRepo = inMemoryRepo + (customer.id -> customer)
      IO.unit
    }

  }

  override def get(customerId: CustomerId): IO[Option[Customer]] = IO.pure(inMemoryRepo.get(customerId))

  override def addBatch(customers: Set[Customer]): IO[Unit] = {
    customers.toList.traverse { add(_).attempt }.flatMap { results =>
      results.collect { case Left(error) => error } match {
        case Nil    => IO.unit
        case errors => IO.raiseError(new Exception(errors.map(_.getMessage).mkString("; ")))
      }
    }
  }

}
