package itmo.highload.repository

import itmo.highload.model.Animal
import itmo.highload.model.Customer
import itmo.highload.model.Ownership
import itmo.highload.model.OwnershipId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OwnershipRepository : JpaRepository<Ownership, OwnershipId> {
    fun findByCustomerAndAnimal(customer: Customer, animal: Animal): Ownership?
}
