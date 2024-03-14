package models

import cats.Id
import models.Domain.{Customer, CustomerId}
import repo.Repository.CustomerRepository

class CustomersInterpreter(repository: CustomerRepository) extends Customers[Id] {
  override def create(customer: Customer): Id[Unit] = repository.add(customer)

  override def createBatch(customers: Set[Customer]): Id[Unit] = customers.map(repository.add)

  override def get(id: CustomerId): Id[Option[Customer]] = repository.get(id)
}
