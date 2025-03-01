package com.example.whatsapp2

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsapp2.databinding.ActivityPerfilBinding
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class PerfilActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            binding.imagePerfil.setImageURI(uri)
            uploadImageStorage(uri)
        } else {
            exibirMensagem("Nenhuma imagem selecionada")
        }
    }

    private fun uploadImageStorage(uri: Uri) {
        // fotos / usuarios / idUsuario / fotoPerfil
        val idUsuario = firebaseAuth.currentUser?.uid
        if (idUsuario != null) {
            storage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener { task ->
                    exibirMensagem("sucesso ao fazer upload da imagem")
                    task.metadata?.reference?.downloadUrl?.addOnSuccessListener { url ->
                        exibirMensagem("sucesso ao pegar a url da imagem")
                        val dados = mapOf(
                            "foto" to url.toString()
                        )
                        //atualizarDadosPerfil(idUsuario, dados)
                    }
                }.addOnFailureListener {
                    exibirMensagem("erro ao fazer upload da imagem")
                }
        }
    }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(binding.root)
            inicializarToolbar()
            solicitarPermissoes()
            inicializarEventosDeClique()

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        private fun inicializarEventosDeClique() {
            binding.fabSelecionarGaleria.setOnClickListener {
                // Lógica para selecionar imagem da galeria
                if (temPermissaoCamera) {
                    gerenciadorGaleria.launch("image/*")
                } else {
                    exibirMensagem("Não tem permissão para acessar galeria")
                    solicitarPermissoes()
                }
            }
        }


        private fun solicitarPermissoes() {
            // Verificar se as permissões já foram concedidas
            temPermissaoCamera = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            temPermissaoGaleria = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

            var listaPermissoes = mutableListOf<String>()
            if (!temPermissaoCamera) {
                listaPermissoes.add(Manifest.permission.CAMERA)
            }
            if (!temPermissaoGaleria) {
                listaPermissoes.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissoes ->
                temPermissaoCamera = permissoes[Manifest.permission.CAMERA] ?: temPermissaoCamera
                temPermissaoGaleria =
                    permissoes[Manifest.permission.READ_MEDIA_IMAGES] ?: temPermissaoGaleria

            }
            if (listaPermissoes.isNotEmpty()) {
                gerenciadorPermissoes.launch(listaPermissoes.toTypedArray())
            }
        }

        private fun inicializarToolbar() {
            val toolbar = binding.includeToolbarPerfil.materialToolbar
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.apply {
                title = "Editar Perfil"
            }
        }

    }