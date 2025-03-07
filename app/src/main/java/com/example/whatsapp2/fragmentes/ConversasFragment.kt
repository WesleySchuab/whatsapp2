package com.example.whatsapp2.fragmentes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.whatsapp2.databinding.FragmentConversasBinding
import com.example.whatsapp2.model.Conversa
import com.example.whatsapp2.utils.Constantes
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class ConversasFragment : Fragment() {

    private lateinit var binding: FragmentConversasBinding
    private lateinit var eventoSnapshot: ListenerRegistration

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentConversasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adicionarListenerConversas()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()

    }

    private fun adicionarListenerConversas() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        if (idUsuarioRemetente != null) {
           // activity?.exibirMensagem("Usuário encontrado")
            eventoSnapshot = firestore
                .collection(Constantes.CONVERSAS)
                .document(idUsuarioRemetente)
                .collection(Constantes.ULTIMAS_CONVERSAS)
                .addSnapshotListener { querySnapshot, erro ->
                    if (erro != null) {
                        activity?.exibirMensagem("Erro ao carregar as conversas")
                    }
                    val listaConversas = mutableListOf<Conversa>()
                    val documento = querySnapshot?.documents
                    documento?.forEach { documentSnapshot ->
                        val conversa = documentSnapshot.toObject(Conversa::class.java)
                        if (conversa != null) {
                            listaConversas.add(conversa)
                            Log.i(
                                "listaConversas",
                                "Conversas ${conversa.nome} - ${conversa.ultimaMensagem} : "
                            )
                        }
                    }
                    if (listaConversas.isNotEmpty()) {
                        // Atualiza o adapter com a lista de conversas

                    }
                }
        }else{
            activity?.exibirMensagem("Usuário não encontrado")

        }
    }


}