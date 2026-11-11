package com.fatimachari.listadecompra.models

data class ListaCompra(
    val id: String,
    val nombre: String,
    val esFavorita: Boolean,
    val esMia: Boolean
)