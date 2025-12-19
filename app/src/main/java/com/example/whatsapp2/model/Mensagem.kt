package com.example.whatsapp2.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Mensagem(
    val idUsuario: String = "",
    val mensagem: String = "",
    var idMensagem: String = "",
    @ServerTimestamp
    val data: Date? = null,
)
