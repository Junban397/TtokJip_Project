package com.example.ttokjip.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ttokjip.R
import com.example.ttokjip.databinding.ActivityLoginMainBinding

class LoginMain : AppCompatActivity() {
    private lateinit var binding: ActivityLoginMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}