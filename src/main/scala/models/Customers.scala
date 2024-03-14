package models

import models.Domain.{Customer, CustomerId}

trait Customers[F[_]] {
  def create(customer: Customer): F[Unit]
  def createBatch(customers: Set[Customer]): F[Unit]
  def get(id: CustomerId): F[Option[Customer]]
}
