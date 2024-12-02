package com.example.ttokjip.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ttokjip.databinding.FragmentSettingViewBinding
import com.example.ttokjip.login.LoginMain

class SettingView : Fragment() {
    private var _binding: FragmentSettingViewBinding?=null
    private val binding get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentSettingViewBinding.inflate(inflater, container, false)
        val sharedPreferences =
            requireContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE)
        binding.logoutBtn.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            navigateToLogin()
        }
        return binding.root
    }
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginMain::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}