package itmo.highload.model

import itmo.highload.infrastructure.postgres.model.AnimalToImageKey
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class AnimalToImageKeyTest {

    @Test
    fun `should correctly initialize AnimalToImageKey with default values`() {
        val key = AnimalToImageKey()

        assertThat(key.animalId).isEqualTo(0)
        assertThat(key.imageId).isEqualTo(0)
    }

    @Test
    fun `should correctly initialize AnimalToImageKey with given values`() {
        val key = AnimalToImageKey(animalId = 1, imageId = 42)

        assertThat(key.animalId).isEqualTo(1)
        assertThat(key.imageId).isEqualTo(42)
    }

    @Test
    fun `should check equality for two equal AnimalToImageKey objects`() {
        val key1 = AnimalToImageKey(1, 42)
        val key2 = AnimalToImageKey(1, 42)

        assertThat(key1).isEqualTo(key2)
    }

    @Test
    fun `should check equality for two different AnimalToImageKey objects`() {
        val key1 = AnimalToImageKey(1, 42)
        val key2 = AnimalToImageKey(2, 43)

        assertThat(key1).isNotEqualTo(key2)
    }

    @Test
    fun `should generate correct hashCode`() {
        val key1 = AnimalToImageKey(1, 42)
        val key2 = AnimalToImageKey(1, 42)

        assertThat(key1.hashCode()).isEqualTo(key2.hashCode())
    }
}
