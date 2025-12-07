package com.example.whatsapp2.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp2.R
import com.example.whatsapp2.adapters.ConversasAdapter
import com.example.whatsapp2.databinding.ActivityMensagensBinding
import com.example.whatsapp2.model.Conversa
import com.example.whatsapp2.model.Mensagem
import com.example.whatsapp2.model.Usuario
import com.example.whatsapp2.utils.Constantes
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

    private lateinit var listenerRegistration: ListenerRegistration

    private var dadosDestinatario: Usuario? = null
    private var dadosUsuarioRemetente: Usuario? = null

    private lateinit var conversasAdapter: ConversasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        recuperarDadosUsuarios()
        inicializarToolbar()
        inicializarEventosDeClique()
        inicializarRecyclerView()
        inicializarListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun inicializarRecyclerView() {
        with(binding) {
            conversasAdapter = ConversasAdapter()
            rvMensagens.adapter = conversasAdapter
            rvMensagens.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListeners() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id

        if (idUsuarioRemetente != null && idUsuarioDestinatario != null) {
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
                            listaMensagens.add(mensagem)
                            Log.i("ListenerMensagem", mensagem.mensagem)
                        }
                    }
                    if (listaMensagens.isNotEmpty()) {
                        // Adapter
                        conversasAdapter.adicionarLista(listaMensagens)
                    }
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
                val mensagem = Mensagem(
                    idUsuario = idUsuarioRemetente,
                    mensagem = textoMensagem
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
        firestore
            .collection(Constantes.MENSAGENS)
            .document(idUsuarioRemetente)
            .collection(idUsuarioDestinatario)
            .add(mensagem)
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
            val origem = extras.getString("origem")
            if (origem == Constantes.ORIGEM_CONTATO) {
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
    }
}