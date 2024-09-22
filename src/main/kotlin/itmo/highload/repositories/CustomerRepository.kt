package itmo.highload.repositories

import itmo.highload.model.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : JpaRepository<Customer, Int> {
    fun findByPhone(phone: String): Customer?
}
