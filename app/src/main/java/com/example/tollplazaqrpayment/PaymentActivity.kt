package com.example.tollplazaqrpayment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import android.content.Intent
import android.view.View
import java.util.*


class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var etSource: EditText
    private lateinit var etDestination: EditText
    private lateinit var spinnerVehicleSelect: Spinner
    private lateinit var btnCalculate: Button
    private lateinit var tvTollAmount: TextView
    private lateinit var btnProceedPayment: Button


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedVehicle: Vehicle? = null
    private var tollAmount: Double = 0.0
    private var sourceAddress: String = ""
    private var destinationAddress: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout
        setContentView(R.layout.activity_payment)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        etSource = findViewById(R.id.etSource)
        etDestination = findViewById<EditText>(R.id.etDestination)
        spinnerVehicleSelect = findViewById<Spinner>(R.id.spinnerVehicleSelect)
        btnCalculate = findViewById<Button>(R.id.btnCalculate)
        tvTollAmount = findViewById<TextView>(R.id.tvTollAmount)
        btnProceedPayment = findViewById<Button>(R.id.btnProceedPayment)

        // Load user's vehicles into Spinner
        loadUserVehicles()

        // Set onClick listener for Calculate button
        btnCalculate.setOnClickListener {
            calculateToll()
        }

        // Set onClick listener for Proceed Payment button
        btnProceedPayment.setOnClickListener {
            if (tollAmount > 0) {
                startPayment()
            } else {
                Toast.makeText(this, "Please calculate the toll amount first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserVehicles() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).collection("vehicles")
                .get()
                .addOnSuccessListener { documents ->
                    val vehicleNames = mutableListOf<String>()
                    val vehicles = mutableListOf<Vehicle>()
                    for (document in documents) {
                        val vehicle = document.toObject(Vehicle::class.java)
                        vehicles.add(vehicle)
                        vehicleNames.add("${vehicle.vehicleNumber} (${vehicle.vehicleType})")
                    }
                    if (vehicleNames.isEmpty()) {
                        Toast.makeText(this, "No vehicles found. Please add a vehicle first.", Toast.LENGTH_LONG).show()
                        // Redirect to VehicleActivity
                        startActivity(Intent(this, VehicleActivity::class.java))
                        finish()
                        return@addOnSuccessListener
                    }
                    val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleNames)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerVehicleSelect.adapter = spinnerAdapter

                    // Set selectedVehicle based on selection
                    spinnerVehicleSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            selectedVehicle = vehicles[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            selectedVehicle = null
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error loading vehicles: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun calculateToll() {
        sourceAddress = etSource.text.toString().trim()
        destinationAddress = etDestination.text.toString().trim()

        if (sourceAddress.isEmpty()) {
            etSource.error = "Source address is required."
            return
        }

        if (destinationAddress.isEmpty()) {
            etDestination.error = "Destination address is required."
            return
        }

        
        val random = Random()
        val distanceKm = 10 + random.nextInt(91)  // Range: 10 to 100 km
        val ratePerKm = 5 + random.nextInt(16)    // Range: ₹5 to ₹20 per km

        tollAmount = distanceKm * ratePerKm.toDouble()

        tvTollAmount.text = "Toll Amount: ₹$tollAmount"

        Toast.makeText(this, "Toll amount calculated based on distance.", Toast.LENGTH_SHORT).show()
    }

    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("YOUR_RAZORPAY_KEY_ID") // Replace with your Razorpay API Key ID

        try {
            val options = JSONObject()
            options.put("name", "Toll Plaza Payment")
            options.put("description", "Toll Payment")
            options.put("currency", "INR")
            options.put("amount", (tollAmount * 100).toLong()) // Amount in paise

            // Generate a unique order ID or use any identifier
            val orderId = UUID.randomUUID().toString()
            options.put("order_id", orderId)

            // Optional: Set pre-filled customer details
            val prefill = JSONObject()
            prefill.put("email", auth.currentUser?.email)
            prefill.put("contact", "9999999999")
            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()
        // Proceed to generate QR code
        generateQRCodeAfterPayment(razorpayPaymentID)
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_LONG).show()
    }

    private fun generateQRCodeAfterPayment(paymentID: String?) {
        // Save payment details in Firestore
        val user = auth.currentUser
        if (user != null) {
            val paymentDetails = hashMapOf(
                "source" to sourceAddress,
                "destination" to destinationAddress,
                "vehicleNumber" to selectedVehicle?.vehicleNumber,
                "vehicleType" to selectedVehicle?.vehicleType,
                "amount" to tollAmount,
                "paymentID" to paymentID,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(user.uid).collection("payments")
                .add(paymentDetails)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Payment details saved.", Toast.LENGTH_SHORT).show()
                    // Optionally, navigate to QRCodeGeneratorActivity
                    startActivity(Intent(this, QRCodeGeneratorActivity::class.java).apply {
                        putExtra("paymentID", paymentID)
                        putExtra("paymentDocID", documentReference.id)
                    })
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error saving payment details: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
