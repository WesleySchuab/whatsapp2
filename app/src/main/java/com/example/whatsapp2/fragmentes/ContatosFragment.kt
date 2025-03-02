package com.example.whatsapp2.fragmentes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp2.R
import com.example.whatsapp2.adapters.ContatosAdapter
import com.example.whatsapp2.databinding.FragmentContatosBinding
import com.example.whatsapp2.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class ContatosFragment : Fragment() {
    private lateinit var binding: FragmentContatosBinding
    private lateinit var eventoSnapshot: ListenerRegistration
    // Cria um objeto ContatosAdapter
    private lateinit var contatosAdapter: ContatosAdapter

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

        //Instancia o adapter
        contatosAdapter = ContatosAdapter()
        // Configura o RecyclerView com o adapter
        binding.rvContatos.adapter = contatosAdapter
        // Configura o layout manager como um LinearLayoutManager
        binding.rvContatos.layoutManager = LinearLayoutManager(context)
        // Adiciona um divisor entre os itens do RecyclerView
        binding.rvContatos.addItemDecoration(
            DividerItemDecoration(
                context, LinearLayoutManager.VERTICAL
            )
        )
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
                val listaContatos = mutableListOf<Usuario>()

                documento?.forEach { documentSnapshot ->

                    val usuario = documentSnapshot.toObject(Usuario::class.java)
                    val idUsuario = firebaseAuth.currentUser?.uid

                    if (usuario != null && idUsuario != null) {
                        if (usuario.id != firebaseAuth.currentUser?.uid) {
                            Log.i("ContatosFragment", "adicionarListenerContatos: ${usuario.nome} ")
                            listaContatos.add(usuario)
                            //binding.recyclerContatos.adapter = ContatosAdapter(listaContatos)
                        }
                    }
                }
                // Passa a lista de contatos para o adapter
                if(listaContatos.isNotEmpty()){
                    contatosAdapter.adicionarLista(listaContatos)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }
}

