package itmo.highload.controller

import itmo.highload.dto.AnimalDto
import itmo.highload.dto.AnimalUpdateDto
import itmo.highload.service.AnimalService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/animals")
class AnimalController(val animalService: AnimalService) {

    // TODO пагинация и бесконечная прокрутка
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAllAnimals() {

    }

    @GetMapping("/{animalId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADOPTION_MANAGER', 'CUSTOMER')")
    fun getAnimal(@PathVariable animalId: Int): Animal {
        return animalService.get(animalId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun addAnimal(@RequestBody @Valid request: AnimalDto): Animal {
        return animalService.save(request)
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun updateAnimal(@RequestBody @Valid request: AnimalUpdateDto) {
        return animalService.update(request)
    }

    @DeleteMapping("/{animalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADOPTION_MANAGER')")
    fun deleteAnimal(@PathVariable animalId: Int) {
        return animalService.delete(animalId)
    }

}