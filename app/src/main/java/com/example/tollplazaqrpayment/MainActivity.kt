package com.example.tollplazaqrpayment

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import android.view.View;

class MainActivity : AppCompatActivity() {

    private lateinit var btnManageVehicles: Button
    private lateinit var btnMakePayment: Button
    private lateinit var btnGenerateQR: Button
    private lateinit var btnLogout: Button
    private lateinit var btnContact: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        btnManageVehicles = findViewById(R.id.btnManageVehicles)
        btnMakePayment = findViewById(R.id.btnMakePayment)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        btnLogout = findViewById(R.id.btnLogout)
        btnContact = findViewById(R.id.btnContact)

        // Set onClick listeners
        btnManageVehicles.setOnClickListener {
            startActivity(Intent(this, VehicleActivity::class.java))
        }

        btnMakePayment.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        btnGenerateQR.setOnClickListener {
            startActivity(Intent(this, QRCodeGeneratorActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        btnContact.setOnClickListener {
            // Display contact information using a Toast or open a new Contact Activity
            Toast.makeText(this, "Contact us at: support@tollplaza.com", Toast.LENGTH_LONG).show()

            // Uncomment this to open a new ContactActivity if needed
            val intent = Intent(this, ContactActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}