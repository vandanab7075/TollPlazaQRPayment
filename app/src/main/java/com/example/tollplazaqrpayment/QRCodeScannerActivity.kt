package com.example.tollplazaqrpayment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import com.example.tollplazaqrpayment.R

class QRCodeScannerActivity : AppCompatActivity() {

    private lateinit var btnStartScan: Button
    private lateinit var tvScanResult: TextView

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout
        setContentView(R.layout.activity_qrcode_scanner)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        btnStartScan = findViewById(R.id.btnStartScan)
        tvScanResult = findViewById(R.id.tvScanResult)

        // Set onClick listener
        btnStartScan.setOnClickListener {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code")
        integrator.setCameraId(0) // Use a specific camera of the device
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Handle the scanned result
                val qrData = result.contents
                tvScanResult.text = "Processing..."

                // Parse JSON data
                try {
                    val jsonObject = JSONObject(qrData)
                    val paymentID = jsonObject.getString("paymentID")
                    val source = jsonObject.getString("source")
                    val destination = jsonObject.getString("destination")
                    val vehicleNumber = jsonObject.getString("vehicleNumber")
                    val vehicleType = jsonObject.getString("vehicleType")
                    val amount = jsonObject.getDouble("amount")
                    val timestamp = jsonObject.getLong("timestamp")

                    // Verify payment in Firestore
                    verifyPayment(paymentID, vehicleNumber, amount)

                } catch (e: Exception) {
                    e.printStackTrace()
                    tvScanResult.text = "Invalid QR Code."
                }

            } else {
                Toast.makeText(this, "No QR code found.", Toast.LENGTH_LONG).show()
                tvScanResult.text = "Scan Cancelled."
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun verifyPayment(paymentID: String, vehicleNumber: String, amount: Double) {
        // Search for the payment in Firestore
        db.collectionGroup("payments")
            .whereEqualTo("paymentID", paymentID)
            .whereEqualTo("vehicleNumber", vehicleNumber)
            .whereEqualTo("amount", amount)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    tvScanResult.text = "Payment Not Verified."
                } else {
                    // Payment verified
                    tvScanResult.text = "Payment Verified.\nVehicle: $vehicleNumber\nAmount: ₹$amount"
                    Toast.makeText(this, "Payment Verified.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error verifying payment: ${exception.message}", Toast.LENGTH_LONG).show()
                tvScanResult.text = "Verification Failed."
            }
    }
}
