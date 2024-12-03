package com.example.tollplazaqrpayment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class RegistrationActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginRedirect: TextView

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect)

        // Set onClick listeners
        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.error = "Password is required."
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters."
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match."
            return
        }

        // Register user with Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Registration successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("RegistrationActivity", "User registration successful.")
                    // Redirect to LoginActivity
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Log.e("RegistrationError", "Registration failed", task.exception)
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
