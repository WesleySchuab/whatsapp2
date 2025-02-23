package com.example.whatsapp2.utils

import android.app.Activity
import android.widget.Toast

fun Activity.exibirMensagem(mensagem: String) {
    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()

}