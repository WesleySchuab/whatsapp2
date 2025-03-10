package com.example.whatsapp2.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Conversa(
    val idusuarioRemetente: String ="",
    val idUsuarioDestinatario: String ="",
    val foto: String ="",
    val nome: String ="",
    val ultimaMensagem: String ="",
    @ServerTimestamp
    val data: Date? = null,
)
