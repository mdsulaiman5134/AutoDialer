package com.example.autodialer

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity3 : AppCompatActivity() {

    private lateinit var listViewAccept: ListView
    private lateinit var acceptAdapter: ArrayAdapter<String>
    private val acceptList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.theme)
        }
        val button7=findViewById<Button>(R.id.home)
        button7.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) // Apply animation
        }
        val button5=findViewById<Button>(R.id.accept)
        button5.setOnClickListener{
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) // Apply animation

        }
        val button4=findViewById<Button>(R.id.reject)
        button4.setOnClickListener{
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
        }
        listViewAccept = findViewById(R.id.listViewNumbers)
        acceptAdapter = object : ArrayAdapter<String>(
            this,
            R.layout.list_item_number,
            R.id.tvNumber,
            acceptList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val cardView = view.findViewById<CardView>(R.id.cardViewTemplate)
                // Customize CardView if needed (e.g., set background color)
                cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                return view
            }
        }

        listViewAccept.adapter = acceptAdapter
        loadAcceptData()
    }

    private fun loadAcceptData() {
        try {
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val fileName = "DNP_$date.txt"
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(documentsDir, fileName)

            if (file.exists()) {
                file.bufferedReader().useLines { lines ->
                    acceptList.clear()
                    lines.forEach { line ->
                        acceptList.add(line.trim())
                    }
                }
                acceptAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Data loaded successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "File not found: $fileName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun locateFile(phoneNumber: String?): String? {
        if (phoneNumber == null) return null

        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        documentsDir.listFiles()?.forEach { file ->
            if (file.name.contains(phoneNumber)) {
                return file.name
            }
        }
        return null
    }
}