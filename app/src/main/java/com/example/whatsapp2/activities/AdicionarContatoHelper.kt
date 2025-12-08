package com.example.whatsapp2.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsapp2.databinding.DialogAdicionarContatoBinding
import com.example.whatsapp2.model.Usuario
import com.example.whatsapp2.utils.Constantes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdicionarContatoHelper(private val activity: AppCompatActivity) {
    
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun mostrarDialogAdicionarContato() {
        val binding = DialogAdicionarContatoBinding.inflate(LayoutInflater.from(activity))
        
        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .create()

        binding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnAdicionar.setOnClickListener {
            val email = binding.editEmailContato.text.toString().trim()
            
            if (email.isEmpty()) {
                Toast.makeText(activity, "Digite um e-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            buscarEAdicionarContato(email) { sucesso ->
                if (sucesso) {
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun buscarEAdicionarContato(email: String, callback: (Boolean) -> Unit) {
        val idUsuarioAtual = firebaseAuth.currentUser?.uid
        
        if (idUsuarioAtual == null) {
            Toast.makeText(activity, "Erro ao obter usuário atual", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        // Buscar usuário pelo e-mail
        firestore.collection(Constantes.USUARIOS)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(activity, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                    callback(false)
                    return@addOnSuccessListener
                }

                val documentSnapshot = querySnapshot.documents[0]
                val usuario = documentSnapshot.toObject(Usuario::class.java)

                if (usuario == null) {
                    Toast.makeText(activity, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show()
                    callback(false)
                    return@addOnSuccessListener
                }

                if (usuario.id == idUsuarioAtual) {
                    Toast.makeText(activity, "Você não pode adicionar a si mesmo", Toast.LENGTH_SHORT).show()
                    callback(false)
                    return@addOnSuccessListener
                }

                // Verificar se já é contato
                firestore.collection(Constantes.CONTATOS)
                    .document(idUsuarioAtual)
                    .collection("meus_contatos")
                    .document(usuario.id)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            Toast.makeText(activity, "Contato já adicionado", Toast.LENGTH_SHORT).show()
                            callback(false)
                        } else {
                            // Adicionar contato
                            salvarContato(idUsuarioAtual, usuario, callback)
                        }
                    }
                    .addOnFailureListener { erro ->
                        Toast.makeText(activity, "Erro ao verificar contato: ${erro.message}", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
            }
            .addOnFailureListener { erro ->
                Toast.makeText(activity, "Erro ao buscar usuário: ${erro.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun salvarContato(idUsuarioAtual: String, contato: Usuario, callback: (Boolean) -> Unit) {
        val contatoData = hashMapOf(
            "id" to contato.id,
            "nome" to contato.nome,
            "email" to contato.email,
            "foto" to contato.foto,
            "adicionadoEm" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        firestore.collection(Constantes.CONTATOS)
            .document(idUsuarioAtual)
            .collection("meus_contatos")
            .document(contato.id)
            .set(contatoData)
            .addOnSuccessListener {
                Toast.makeText(activity, "Contato adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { erro ->
                Toast.makeText(activity, "Erro ao adicionar contato: ${erro.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }
}
