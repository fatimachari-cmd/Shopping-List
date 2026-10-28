package com.fatimachari.listadecompra.models

// Hemos creado unos propiedades del producto (id, nombre, quantidad, ...)
data class Product (
    val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val quantity: Double = 1.0,
    val unit: String = "unidad",
    val completed: Boolean = false
)
