package com.example.ttokjip.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.ttokjip.R
import com.example.ttokjip.data.Device
import com.example.ttokjip.databinding.ActivityCenterViewBinding

class CenterView : AppCompatActivity() {
    private lateinit var binding: ActivityCenterViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCenterViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFragmentView(MainView())

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_main -> {
                    setFragmentView(MainView())
                    true
                }
                R.id.menu_deviceManager ->{
                    setFragmentView(DeviceManagerView())
                    true
                }
                else -> false
            }
        }

    }

    private fun setFragmentView(fragmentId: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.center_frame, fragmentId)
            .commit()

    }


}