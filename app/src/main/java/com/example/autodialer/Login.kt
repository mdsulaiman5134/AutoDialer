//package com.example.autodialer
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.drawable.ColorDrawable
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.os.Build
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.text.InputType
//import android.util.Log
//import android.util.Patterns
//import android.view.MotionEvent
//import android.view.View
//import android.view.WindowManager
//import android.widget.Button
//import android.widget.EditText
//import android.widget.FrameLayout
//import android.widget.ProgressBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.content.ContextCompat
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//
//class Login : AppCompatActivity() {
//
//    private lateinit var firebaseAuth: FirebaseAuth
//    private lateinit var progressBar: ProgressBar
//    private var isPasswordVisible = false
//    private val database = FirebaseDatabase.getInstance("https://autodialer-2cd95-default-rtdb.asia-southeast1.firebasedatabase.app/").reference // Firebase Realtime Database reference
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.statusBarColor = ContextCompat.getColor(this, R.color.theme)
//        }
//        firebaseAuth = FirebaseAuth.getInstance()
//        val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
//        spinnerOverlay.visibility = View.GONE // Hide the spinner
//        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//
//        val passwordEditText = findViewById<EditText>(R.id.login_password)
//
//        passwordEditText.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                // Check if the touch was on the drawableRight
//                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[2].bounds.width())) {
//                    togglePasswordVisibility(passwordEditText)
//                    true
//                } else {
//                    false
//                }
//            } else {
//                false
//            }
//        }
//
//        if (!isInternetAvailable()) {
//            val str = "Connect to internet"
//            showGatewayDialog(str)
//        }
//
//        val home = findViewById<Button>(R.id.home)
//        home.setOnClickListener {
//            val intent = Intent(this, Registration::class.java)
//            startActivity(intent)
//        }
//
//        val loginButton = findViewById<Button>(R.id.login_button)
//        loginButton.setOnClickListener {
//            val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
//            spinnerOverlay.visibility = View.VISIBLE
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//            )
//            val loginEmail = findViewById<EditText>(R.id.login_email)
//            val loginPassword = findViewById<EditText>(R.id.login_password)
//            val email = loginEmail.text.toString()
//            val password = loginPassword.text.toString()
//
//            if (email.isNotEmpty() && password.isNotEmpty()) {
//                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        // Check if email exists in the Firebase Realtime Database and decrement the number
//                        checkAndDecrementUser(email)
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                    } else {
//                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
//            }
//            Handler(Looper.getMainLooper()).postDelayed({
//                spinnerOverlay.visibility = View.GONE // Hide the spinner
//                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // Re-enable interaction
//            }, 7000)
//        }
//
//        val forgotPassword = findViewById<TextView>(R.id.forgot_password)
//        forgotPassword.setOnClickListener {
//            val builder = AlertDialog.Builder(this)
//            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
//            val userEmail = view.findViewById<EditText>(R.id.editBox)
//
//            builder.setView(view)
//            val dialog = builder.create()
//
//            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
//                compareEmail(userEmail)
//                dialog.dismiss()
//            }
//            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
//                dialog.dismiss()
//            }
//            if (dialog.window != null) {
//                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
//            }
//            dialog.show()
//        }
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
//        spinnerOverlay.visibility = View.GONE // Hide the spinner
//        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//    }
//
//    // Compare the entered email to send a password reset if needed
//    private fun compareEmail(email: EditText) {
//        if (email.text.toString().isEmpty()) {
//            return
//        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
//            return
//        }
//        firebaseAuth.sendPasswordResetEmail(email.text.toString())
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//
//    private fun togglePasswordVisibility(editText: EditText) {
//        if (isPasswordVisible) {
//            // Hide password
//            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//        } else {
//            // Show password
//            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//        }
//        isPasswordVisible = !isPasswordVisible
//        editText.setSelection(editText.text.length) // Move cursor to end of text
//    }
//
//    private fun isInternetAvailable(): Boolean {
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
//
//        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//    }
//
//    private fun showGatewayDialog(str: String) {
//        val builder = AlertDialog.Builder(this)
//        builder.setMessage(str)
//            .setCancelable(false)
//            .setNegativeButton("OK") { dialog, _ ->
//                dialog.dismiss()
//            }
//        val alert = builder.create()
//        alert.show()
//    }
//
//    // Check if the email exists in the database and decrement the number
//    private fun checkAndDecrementUser(email: String) {
//        val usersRef = database.child("device")
//        usersRef.get().addOnSuccessListener { snapshot ->
//            for (user in snapshot.children) {
//                val userData = user.value.toString() // This will contain email,emp (e.g., user1@example.com,5)
//                val userParts = userData.split(",")
//                val userEmail = userParts[0] // Extracting the email
//                val currentCount = userParts[1] // Extracting the number after the comma
//
//                // Log the full string (email and number after the comma)
//                Log.e("User Data", "Retrieved user data: $userData")
//
//                if (userEmail == email) {
//                    // Try to parse the number and decrement it by 1
//                    val newCount = currentCount.toIntOrNull()?.minus(1)?.toString() ?: "0" // Default to "0" if parsing fails
//
//                    // Log the new count after decrement
//                    Log.e("User Data", "Decremented count: $newCount")
//
//                    // Get the current key (e.g., user1, user2, etc.) to update that specific record
//                    val count = user.key // This would be user1, user2, etc.
//
//                    // Update the value in Firebase with the new email and decremented number
//                    if (count != null) {
//                        usersRef.child(count).setValue("$userEmail,$newCount").addOnCompleteListener {
//                            if (it.isSuccessful) {
//                                Toast.makeText(this, "Number decremented successfully", Toast.LENGTH_SHORT).show()
//                            } else {
//                                Toast.makeText(this, "Failed to decrement number", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                    return@addOnSuccessListener
//                }
//            }
//            // If the email is not found
//            Toast.makeText(this, "Email not found in database", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener {
//            Toast.makeText(this, "Failed to access Firebase database", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // Modify checkAndDecrementUser to increment the count
//    private fun checkAndIncrementUser(email: String) {
//        val usersRef = database.child("device")
//        usersRef.get().addOnSuccessListener { snapshot ->
//            for (user in snapshot.children) {
//                val userData = user.value.toString() // This will contain email, emp (e.g., user1@example.com,5)
//                val userParts = userData.split(",")
//                val userEmail = userParts[0] // Extracting the email
//                val currentCount = userParts[1] // Extracting the number after the comma
//
//                // Log the full string (email and number after the comma)
//                Log.e("User Data", "Retrieved user data: $userData")
//
//                if (userEmail == email) {
//                    // Try to parse the number and increment it by 1
//                    val newCount = currentCount.toIntOrNull()?.plus(1)?.toString() ?: "1" // Default to "1" if parsing fails
//
//                    // Log the new count after increment
//                    Log.e("User Data", "Incremented count: $newCount")
//
//                    // Get the current key (e.g., user1, user2, etc.) to update that specific record
//                    val count = user.key // This would be user1, user2, etc.
//
//                    // Update the value in Firebase with the new email and incremented number
//                    if (count != null) {
//                        usersRef.child(count).setValue("$userEmail,$newCount").addOnCompleteListener {
//                            if (it.isSuccessful) {
//                                Toast.makeText(this, "Number incremented successfully", Toast.LENGTH_SHORT).show()
//                            } else {
//                                Toast.makeText(this, "Failed to increment number", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                    return@addOnSuccessListener
//                }
//            }
//            // If the email is not found
//            Toast.makeText(this, "Email not found in database", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener {
//            Toast.makeText(this, "Failed to access Firebase database", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        val loginEmail = findViewById<EditText>(R.id.login_email)
//        val email = loginEmail.text.toString()
//
//        if (email.isNotEmpty()) {
//            checkAndIncrementUser(email)
//        }
//    }
//
//
//}

package com.example.autodialer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.AdError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

//class Login : AppCompatActivity() {
//
//    private lateinit var firebaseAuth: FirebaseAuth
//    private lateinit var progressBar: ProgressBar
//    private var isPasswordVisible = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.statusBarColor = ContextCompat.getColor(this, R.color.theme)
//        }
//        firebaseAuth = FirebaseAuth.getInstance()
//        val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
//        spinnerOverlay.visibility = View.GONE // Hide the spinner
//        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//
//        val passwordEditText = findViewById<EditText>(R.id.login_password)
//
//        passwordEditText.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                // Check if the touch was on the drawableRight
//                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[2].bounds.width())) {
//                    togglePasswordVisibility(passwordEditText)
//                    true
//                } else {
//                    false
//                }
//            } else {
//                false
//            }
//        }
//
//        if (!isInternetAvailable()) {
//            val str="Connect to internet"
//            showGatewayDialog(str)
//        }
//
//        val home = findViewById<Button>(R.id.home)
//        home.setOnClickListener {
//            val intent = Intent(this, Registration::class.java)
//            startActivity(intent)
//        }
//
//
//        val loginButton=findViewById<Button>(R.id.login_button)
////        loginButton.setOnClickListener {
////            val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
////            spinnerOverlay.visibility = View.VISIBLE
////            window.setFlags(
////                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
////                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
////            )
////            val loginEmail=findViewById<EditText>(R.id.login_email)
////            val loginPassword=findViewById<EditText>(R.id.login_password)
////            val email = loginEmail.text.toString()
////            val password = loginPassword.text.toString()
////
////            if (email.isNotEmpty() && password.isNotEmpty()) {
////                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
////                    if (it.isSuccessful) {
////                        startActivity(Intent(this, MainActivity::class.java))
////                    } else {
////                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
////                    }
////                }
////            } else {
////                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
////            }
////            Handler(Looper.getMainLooper()).postDelayed({
////                spinnerOverlay.visibility = View.GONE // Hide the spinner
////                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // Re-enable interaction
////            }, 7000)
////        }
//
//        loginButton.setOnClickListener {
//            val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
//            spinnerOverlay.visibility = View.VISIBLE
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//            )
//            val loginEmail = findViewById<EditText>(R.id.login_email)
//            val loginPassword = findViewById<EditText>(R.id.login_password)
//            val email = loginEmail.text.toString()
//            val password = loginPassword.text.toString()
//
//            if (email.isNotEmpty() && password.isNotEmpty()) {
//                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        // Get the user type from the intent
//                        val userType = intent.getStringExtra("USER_TYPE")
//                        val targetActivity = if (userType == "Individual") {
//                            MainActivity::class.java
//                        } else {
//                            MainActivity4::class.java
//                        }
//                        val intent = Intent(this, targetActivity)
//                        startActivity(intent)
//                        finish()
//                    } else {
//                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
//            }
//            Handler(Looper.getMainLooper()).postDelayed({
//                spinnerOverlay.visibility = View.GONE // Hide the spinner
//                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // Re-enable interaction
//            }, 7000)
//        }
//
//
//        val forgotPassword=findViewById<TextView>(R.id.forgot_password)
//        forgotPassword.setOnClickListener {
//            val builder = AlertDialog.Builder(this)
//            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
//            val userEmail = view.findViewById<EditText>(R.id.editBox)
//
//            builder.setView(view)
//            val dialog = builder.create()
//
//            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
//                compareEmail(userEmail)
//                dialog.dismiss()
//            }
//            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
//                dialog.dismiss()
//            }
//            if (dialog.window != null){
//                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
//            }
//            dialog.show()
//        }
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
//        spinnerOverlay.visibility = View.GONE // Hide the spinner
//        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//    }
//
//    //Outside onCreate
//    private fun compareEmail(email: EditText){
//        if (email.text.toString().isEmpty()){
//            return
//        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()){
//            return
//        }
//        firebaseAuth.sendPasswordResetEmail(email.text.toString())
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//
//    private fun togglePasswordVisibility(editText: EditText) {
//        if (isPasswordVisible) {
//            // Hide password
//            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//        } else {
//            // Show password
//            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//        }
//        isPasswordVisible = !isPasswordVisible
//        editText.setSelection(editText.text.length) // Move cursor to end of text
//    }
//
//    private fun isInternetAvailable(): Boolean {
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
//
//        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//    }
//    private fun showGatewayDialog(str:String) {
//        val builder = AlertDialog.Builder(this)
//        builder.setMessage("$str")
//            .setCancelable(false)
//            .setNegativeButton("OK") { dialog, _ ->
//                dialog.dismiss()
//            }
//        val alert = builder.create()
//        alert.show()
//    }
//}
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class Login : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private var isPasswordVisible = false
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var timerHandler: Handler
    private val PAY_TIMESTAMP_KEY = "pay_timestamp_key"
    private lateinit var alertDialog: AlertDialog
    private var upiId = "7259318005@ybl"
    private val REQUEST_CALL_PHONE = 2
    private lateinit var adView: AdView
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.theme)
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
        spinnerOverlay.visibility = View.GONE // Hide the spinner
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        firebaseDatabase = FirebaseDatabase.getInstance()
        timerHandler = Handler(Looper.getMainLooper())

        // Start the 3-minute timer
        checkLastPayTimestamp()

        val passwordEditText = findViewById<EditText>(R.id.login_password)

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

        if (!isInternetAvailable()) {
            val str="Connect to internet"
            showGatewayDialog(str)
        }

        val home = findViewById<Button>(R.id.home)
        home.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }

        val emailField = findViewById<EditText>(R.id.login_email)
        val passwordField = findViewById<EditText>(R.id.login_password)
        val userTypeField = findViewById<TextView>(R.id.textView5)

        // Get the user type from intent and display it in TextView7
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")
        val userType = sharedPreferences.getString("user_type", "")

        // Set data to views
        emailField.setText(email)
        passwordField.setText(password)
        userTypeField.text = userType

        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            spinnerOverlay.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            val inputEmail = emailField.text.toString().trim()
            val inputPassword = passwordField.text.toString().trim()

            if (inputEmail.isNotEmpty() && inputPassword.isNotEmpty()) {
                fetchCurrentTimeFromFirebase { currentTime ->
                    val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    val lastPayTimestamp = sharedPreferences.getLong(PAY_TIMESTAMP_KEY, 0L)

                    // Check if 10 days (in milliseconds) have passed
                    if (lastPayTimestamp == 0L || (currentTime - lastPayTimestamp) >= 28 * 24 * 60 * 60 * 1000L) {
                        showPayDialog(currentTime)
                    }
                    else{
                        if (userType == "individual") {
                            // Authenticate using SharedPreferences
                            if (inputEmail == email && inputPassword == password) {
                                navigateToActivity(MainActivity::class.java)
                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_LONG).show()
                            }
                        } else if (userType == "group" || userType == "" || userType=="null") {
                            // Authenticate using Firebase
                            firebaseAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        navigateToActivity(AdminActivity::class.java)
                                    } else {
                                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            //Toast.makeText(this, "Invalid user type", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                spinnerOverlay.visibility = View.GONE // Hide the spinner
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // Re-enable interaction
            }, 2000)
        }


        val forgotPassword = findViewById<TextView>(R.id.forgot_password)
        forgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val userEmail = view.findViewById<EditText>(R.id.editBox)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                compareEmail(userEmail)
                dialog.dismiss()
            }
            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }

        val etCode = findViewById<EditText>(R.id.etCode)
        val etComp = findViewById<EditText>(R.id.etComp)
        val btnCheck = findViewById<Button>(R.id.btnCheck)

        btnCheck.setOnClickListener {
            val enteredCode = etCode.text.toString().trim()
            val companyName = etComp.text.toString().trim()

            if (enteredCode.isNotEmpty() && companyName.isNotEmpty()) {
                val isValid = decodeAndValidateCode(enteredCode, companyName)

                if (isValid) {
                   // Toast.makeText(this, "Code valid! Navigating...", Toast.LENGTH_SHORT).show()
                    navigateToActivity(MainActivity::class.java)
                } else {
                    Toast.makeText(this, "Invalid code. Please try again.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
            }
        }

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)



        val adRequest1 = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-4109819105033978/8583597445", adRequest1, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                showInterstitialAd()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
                Log.d("AdMob", "Interstitial Ad failed to load: ${adError.message}")
            }
        })


    }

    override fun onResume() {
        super.onResume()
        val spinnerOverlay = findViewById<FrameLayout>(R.id.spinnerOverlay)
        spinnerOverlay.visibility = View.GONE // Hide the spinner
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    // Outside onCreate
    private fun compareEmail(email: EditText) {
        if (email.text.toString().isEmpty()) {
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            return
        }
        firebaseAuth.sendPasswordResetEmail(email.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Check your email", Toast.LENGTH_LONG).show()
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

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showGatewayDialog(str: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("$str")
            .setCancelable(false)
            .setNegativeButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
        finish()
    }

    private fun decodeAndValidateCode(code: String, companyName: String): Boolean {
        return try {
            val decodedCode = String(android.util.Base64.decode(code, android.util.Base64.DEFAULT)).trim()

            // Extract the expected components
            val parts = decodedCode.split("-")
            if (parts.size == 3) {
                val decodedCompanyName = parts[0]
                val decodedDate = parts[1]

                // Check if company name and current date match
                val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                decodedCompanyName == companyName && decodedDate == currentDate
            } else {
                false
            }
        } catch (e: IllegalArgumentException) {
            false // Return false if decoding fails
        }
    }

    private fun checkLastPayTimestamp() {
        fetchCurrentTimeFromFirebase { currentTime ->
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val lastPayTimestamp = sharedPreferences.getLong(PAY_TIMESTAMP_KEY, 0L)

            // Check if 10 days (in milliseconds) have passed
            if (lastPayTimestamp == 0L || (currentTime - lastPayTimestamp) >= 28 * 24 * 60 * 60 * 1000L) {
                showPayDialog(currentTime)
            }
        }
    }

    private fun showPayDialog(currentTime: Long) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pay Bill")
            .setMessage("Buy Your Subscription")
            .setCancelable(false)
            .setPositiveButton("Pay") { _, _ ->
                showEmailPasswordDialog(currentTime)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        alertDialog = builder.create()
        alertDialog.show()
    }

//    private fun showEmailPasswordDialog(currentTime: Long) {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_email_password, null)
//        val emailField = dialogView.findViewById<EditText>(R.id.emailField)
//        val passwordField = dialogView.findViewById<EditText>(R.id.passwordField)
//
//        AlertDialog.Builder(this)
//            .setTitle("Enter Email and Password")
//            .setView(dialogView)
//            .setPositiveButton("Submit") { _, _ ->
//                val enteredEmail = emailField.text.toString().trim()
//                val enteredPassword = passwordField.text.toString().trim()
//                val description = enteredEmail
//                if (enteredEmail.isNotEmpty() && enteredPassword.isNotEmpty()) {
//                    // First, try to authenticate using Firebase
//                    firebaseAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                // Firebase authentication successful
//                                Toast.makeText(this, "1000", Toast.LENGTH_SHORT).show()
//                                payUsingUpi("Auto Dialer", description, "1000")
////                                handleUpiResponse("SUCCESS")
//                                //saveCurrentTimestamp(currentTime)
//                            } else {
//                                // Firebase authentication failed, check SharedPreferences
//                                authenticateWithSharedPreferences(enteredEmail, enteredPassword, currentTime)
//                            }
//                        }
//                } else {
//                    Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .setNegativeButton("Cancel") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .create()
//            .show()
//    }

    private fun showEmailPasswordDialog(currentTime: Long) {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_email_password, null)

        // Get references to the EditText fields and buttons
        val emailField = dialogView.findViewById<EditText>(R.id.emailField)
        val passwordField = dialogView.findViewById<EditText>(R.id.passwordField)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel)
        val payButton = dialogView.findViewById<Button>(R.id.pay)

        // Create the dialog with the custom layout
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Enter Email and Password")
            .setView(dialogView)
            .create()

        // Set up the Cancel button
        cancelButton.setOnClickListener {
            alertDialog.dismiss() // Dismiss the dialog when cancel is clicked
        }

        // Set up the Pay button
        payButton.setOnClickListener {
            val enteredEmail = emailField.text.toString().trim()
            val enteredPassword = passwordField.text.toString().trim()
            val description = enteredEmail

            if (enteredEmail.isNotEmpty() && enteredPassword.isNotEmpty()) {
                // Try to authenticate using Firebase
                firebaseAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Firebase authentication successful
                            //Toast.makeText(this, "1000", Toast.LENGTH_SHORT).show()
                            payUsingUpi("Auto Dial", description, "1000")
//                            handleUpiResponse("SUCCESS")
                            //saveCurrentTimestamp(currentTime)
                            alertDialog.dismiss() // Close dialog after success
                        } else {
                            // Firebase authentication failed, check SharedPreferences
                            authenticateWithSharedPreferences(enteredEmail, enteredPassword, currentTime)
                            alertDialog.dismiss() // Close dialog after failure
                        }
                    }
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun authenticateWithSharedPreferences(email: String, password: String, currentTime: Long) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")
        val userType = sharedPreferences.getString("user_type", "")
        val description = savedEmail.toString()
        if (email == savedEmail && password == savedPassword && userType == "individual") {
            //Toast.makeText(this, "100", Toast.LENGTH_SHORT).show()
            payUsingUpi("Auto Dial", description, "200")
            //saveCurrentTimestamp(currentTime)
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_LONG).show()
        }
    }


    private fun saveCurrentTimestamp(currentTime: Long) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong(PAY_TIMESTAMP_KEY, currentTime).apply()
    }

    private fun fetchCurrentTimeFromFirebase(onTimeFetched: (Long) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("server_time").setValue(ServerValue.TIMESTAMP)
        databaseReference.child("server_time").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val serverTimestamp = snapshot.getValue(Long::class.java)
                if (serverTimestamp != null) {
                    onTimeFetched(serverTimestamp)
                } else {
                    Toast.makeText(this@Login, "Unable to fetch server time.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Error fetching server time: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun payUsingUpi(name: String, description: String, amount: String) {
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
        if (response.contains("SUCCESS", true)) {
            Toast.makeText(this, "Transaction Successful", Toast.LENGTH_SHORT).show()
            val currentTime = System.currentTimeMillis()
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putLong(PAY_TIMESTAMP_KEY, currentTime).apply()
        } else if (response.contains("FAILURE", true)) {
            Toast.makeText(this, "Transaction Failed", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val UPI_PAYMENT = 1
    }

    private fun showInterstitialAd() {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null // Set to null so it can be reloaded later if needed
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
                }
            }
            ad.show(this)
        }
    }


}