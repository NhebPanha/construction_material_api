package org.example.construction_material_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ConstructionMaterialApiApplication

fun main(args: Array<String>) {
    runApplication<ConstructionMaterialApiApplication>(*args)
}
