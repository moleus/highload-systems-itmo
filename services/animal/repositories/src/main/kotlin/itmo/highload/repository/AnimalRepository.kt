package itmo.highload.repository

import itmo.highload.model.Animal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AnimalRepository : JpaRepository<Animal, Int> {
    fun findByIdNotIn(ids: List<Int>, pageable: Pageable): Page<Animal>
    fun findByNameAndIdNotIn(name: String, ids: List<Int>, pageable: Pageable): Page<Animal>
}
