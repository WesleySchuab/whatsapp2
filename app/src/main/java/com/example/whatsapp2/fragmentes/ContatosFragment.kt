package com.example.whatsapp2.fragmentes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.whatsapp2.R
import com.example.whatsapp2.databinding.FragmentContatosBinding
import com.example.whatsapp2.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class ContatosFragment : Fragment() {
    private lateinit var binding: FragmentContatosBinding
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
        binding = FragmentContatosBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_contatos, container, false)
    }
    override fun onStart() {
        super.onStart()
        adicionarListenerContatos()
    }

    private fun adicionarListenerContatos() {
         eventoSnapshot = firestore
            .collection("usuarios")
            .addSnapshotListener { querySnapshot, erro ->

               val documento = querySnapshot?.documents

                documento?.forEach { documentSnapshot ->

                    val listaContatos = mutableListOf<Usuario>()
                    val usuario = documentSnapshot.toObject(Usuario::class.java)

                    if(usuario != null){
                        if(usuario.id != firebaseAuth.currentUser?.uid){
                            Log.i("ContatosFragment", "adicionarListenerContatos: ${usuario.nome} ")
                            val idUsuario = firebaseAuth.currentUser?.uid
                            if (idUsuario != null) {
                                listaContatos.add(usuario)
                                //binding.recyclerContatos.adapter = ContatosAdapter(listaContatos)
                            }
                        }
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }
}

