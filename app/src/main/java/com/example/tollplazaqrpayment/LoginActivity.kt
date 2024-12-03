package com.example.tollplazaqrpayment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmailLogin: EditText
    private lateinit var etPasswordLogin: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterRedirect: TextView

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        etEmailLogin = findViewById(R.id.etEmailLogin)
        etPasswordLogin = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect)

        // Set onClick listeners
        btnLogin.setOnClickListener {
            loginUser()
        }

        tvRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = etEmailLogin.text.toString().trim()
        val password = etPasswordLogin.text.toString().trim()

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmailLogin.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(password)) {
            etPasswordLogin.error = "Password is required."
            return
        }

        // Login user with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Login successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Redirect to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
