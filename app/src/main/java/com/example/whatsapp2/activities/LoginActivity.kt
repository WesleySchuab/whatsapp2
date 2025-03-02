package com.example.whatsapp2.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsapp2.R
import com.example.whatsapp2.databinding.ActivityLoginBinding
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        inicializarEventosClique()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser
        if (usuarioAtual != null) {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }
    }


        private fun inicializarEventosClique() {
            binding.textCadastro.setOnClickListener {
                startActivity(
                    Intent(this, CadastroActivity::class.java)
                )
            }
            binding.btnLogar.setOnClickListener {
                if (validarCampos()) {
                    logarUsuario()
                }
            }
        }

        private fun logarUsuario() {
            firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    exibirMensagem("Logado com sucesso")
                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )
                }
                .addOnFailureListener { erro ->
                    try {
                        throw erro
                    } catch (erroUsuarioInvalido: FirebaseAuthInvalidUserException) {
                        exibirMensagem("Usuário inválido")
                    } catch (erroSenhaInvalida: FirebaseAuthInvalidCredentialsException) {
                        exibirMensagem("Senha ou e-mail inválido")
                    }
                }
        }

        private fun validarCampos(): Boolean {
            email = binding.editLoginEmail.text.toString()
            senha = binding.editLoginSenha.text.toString()
            var retorno = true
            if (email.isNotEmpty()) {
                binding.textInputLayoultLoginEmail.error = null
                if (senha.isNotEmpty()) {
                    binding.textInputLayoutLoginSenha.error = null
                } else {
                    binding.textInputLayoutLoginSenha.error = "Digite sua senha"
                    retorno = false
                }
            } else {
                binding.textInputLayoultLoginEmail.error = "Digite seu e-mail"
                retorno = false
            }
            return retorno
        }
    }

