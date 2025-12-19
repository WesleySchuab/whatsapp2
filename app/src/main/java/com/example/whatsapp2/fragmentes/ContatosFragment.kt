package com.example.whatsapp2.fragmentes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp2.activities.AdicionarContatoHelper
import com.example.whatsapp2.activities.MensagensActivity
import com.example.whatsapp2.adapters.ContatosAdapter
import com.example.whatsapp2.databinding.FragmentContatosBinding
import com.example.whatsapp2.model.Usuario
import com.example.whatsapp2.utils.Constantes
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class ContatosFragment : Fragment() {
    private lateinit var binding: FragmentContatosBinding
    private lateinit var eventoSnapshot: ListenerRegistration
    private lateinit var contatosAdapter: ContatosAdapter
    private lateinit var adicionarContatoHelper: AdicionarContatoHelper

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContatosBinding.inflate(inflater, container, false)
        
        adicionarContatoHelper = AdicionarContatoHelper(requireActivity() as androidx.appcompat.app.AppCompatActivity)
        
        contatosAdapter = ContatosAdapter(
            onClick = { usuario ->
                val intent = Intent(context, MensagensActivity::class.java)
                intent.putExtra("dadosDestinatario", usuario)
                intent.putExtra("origem", Constantes.ORIGEM_CONTATO)
                startActivity(intent)
            },
            onDelete = { usuario ->
                mostrarConfirmacaoExclusao(listOf(usuario))
            }
        )
        
        binding.rvContatos.adapter = contatosAdapter
        binding.rvContatos.layoutManager = LinearLayoutManager(context)
        binding.rvContatos.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        )
        
        // Configurar FAB para adicionar contato
        binding.fabAdicionarContato.setOnClickListener {
            adicionarContatoHelper.mostrarDialogAdicionarContato()
        }
        
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adicionarListenerContatos()
    }

    private fun adicionarListenerContatos() {
        val idUsuario = firebaseAuth.currentUser?.uid
        
        if (idUsuario == null) {
            Log.e("ContatosFragment", "Usuário não autenticado")
            return
        }
        
        eventoSnapshot = firestore
            .collection(Constantes.CONTATOS)
            .document(idUsuario)
            .collection("meus_contatos")
            .addSnapshotListener { querySnapshot, erro ->
                if (erro != null) {
                    Log.e("ContatosFragment", "Erro: ${erro.message}")
                    return@addSnapshotListener
                }

                val listaContatos = mutableListOf<Usuario>()

                querySnapshot?.documents?.forEach { documentSnapshot ->
                    val usuario = Usuario(
                        id = documentSnapshot.getString("id") ?: "",
                        nome = documentSnapshot.getString("nome") ?: "",
                        email = documentSnapshot.getString("email") ?: "",
                        foto = documentSnapshot.getString("foto") ?: ""
                    )
                    
                    if (usuario.id.isNotEmpty()) {
                        listaContatos.add(usuario)
                    }
                }
                
                contatosAdapter.adicionarLista(listaContatos)
            }
    }

    private fun mostrarConfirmacaoExclusao(contatos: List<Usuario>) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirmar exclusão")
            .setMessage("Tem certeza que deseja excluir ${contatos.size} contato(s)?")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Excluir") { dialog, _ ->
                excluirContatos(contatos)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun excluirContatos(contatos: List<Usuario>) {
        val idUsuario = firebaseAuth.currentUser?.uid ?: return
        
        contatos.forEach { usuario ->
            firestore
                .collection(Constantes.CONTATOS)
                .document(idUsuario)
                .collection("meus_contatos")
                .document(usuario.id)
                .delete()
                .addOnFailureListener { erro ->
                    Log.e("ContatosFragment", "Erro ao excluir: ${erro.message}")
                }
        }
        
        exibirMensagem("${contatos.size} contato(s) excluído(s)")
    }

    private fun exibirMensagem(mensagem: String) {
        requireActivity().exibirMensagem(mensagem)
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }
}

