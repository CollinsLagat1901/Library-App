package com.example.libraryapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.libraryapp.databinding.ActivityMainScreenBinding
import com.example.libraryapp.SignInActivity

class MainScreen : AppCompatActivity() {

    private lateinit var binding: ActivityMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //handle login click
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))

        }

        binding.skipBtn.setOnClickListener {

            startActivity(Intent(this, DashBoardUser::class.java))
        }
    }

}