@file:Suppress("UnusedParameter", "CommentWrapping")

package itmo.highload.controller

import itmo.highload.dto.AnimalDto
import itmo.highload.dto.response.AnimalResponse
import itmo.highload.service.AnimalService
import jakarta.validation.Valid
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
    fun getAllAnimalsPage(pageable: Pageable): List<AnimalResponse> {
        return listOf()
    }

    @GetMapping("/scroll")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimalsInfiniteScroll(
        @RequestParam(value = "offset", defaultValue = "0") offset: Int,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int
    ): List<AnimalResponse> {
        return listOf()
    }

    @GetMapping("/{animalId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAnimal(@PathVariable animalId: Int): AnimalResponse {
        return animalService.get(animalId)
    }

    @GetMapping("/health-statuses")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun getAllHealthStatuses(pageable: Pageable)/*: List<HealthStatus>*/ {
        return
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun addAnimal(@RequestBody @Valid request: AnimalDto): AnimalResponse {
        return animalService.save(request)
    }

    @PutMapping("/{animalId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAnimal(
        @PathVariable animalId: Int,
        @RequestBody @Valid request: AnimalDto
    ): AnimalResponse {
        return animalService.update(animalId, request)
    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun deleteAnimal(@PathVariable animalId: Int) {
        animalService.delete(animalId)
    }
}
