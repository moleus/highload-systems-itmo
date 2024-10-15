package itmo.highload.utils

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification

fun defaultJsonRequestSpec(): RequestSpecification = RestAssured.given().contentType(ContentType.JSON)

fun RequestSpecification.withJwt(token: String): RequestSpecification = this.header("Authorization", "Bearer $token")
