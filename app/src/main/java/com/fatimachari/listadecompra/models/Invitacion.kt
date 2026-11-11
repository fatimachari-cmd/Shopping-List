package com.fatimachari.listadecompra.models

// Una invitación para unirse a una lista de otra persona
data class Invitacion(
    val id: String,
    val listId: String,
    val nombreLista: String,
    val deQuien: String
)