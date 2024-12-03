package com.example.tollplazaqrpayment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRCodeGeneratorActivity : AppCompatActivity() {

    private lateinit var ivQRCode: ImageView
    private lateinit var btnBackToMain: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var paymentID: String? = null
    private var paymentDocID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout
        setContentView(R.layout.activity_qr_code_generator)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        ivQRCode = findViewById(R.id.ivQRCode)
        btnBackToMain = findViewById(R.id.btnBackToMain)

        // Get payment details from Intent
        paymentID = intent.getStringExtra("paymentID")
        paymentDocID = intent.getStringExtra("paymentDocID")

        // Generate QR code
        if (paymentID != null && paymentDocID != null) {
            generateQRCode(paymentDocID!!)
        } else {
            Toast.makeText(this, "Invalid payment details.", Toast.LENGTH_LONG).show()
            // Redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Set onClick listener for back button
        btnBackToMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun generateQRCode(paymentDocID: String) {
        // Retrieve payment details from Firestore
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).collection("payments").document(paymentDocID)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val source = document.getString("source") ?: ""
                        val destination = document.getString("destination") ?: ""
                        val vehicleNumber = document.getString("vehicleNumber") ?: ""
                        val vehicleType = document.getString("vehicleType") ?: ""
                        val amount = document.getDouble("amount") ?: 0.0
                        val timestamp = document.getLong("timestamp") ?: 0L

                        // Create a JSON string or any format you prefer
                        val qrData = """
                            {
                                "paymentID": "$paymentID",
                                "source": "$source",
                                "destination": "$destination",
                                "vehicleNumber": "$vehicleNumber",
                                "vehicleType": "$vehicleType",
                                "amount": $amount,
                                "timestamp": $timestamp
                            }
                        """.trimIndent()

                        // Generate QR code
                        try {
                            val barcodeEncoder = BarcodeEncoder()
                            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400)
                            ivQRCode.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error generating QR code: ${e.message}", Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }

                    } else {
                        Toast.makeText(this, "Payment details not found.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching payment details: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
