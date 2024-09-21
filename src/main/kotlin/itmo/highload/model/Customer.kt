package itmo.highload.model

import itmo.highload.model.enums.Gender
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(name = "customers")
data class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Phone is mandatory")
    @Pattern(regexp = "^7[0-9]{10}\$", message = "Invalid phone number")
    val phone: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Address is mandatory")
    @Size(max = 50, message = "Address cannot exceed 50 characters")
    val address: String
)
