package itmo.highload.controller

import itmo.highload.api.dto.AnimalDto
import itmo.highload.api.dto.response.AnimalResponse
import itmo.highload.model.AnimalMapper
import itmo.highload.service.AnimalService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${app.base-url}/animals")
class AnimalController(val animalService: AnimalService) {
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAll(
        @RequestParam(required = false) name: String?, pageable: Pageable
    ): List<AnimalResponse> {
        return animalService.getAll(name, pageable).map { AnimalMapper.toAnimalResponse(it) }.content
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAnimal(@PathVariable id: Int): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.getById(id))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun addAnimal(@RequestBody @Valid request: AnimalDto): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.save(request))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAnimal(
        @PathVariable id: Int, @RequestBody @Valid request: AnimalDto
    ): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun deleteAnimal(@PathVariable id: Int) {
        animalService.delete(id)
    }
}
