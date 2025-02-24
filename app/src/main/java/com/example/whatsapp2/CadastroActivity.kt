package com.example.whatsapp2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsapp2.databinding.ActivityCadastroBinding
import com.example.whatsapp2.model.Usuario
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }
    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        inicializarToolbar()
        inicializarEventosClique()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validarCampos(): Boolean {
        var retorno = true;
        nome = binding.editNome.text.toString()
        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()

        if (nome.isNotEmpty()) {
            binding.textInputLayouNome.error = null
            //retorno = true

            if (email.isNotEmpty()) {
                binding.textInputLayouEmail.error = null
                //  retorno = true

                if (senha.isNotEmpty()) {
                    binding.textInputLayoutSenha.error = null
                    // retorno = true
                } else {
                    binding.textInputLayoutSenha.error = "Digite sua senha"
                    retorno = false
                }
            } else {
                binding.textInputLayouEmail.error = "Digite seu e-mail"
                retorno = false
            }
        } else {
            binding.textInputLayouNome.error = "Digite seu nome"
            retorno = false
        }

        return retorno

    }

    private fun inicializarEventosClique() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                cadastrarUsuario(nome, email, senha)
            } else {
                exibirMensagem("Preencha todos os campos")
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        exibirMensagem("Dados a serem cadastrados: ${nome} ${email} ${senha}")
        firebaseAuth.createUserWithEmailAndPassword(email, senha)

            .addOnCompleteListener { resultado ->
                if (resultado.isSuccessful) {
                    val idUsuario = resultado.result.user?.uid

                    if (idUsuario != null) {
                        val usuario = Usuario(idUsuario, nome, email)
                        salvarDadosUsuario(usuario)
                    }

                }
            }.addOnFailureListener { erro ->
                try {
                    throw erro
                } catch (erroSenhaFraca: FirebaseAuthInvalidCredentialsException) {
                    exibirMensagem("Senha inválida, inclua letras e números")
                } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException) {
                    exibirMensagem("E-mail Ja esta sendo usado")
                } catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException) {
                    exibirMensagem("E-mail inválido")
                }
            }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.materialToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.apply{
            title = "Cadastro"
        }
    }

    private fun salvarDadosUsuario(usuario: Usuario) {
        firebaseFirestore.collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                exibirMensagem("Usuário cadastrado com sucesso")
                startActivity(
                    Intent(this, MainActivity::class.java)
                )
            }
            .addOnFailureListener{
                exibirMensagem("Erro ao cadastrar usuário")
            }
    }
}




