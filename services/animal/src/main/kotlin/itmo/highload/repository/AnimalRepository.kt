package itmo.highload.repository

import itmo.highload.model.Animal
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface AnimalRepository : R2dbcRepository<Animal, Int> {
    fun findByIdNotIn(ids: List<Int>): Flux<Animal>
    fun findByNameAndIdNotIn(name: String, ids: List<Int>): Flux<Animal>
}
