package com.example.whatsapp2.fragmentes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.whatsapp2.R
import com.example.whatsapp2.databinding.FragmentContatosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ContatosFragment : Fragment() {
    private lateinit var binding: FragmentContatosBinding
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
}