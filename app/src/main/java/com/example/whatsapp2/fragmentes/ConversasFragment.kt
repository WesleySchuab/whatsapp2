package com.example.whatsapp2.fragmentes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp2.activities.MensagensActivity
import com.example.whatsapp2.adapters.UltimasConversasAdapter
import com.example.whatsapp2.databinding.FragmentConversasBinding
import com.example.whatsapp2.model.Conversa
import com.example.whatsapp2.model.Usuario
import com.example.whatsapp2.utils.Constantes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ConversasFragment : Fragment() {

    private var binding: FragmentConversasBinding? = null
    private var listenerRegistration: ListenerRegistration? = null
    private lateinit var conversasAdapter: UltimasConversasAdapter

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentConversasBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        conversasAdapter = UltimasConversasAdapter(
            onClick = { conversa ->
                val usuario = Usuario(
                    id = conversa.idUsuarioDestinatario,
                    nome = conversa.nome,
                    foto = conversa.foto
                )
                val intent = Intent(context, MensagensActivity::class.java).apply {
                    putExtra("dadosDestinatario", usuario)
                    putExtra("origem", Constantes.ORIGEM_CONVERSA)
                }
                startActivity(intent)
            },
            onDelete = { conversa ->
                confirmarExclusaoConversa(conversa)
            }
        )

        fragmentBinding.rvConversas.apply {
            adapter = conversasAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }

        return fragmentBinding.root
    }

    override fun onStart() {
        super.onStart()
        adicionarListenerConversas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        binding = null
    }

    private fun confirmarExclusaoConversa(conversa: Conversa) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir conversa")
            .setMessage("Excluir a conversa com ${conversa.nome}?")
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Excluir") { dialog, _ ->
                excluirConversa(conversa)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun excluirConversa(conversa: Conversa) {
        val idUsuario = firebaseAuth.currentUser?.uid ?: return
        val idDestinatario = conversa.idUsuarioDestinatario

        // Remove a última conversa da lista do usuário atual
        firestore.collection(Constantes.CONVERSAS)
            .document(idUsuario)
            .collection(Constantes.ULTIMAS_CONVERSAS)
            .document(idDestinatario)
            .delete()

        // Remove as mensagens que EU enviei para o outro usuário
        firestore.collection(Constantes.MENSAGENS)
            .document(idUsuario)
            .collection(idDestinatario)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot?.documents?.forEach { it.reference.delete() }
            }

        // Remove as mensagens que o outro usuário enviou para MIM
        firestore.collection(Constantes.MENSAGENS)
            .document(idDestinatario)
            .collection(idUsuario)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot?.documents?.forEach { it.reference.delete() }
            }
    }

    private fun adicionarListenerConversas() {
        val idUsuario = firebaseAuth.currentUser?.uid ?: return

        listenerRegistration?.remove()
        listenerRegistration = firestore
            .collection(Constantes.CONVERSAS)
            .document(idUsuario)
            .collection(Constantes.ULTIMAS_CONVERSAS)
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, erro ->
                if (erro != null) {
                    return@addSnapshotListener
                }

                val listaConversas = querySnapshot?.documents?.mapNotNull { documentSnapshot ->
                    documentSnapshot.toObject(Conversa::class.java)
                } ?: emptyList()

                conversasAdapter.atualizarLista(listaConversas)
            }
    }
}