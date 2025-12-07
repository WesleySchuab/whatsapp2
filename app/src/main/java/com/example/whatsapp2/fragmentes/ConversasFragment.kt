package com.example.whatsapp2.fragmentes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        conversasAdapter = UltimasConversasAdapter { conversa ->
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
        }

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