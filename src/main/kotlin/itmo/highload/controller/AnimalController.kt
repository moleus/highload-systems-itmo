package itmo.highload.controller

import itmo.highload.dto.AnimalDto
import itmo.highload.dto.response.AnimalResponse
import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import itmo.highload.service.AnimalService
import itmo.highload.service.mapper.AnimalMapper
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/animals")
class AnimalController(val animalService: AnimalService) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimalsPage(pageable: Pageable): Page<AnimalResponse> {
        return animalService.getAll(pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    @GetMapping("/scroll")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimalsInfiniteScroll(
        @RequestParam(value = "offset", defaultValue = "0") offset: Int,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int
    ): Page<AnimalResponse> {
        val pageable = Pageable.ofSize(limit).withPage(offset / limit)
        return animalService.getAll(pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    @GetMapping("/{animalId}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAnimal(@PathVariable animalId: Int): AnimalResponse {
        val animal = animalService.get(animalId)
        return AnimalMapper.toAnimalResponse(animal)
    }

    @GetMapping("/health-statuses")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getAllHealthStatuses(): List<HealthStatus> {
        return animalService.getAllHealthStatus()
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun addAnimal(@RequestBody @Valid request: AnimalDto): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.save(request))
    }

    @PutMapping("/{animalId}")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAnimal(
        @PathVariable animalId: Int,
        @RequestBody @Valid request: AnimalDto
    ): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.update(animalId, request))
    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun deleteAnimal(@PathVariable animalId: Int) {
        animalService.delete(animalId)
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimalsByType(
        @PathVariable type: String,
        pageable: Pageable
    ): Page<AnimalResponse> {
        return animalService.getAllByType(type, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimalsByName(
        @PathVariable name: String,
        pageable: Pageable
    ): Page<AnimalResponse> {
        return animalService.getAllByName(name, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    @GetMapping("/health-status/{healthStatus}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimalsByHealthStatus(
        @PathVariable healthStatus: HealthStatus,
        pageable: Pageable
    ): Page<AnimalResponse> {
        return animalService.getAllByHealthStatus(healthStatus, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }

    @GetMapping("/gender/{gender}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimalsByGender(
        @PathVariable gender: Gender,
        pageable: Pageable
    ): Page<AnimalResponse> {
        return animalService.getAllByGender(gender, pageable)
            .map { AnimalMapper.toAnimalResponse(it) }
    }
}
