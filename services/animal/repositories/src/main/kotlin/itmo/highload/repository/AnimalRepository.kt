package itmo.highload.repository

import itmo.highload.model.Animal
import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AnimalRepository : JpaRepository<Animal, Int> {
    fun findByTypeOfAnimal(typeOfAnimal: String, pageable: Pageable): Page<Animal>
    fun findByName(name: String, pageable: Pageable): Page<Animal>
    fun findByHealthStatus(healthStatus: HealthStatus, pageable: Pageable): Page<Animal>
    fun findByGender(gender: Gender, pageable: Pageable): Page<Animal>

    @Query("SELECT DISTINCT a.healthStatus FROM Animal a")
    fun findAllUniqueHealthStatuses(pageable: Pageable): Page<HealthStatus>
}

