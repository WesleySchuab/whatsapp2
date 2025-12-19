package com.example.whatsapp2.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp2.R
import com.example.whatsapp2.adapters.MensagensAdapter
import com.example.whatsapp2.databinding.ActivityMensagensBinding
import com.google.firebase.firestore.QuerySnapshot
import com.example.whatsapp2.model.Conversa
import com.example.whatsapp2.model.Mensagem
import com.example.whatsapp2.model.Usuario
import com.example.whatsapp2.utils.Constantes
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.squareup.picasso.Picasso

class MensagensActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMensagensBinding.inflate(layoutInflater)
    }
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private var listenerRegistration: ListenerRegistration? = null

    private var dadosDestinatario: Usuario? = null
    private var dadosUsuarioRemetente: Usuario? = null

    private lateinit var mensagensAdapter: MensagensAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        recuperarDadosUsuarios()
        inicializarToolbar()
        inicializarEventosDeClique()
        inicializarRecyclerView()
        // Atrasar a inicialização de listeners para garantir que dadosDestinatario esteja disponível
        binding.root.postDelayed({
            inicializarListeners()
        }, 500)
    }

    private fun inicializarRecyclerView() {
        with(binding) {
            mensagensAdapter = MensagensAdapter { mensagem ->
                excluirMensagem(mensagem)
            }
            rvMensagens.adapter = mensagensAdapter
            rvMensagens.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun inicializarListeners() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id

        if (idUsuarioRemetente != null && idUsuarioDestinatario != null) {
            // Carga inicial direta do servidor para garantir sincronização
            firestore
                .collection(Constantes.MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario)
                .orderBy("data", Query.Direction.ASCENDING)
                .get(Source.SERVER)
                .addOnSuccessListener { querySnapshot ->
                    val lista = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Mensagem::class.java)?.apply {
                            if (idMensagem.isBlank()) idMensagem = doc.id
                        }
                    }.toMutableList()
                    mensagensAdapter.adicionarLista(lista)
                }

            listenerRegistration = firestore
                .collection(Constantes.MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario)
                .orderBy("data", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        exibirMensagem("Erro ao carregar mensagens")
                    }

                    val listaMensagens = mutableListOf<Mensagem>()
                    val documentos = querySnapshot?.documents
                    documentos?.forEach { documentSnapshot ->
                        val mensagem = documentSnapshot.toObject(Mensagem::class.java)
                        if (mensagem != null) {
                            if (mensagem.idMensagem.isBlank()) {
                                mensagem.idMensagem = documentSnapshot.id
                            }
                            listaMensagens.add(mensagem)
                            Log.i("ListenerMensagem", mensagem.mensagem)
                        }
                    }
                    mensagensAdapter.adicionarLista(listaMensagens)
                }
        }
    }

    private fun inicializarEventosDeClique() {
        binding.fabEnviar.setOnClickListener {
            // Enviar mensagem
            val mensagem = binding.editMensagem.text.toString()
            if (mensagem.isNotEmpty()) {
                salvarMensagem(mensagem)
            }
        }
    }

    private fun salvarMensagem(textoMensagem: String) {
        if (textoMensagem.isNotEmpty()) {
            val idUsuarioRemetente = firebaseAuth.currentUser?.uid
            val idUsuarioDestinatario = dadosDestinatario?.id
            // idUsuarioRemetente e idUsuarioDestinatario podem ser subistituido
            // por dadosDestinatario e dadosUsuarioRemetente
            if (idUsuarioRemetente != null && idUsuarioDestinatario != null) {
                val idMensagem = firestore.collection(Constantes.MENSAGENS).document().id
                val mensagem = Mensagem(
                    idUsuario = idUsuarioRemetente,
                    mensagem = textoMensagem,
                    idMensagem = idMensagem
                )
                // Salvar mensagem para o remetente
                salvarMensagemFirestore(mensagem, idUsuarioRemetente, idUsuarioDestinatario)

                // Salvar mensagem para o destinatário
                salvarMensagemFirestore(mensagem, idUsuarioDestinatario, idUsuarioRemetente)

                // O usuário que está logado precisa salvar na conversa
            // nome e foto do destinatário
                val conversaRemetente = Conversa(
                     idUsuarioRemetente,
                     idUsuarioDestinatario,
                     dadosDestinatario!!.foto,
                    dadosDestinatario!!.nome,
                   textoMensagem
                )
                // O usuário destinatário precisa salvar na conversa nome foto do remetente
            val conversaDestinatario = Conversa(
                idUsuarioDestinatario,
                idUsuarioRemetente,
                dadosUsuarioRemetente!!.foto,
                dadosUsuarioRemetente!!.nome,
                textoMensagem
            )
                salvarConversaFirestore(conversaDestinatario)
                salvarConversaFirestore(conversaRemetente)
            }
        }
    }

    private fun salvarConversaFirestore(conversa: Conversa) {
        firestore
            .collection(Constantes.CONVERSAS)
            .document(conversa.idusuarioRemetente)
            .collection(Constantes.ULTIMAS_CONVERSAS)
            .document(conversa.idUsuarioDestinatario)
            .set(conversa)
            .addOnFailureListener {
                exibirMensagem("Erro ao salvar conversa")
            }


    }

    private fun salvarMensagemFirestore(
        mensagem: Mensagem,
        idUsuarioRemetente: String,
        idUsuarioDestinatario: String
    ) {
        val docId = mensagem.idMensagem.ifBlank { firestore.collection(Constantes.MENSAGENS).document().id }
        mensagem.idMensagem = docId
        firestore
            .collection(Constantes.MENSAGENS)
            .document(idUsuarioRemetente)
            .collection(idUsuarioDestinatario)
            .document(docId)
            .set(mensagem)
            .addOnFailureListener() {
                exibirMensagem("Erro ao enviar mensagem")
            }
        binding.editMensagem.setText("")
    }

    private fun inicializarToolbar() {
        val toolbar = binding.tbMensagens
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            if (dadosDestinatario != null) {
                binding.textMensagemNome.text = dadosDestinatario!!.nome
                Picasso.get()
                    .load(dadosDestinatario!!.foto)
                    .into(binding.imageFotoPerfil)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun recuperarDadosUsuarios() {
        // dados do remetente
        val idUsuarioLogado = firebaseAuth.currentUser?.uid
        if (idUsuarioLogado != null){
            firestore
                .collection(Constantes.USUARIOS)
                .document(idUsuarioLogado)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    try {
                        val usuario = documentSnapshot.toObject(Usuario::class.java)
                        if (usuario != null) {
                            dadosUsuarioRemetente = usuario
                        }
                    } catch (e: Exception) {
                        Log.e("MensagensActivity", "Erro ao converter documento para Usuario: ${e.message}", e)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MensagensActivity", "Erro ao buscar dados do usuário: ${exception.message}", exception)
                }
        }

        val extras = intent.extras
        if (extras != null) {
            try {
                dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable("dadosDestinatario", Usuario::class.java)
                } else {
                    extras.getParcelable("dadosDestinatario")
                }
                Log.i("MensagensActivity", "Dados do destinatário carregados: ${dadosDestinatario?.nome}")
            } catch (e: Exception) {
                Log.e("MensagensActivity", "Erro ao recuperar dados do destinatário: ${e.message}", e)
            }
        }
    }

    private fun excluirMensagem(mensagem: Mensagem) {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id
        
        if (idUsuarioRemetente == null || idUsuarioDestinatario == null) {
            exibirMensagem("Erro: Dados do usuário não disponíveis")
            return
        }

        val idMensagem = mensagem.idMensagem
        
        if (idMensagem.isNotBlank()) {
            // Deleta a mensagem do lado do remetente
            firestore.collection(Constantes.MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario)
                .document(idMensagem)
                .delete()
                .addOnSuccessListener {
                    Log.i("MensagensActivity", "Mensagem deletada do lado do remetente")
                    // Deleta também da coleção do destinatário
                    deletarMensagemDestinatario(idUsuarioDestinatario, idUsuarioRemetente, idMensagem)
                }
                .addOnFailureListener { erro ->
                    Log.e("MensagensActivity", "Erro ao deletar mensagem do remetente: ${erro.message}")
                    exibirMensagem("Erro ao deletar mensagem")
                }
        } else {
            // Fallback: busca pelo texto (menos confiável)
            firestore.collection(Constantes.MENSAGENS)
                .document(idUsuarioRemetente)
                .collection(idUsuarioDestinatario)
                .whereEqualTo("mensagem", mensagem.mensagem)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val doc = querySnapshot.documents[0]
                        val docId = doc.id
                        doc.reference.delete()
                            .addOnSuccessListener {
                                Log.i("MensagensActivity", "Mensagem deletada pelo texto do remetente")
                                deletarMensagemDestinatario(idUsuarioDestinatario, idUsuarioRemetente, docId)
                            }
                            .addOnFailureListener { erro ->
                                Log.e("MensagensActivity", "Erro ao deletar pelo texto: ${erro.message}")
                                exibirMensagem("Erro ao deletar mensagem")
                            }
                    }
                }
                .addOnFailureListener { erro ->
                    Log.e("MensagensActivity", "Erro ao buscar mensagem: ${erro.message}")
                    exibirMensagem("Erro ao buscar mensagem")
                }
        }
    }

    private fun deletarMensagemDestinatario(
        idUsuarioDestinatario: String,
        idUsuarioRemetente: String,
        idMensagem: String
    ) {
        firestore.collection(Constantes.MENSAGENS)
            .document(idUsuarioDestinatario)
            .collection(idUsuarioRemetente)
            .document(idMensagem)
            .delete()
            .addOnSuccessListener {
                Log.i("MensagensActivity", "Mensagem deletada do lado do destinatário")
                exibirMensagem("Mensagem deletada com sucesso")
            }
            .addOnFailureListener { erro ->
                Log.e("MensagensActivity", "Erro ao deletar cópia da mensagem: ${erro.message}")
                exibirMensagem("Erro ao deletar cópia da mensagem")
            }
    }
}