package com.example.autodialer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.theme)
        }
        val etCompany = findViewById<EditText>(R.id.etCompany)
        val linkTextView = findViewById<TextView>(R.id.link)
        val btnCreate = findViewById<Button>(R.id.btnCreate)

        val button=findViewById<Button>(R.id.button)
        button.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        btnCreate.setOnClickListener {
            val companyName = etCompany.text.toString().trim()
            if (companyName.isEmpty()) {
                etCompany.error = "Company name cannot be empty"
                return@setOnClickListener
            }

            // Get current date
            val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

            // Generate a random string
            val randomString = generateRandomString(8)

            // Combine company name, current date, and random string
            val uniqueCode = "$companyName-$currentDate-$randomString"

            // Encrypt the code (you can customize the encryption logic)
            val encryptedCode = encodeCode(uniqueCode)

            // Display the encrypted code in the TextView
            linkTextView.text = encryptedCode
        }

        val shareButton = findViewById<Button>(R.id.btnShare)

        shareButton.setOnClickListener {
            val linkText = linkTextView.text.toString()

            if (linkText.isNotBlank()) {
                // Create share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, linkText)
                }

                // Launch chooser
                startActivity(Intent.createChooser(shareIntent, "Share link via"))
            } else {
                Toast.makeText(this, "Link is empty. Please create one first.", Toast.LENGTH_SHORT).show()
            }
        }



    }

    // Function to generate a random alphanumeric string
    private fun generateRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    // Function to encode the code (basic example using Base64, customize as needed)
    // Function to encode the code
    private fun encodeCode(code: String): String {
        return android.util.Base64.encodeToString(code.toByteArray(), android.util.Base64.DEFAULT).trim()
    }


    // Placeholder for edge-to-edge enabling (implement as needed)
    private fun enableEdgeToEdge() {
        // Customize your edge-to-edge functionality
    }
}
