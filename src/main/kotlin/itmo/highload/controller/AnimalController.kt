package itmo.highload.controller

import itmo.highload.dto.AnimalDto
import itmo.highload.dto.response.AnimalResponse
import itmo.highload.mapper.AnimalMapper
import itmo.highload.service.AnimalService
import itmo.highload.utils.PaginationResponseHelper
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("\${app.base-url}/animals")
class AnimalController(val animalService: AnimalService) {
    @GetMapping
    fun getAll(
        @RequestParam(required = false) name: String?, pageable: Pageable
    ): List<AnimalResponse> {
        val limitedPageable = PaginationResponseHelper.limitPageSize(pageable)
        return animalService.getAll(name, limitedPageable).map { AnimalMapper.toAnimalResponse(it) }.content
    }

    @GetMapping("/{id}")
    fun getAnimal(@PathVariable id: Int): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.getById(id))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addAnimal(@RequestBody @Valid request: AnimalDto): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.save(request))
    }

    @PutMapping("/{id}")
    fun updateAnimal(
        @PathVariable id: Int, @RequestBody @Valid request: AnimalDto
    ): AnimalResponse {
        return AnimalMapper.toAnimalResponse(animalService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAnimal(@PathVariable id: Int) {
        animalService.delete(id)
    }
}
