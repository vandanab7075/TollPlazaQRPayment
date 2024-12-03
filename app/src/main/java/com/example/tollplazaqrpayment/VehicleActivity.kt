package com.example.tollplazaqrpayment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Vehicle(
    var vehicleNumber: String = "",
    var licenseNumber: String = "",
    var vehicleType: String = ""
)

class VehicleActivity : AppCompatActivity() {

    private lateinit var etVehicleNumber: EditText
    private lateinit var etLicenseNumber: EditText
    private lateinit var spinnerVehicleType: Spinner
    private lateinit var btnAddVehicle: Button
    private lateinit var lvVehicles: ListView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var vehicleList: MutableList<Vehicle>
    private lateinit var adapter: ArrayAdapter<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout
        setContentView(R.layout.activity_vehicle)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        etVehicleNumber = findViewById(R.id.etVehicleNumber)
        etLicenseNumber = findViewById(R.id.etLicenseNumber)
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType)
        btnAddVehicle = findViewById(R.id.btnAddVehicle)
        lvVehicles = findViewById(R.id.lvVehicles)

        // Initialize vehicle list and adapter
        vehicleList = mutableListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        lvVehicles.adapter = adapter

        // Setup Spinner
        val vehicleTypes = listOf("Car", "Motorcycle", "Truck", "Bus")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerVehicleType.adapter = spinnerAdapter

        // Load existing vehicles
        loadVehicles()

        // Set onClick listener for adding vehicle
        btnAddVehicle.setOnClickListener {
            addVehicle()
        }

        // Set onItemLongClickListener for deleting vehicle
        lvVehicles.setOnItemLongClickListener { parent, view, position, id ->
            val vehicle = vehicleList[position]
            deleteVehicle(vehicle)
            true
        }
    }

    private fun loadVehicles() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).collection("vehicles")
                .get()
                .addOnSuccessListener { documents ->
                    vehicleList.clear()
                    adapter.clear()
                    for (document in documents) {
                        val vehicle = document.toObject(Vehicle::class.java)
                        vehicleList.add(vehicle)
                        adapter.add("${vehicle.vehicleNumber} (${vehicle.vehicleType})")
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error loading vehicles: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun addVehicle() {
        val vehicleNumber = etVehicleNumber.text.toString().trim()
        val licenseNumber = etLicenseNumber.text.toString().trim()
        val vehicleType = spinnerVehicleType.selectedItem.toString()

        val pattern = "^[A-Za-z0-9]{9}$".toRegex() // Regex for exactly 7 alphanumeric characters

        if (!pattern.matches(vehicleNumber)) {
            etVehicleNumber.error = "Vehicle Number must be 9 alphanumeric characters."
            return
        }

        if (!pattern.matches(licenseNumber)) {
            etLicenseNumber.error = "License Number must be 9 alphanumeric characters."
            return
        }

        val vehicle = Vehicle(vehicleNumber, licenseNumber, vehicleType)

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).collection("vehicles")
                .add(vehicle)
                .addOnSuccessListener {
                    Toast.makeText(this, "Vehicle added successfully.", Toast.LENGTH_SHORT).show()
                    etVehicleNumber.text.clear()
                    etLicenseNumber.text.clear()
                    loadVehicles()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error adding vehicle: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun deleteVehicle(vehicle: Vehicle) {
        val user = auth.currentUser
        if (user != null) {
            // Find the document to delete
            db.collection("users").document(user.uid).collection("vehicles")
                .whereEqualTo("vehicleNumber", vehicle.vehicleNumber)
                .whereEqualTo("licenseNumber", vehicle.licenseNumber)
                .whereEqualTo("vehicleType", vehicle.vehicleType)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Vehicle deleted.", Toast.LENGTH_SHORT).show()
                                loadVehicles()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error deleting vehicle: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
