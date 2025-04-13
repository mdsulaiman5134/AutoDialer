package com.example.autodialer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class Registration : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var registerButton: Button
    private val PAY_TIMESTAMP_KEY = "pay_timestamp_key"
    private var selectedPaymentAmount = 0
    private var upiId = "7259318005@ybl"
    private var description = ""
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.theme)
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val login = findViewById<Button>(R.id.Login)
        login.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        emailField = findViewById(R.id.etEmail)
        passwordField = findViewById(R.id.etPassword)
        registerButton = findViewById(R.id.btnRegister)

        // Dropdown options
        val dropdownOptions = listOf("individual", "Company")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dropdownOptions)
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = dropdownOptions[position]
            selectedPaymentAmount = if (selectedOption == "individual") 200 else 1000
        }

        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (selectedPaymentAmount > 0) {
                description = email
//                handleUpiResponse("success")
                val currentTime = System.currentTimeMillis()
                val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putLong(PAY_TIMESTAMP_KEY, currentTime).apply()
                payUsingUpi(email, password, upiId, "Auto Dial", description, selectedPaymentAmount.toString())
            } else {
                Toast.makeText(this, "Please select a valid registration type", Toast.LENGTH_LONG).show()
            }
        }

        val passwordEditText = findViewById<EditText>(R.id.etPassword)

        passwordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Check if the touch was on the drawableRight
                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[2].bounds.width())) {
                    togglePasswordVisibility(passwordEditText)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }

    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (isPasswordVisible) {
            // Hide password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            // Show password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        isPasswordVisible = !isPasswordVisible
        editText.setSelection(editText.text.length) // Move cursor to end of text
    }

    private fun payUsingUpi(email: String, password: String, upiId: String, name: String, description: String, amount: String) {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", description)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()

        val upiIntent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }

        val chooser = Intent.createChooser(upiIntent, "Pay with")
        if (chooser.resolveActivity(packageManager) != null) {
            startActivityForResult(chooser, UPI_PAYMENT)
        } else {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_LONG).show()
        }

        // Store the email and password to use after payment confirmation
        pendingEmail = email
        pendingPassword = password
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPI_PAYMENT) {
            val response = data?.getStringExtra("response")
            if (response == null) {
                Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_LONG).show()
            } else {
                handleUpiResponse(response)
            }
        }
    }


    private fun handleUpiResponse(response: String) {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        if (response.contains("SUCCESS", true)) {
            Toast.makeText(this, "Transaction Successful", Toast.LENGTH_LONG).show()
            if (selectedPaymentAmount == 200) {
                // Save for individual users
                saveUserLocally(email, password, "individual")
            } else {
                saveUserLocally(email, password, "Company")
                registerUser(email, password) // Register user for Company
            }
        } else if (response.contains("FAILURE", true)) {
            Toast.makeText(this, "Transaction Failed", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_LONG).show()
        }
    }


    private fun saveUserLocally(email: String, password: String, userType: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Log.e("Registration", "Error: Email or Password is empty. Email=$email, Password=$password")
            return
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Log data before saving
       // Log.e("Registration", "Saving data: Email=$email, Password=$password, UserType=$userType")

        // Save data to SharedPreferences
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putString("user_type", userType)
        editor.apply()

        // Log data after saving to confirm
        val savedEmail = sharedPreferences.getString("email", "Not Found")
        val savedPassword = sharedPreferences.getString("password", "Not Found")
        val savedUserType = sharedPreferences.getString("user_type", "Not Found")
      //  Log.e("Registration", "Data saved: Email=$savedEmail, Password=$savedPassword, UserType=$savedUserType")

        navigateToMainActivity(userType == "individual")
    }


    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // After successful registration, navigate to the correct MainActivity
                    navigateToMainActivity(false)
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToMainActivity(isIndividualSelected: Boolean) {
        val intent = if (isIndividualSelected) {
            Intent(this, Login::class.java) // Individual registration
        } else {
            Intent(this, Login::class.java) // Company registration
        }
        intent.putExtra("USER_TYPE", if (isIndividualSelected) "individual" else "Company")
        startActivity(intent)
        finish() // Close the registration activity
    }

    companion object {
        private const val UPI_PAYMENT = 1
        private var pendingEmail = ""
        private var pendingPassword = ""
    }
}
