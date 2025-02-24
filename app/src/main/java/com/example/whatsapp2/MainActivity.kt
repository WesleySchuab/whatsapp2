package com.example.whatsapp2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whatsapp2.adapters.ViewPagerAdapter
import com.example.whatsapp2.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        iniciliarToolbar()
        inicializarNAvegacaoAbas()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun inicializarNAvegacaoAbas() {
        val tabLayout = binding.tabLayoutPrincipal
        val viewPager = binding.viewPagePrincipal
        val abas = listOf("CONVERSAS", "CONTATOS")
        viewPager.adapter = ViewPagerAdapter(
            abas, supportFragmentManager, lifecycle
        )
        TabLayoutMediator(tabLayout, viewPager) { aba, posicao ->
            aba.text = abas[posicao]
        }.attach()

    }

    private fun iniciliarToolbar() {
        val toolbar = binding.includeMaintoolbar.materialToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "WhatsApp"
        }
        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_principal, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.item_perfil -> {
                            startActivity(
                                Intent(this@MainActivity, PerfilActivity::class.java)
                            )
                        }

                        R.id.item_sair -> {
                            deslogarUsuario()
                        }

                    }
                    return true
                }
            }
        )
    }

    private fun deslogarUsuario() {
        AlertDialog.Builder(this)
            .setTitle("Deslogar")
            .setMessage("Deseja realmente sair?")
            .setNegativeButton("Não") { dialog, posicao -> }
            .setPositiveButton("Confirmar") { dialog, posicao ->
                firebaseAuth.signOut()
                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
            }
            .create()
            .show()

    }
}