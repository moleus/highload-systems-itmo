package itmo.highload.infrastructure.postgres

import itmo.highload.domain.AnimalRepository
import itmo.highload.infrastructure.postgres.model.Animal
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface AnimalRepositoryImpl : R2dbcRepository<Animal, Int>, AnimalRepository {
    override fun findByIdNotIn(ids: List<Int>): Flux<Animal>
    override fun findByNameAndIdNotIn(name: String, ids: List<Int>): Flux<Animal>
}
