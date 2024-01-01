package com.pavlov.MyShadowGallery

import android.content.Intent
import com.pavlov.MyShadowGallery.util.PasswordManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.pavlov.MyShadowGallery.databinding.ActivityLoginBinding  // Замените на ваш путь к файлу биндинга

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var passwordManager: PasswordManager
    private lateinit var passwordButton: Button
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_login)
        setContentView(binding.root)

        passwordManager = PasswordManager(this)
        passwordButton = findViewById(R.id.loginButton)
        passwordEditText = findViewById(R.id.passwordEditText)

        if (!passwordManager.hasPassword()) {
            // Пользователь не установил пароль, перенаправим его на активность установки пароля
            startActivity(Intent(this, SetPasswordActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            val enteredPassword = binding.passwordEditText.text.toString()
            val savedPassword = passwordManager.getPassword(this)

            if (enteredPassword == savedPassword) {
                // Пароль верен, перенаправим пользователя на главную активность
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // Пароль неверен, выполните действия по вашему усмотрению (например, вывод ошибки)
                // В данном случае, просто очистим поле ввода пароля
                binding.passwordEditText.text.clear()
            }
        }
    }
}