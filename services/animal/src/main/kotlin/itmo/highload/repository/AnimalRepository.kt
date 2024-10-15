package itmo.highload.repository

import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import itmo.highload.model.Animal
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AnimalRepository : R2dbcRepository<Animal, Int> {
    fun findByTypeOfAnimal(typeOfAnimal: String): Mono<Animal>
    fun findByName(name: String): Mono<Animal>
    fun findByHealthStatus(healthStatus: HealthStatus, pageable: Pageable): Flux<Animal>
    fun findByGender(gender: Gender): Mono<Animal>
}

