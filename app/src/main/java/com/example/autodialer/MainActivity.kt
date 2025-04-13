package com.example.autodialer

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val CALL_PHONE_PERMISSION = Manifest.permission.CALL_PHONE
    private val REQUEST_CALL_PERMISSION = 1
    private val REQUEST_CODE_PICK_FILE = 2
    private val phoneNumbers = mutableListOf<String>()
    private var currentIndex = 0
    private var isCallActive = false
    private lateinit var listViewNumbers: ListView
    private lateinit var textView2: TextView
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private lateinit var telephonyManager: TelephonyManager

    private val callStateCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_IDLE -> {
                        if (isCallActive) {
                            removeOverlay() // Remove overlay when the call ends
                            Log.e("connection", "Disconnected")
                           // Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show()
                        }
                        isCallActive = false
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        if (!isCallActive) {
                            showOverlay() // Show overlay when the call starts
                            Log.e("connection", "Connecting")
                            //Toast.makeText(this@MainActivity, "Connecting", Toast.LENGTH_SHORT).show()
                        }
                        isCallActive = true
                    }
                    else -> {
                        Log.e("connection", "Call state unknown")
                    }
                }
            }
        }
    } else {
        TODO("VERSION.SDK_INT < S")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.theme)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 123)
        } else {
            //showOverlay()
        }
        val btnStartDialing = findViewById<Button>(R.id.btnStartDialing)
        val btnUploadFile = findViewById<Button>(R.id.btnUploadFile)
        textView2=findViewById(R.id.textView2)
        listViewNumbers = findViewById(R.id.listViewNumbers)

        val adapter = object : ArrayAdapter<String>(this, R.layout.list_item_number, R.id.tvNumber, phoneNumbers) {
            override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.tvNumber)
                textView.text = phoneNumbers[position]
                val btnsave = view.findViewById<ImageButton>(R.id.btnSave1)
                val btn2 = view.findViewById<ImageButton>(R.id.button2)
                val btn3 = view.findViewById<Button>(R.id.button3)
                val cardView = view.findViewById<CardView>(R.id.cardViewTemplate)  // Assuming your card is a CardView

                if (position == 0) {
                    val redBorder = ContextCompat.getDrawable(context, R.drawable.red_border)  // Load the red border drawable
                    cardView?.background = redBorder  // Set the background with the red border
                    btnsave.visibility = View.VISIBLE
                    btn2.visibility = View.VISIBLE
                    btn3.visibility = View.VISIBLE
                } else {
                    // For all other cards, ensure the background color remains the same (#F3EFE6)
                    cardView?.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                }


                val button = view.findViewById<ImageButton>(R.id.btnSave1)
                button.setOnClickListener {
                    if (!isCallActive) {
                        showDatePicker { selectedDate ->
                            // Save the current number to the file with the selected date
                            saveNumberToFile(phoneNumbers[position], selectedDate)

                            // Remove the current card and call the next number
                            removeCard(position)
                            callNextNumber()
                            updateCount(textView2)
                        }
                    } else {
                        Toast.makeText(context, "Wait for the current call to end.", Toast.LENGTH_SHORT).show()
                    }
                }



//                val button = view.findViewById<ImageButton>(R.id.btnSave1)
//                button.setOnClickListener {
////                    saveNumberToFile(phoneNumbers[position])
//                    showDatePicker { selectedDate ->
//                        saveNumberToFile(phoneNumbers[position], selectedDate) // Replace "12345" with the actual number to save
//                    }
//                    if (!isCallActive) {
//                        removeCard(position)
//                        callNextNumber()
//                        updateCount(textView2)
//                    } else {
//                        Toast.makeText(context, "Wait for the current call to end.", Toast.LENGTH_SHORT).show()
//                    }
//                }
                val button2 = view.findViewById<ImageButton>(R.id.button2)
                button2.setOnClickListener {
                    saveNumberToFile2(phoneNumbers[position])
                    if (!isCallActive) {
                        removeCard(position)
                        callNextNumber()
                        updateCount(textView2)
                    } else {
                        Toast.makeText(context, "Wait for the current call to end.", Toast.LENGTH_SHORT).show()
                    }
                }
                val button3 = view.findViewById<Button>(R.id.button3)
                button3.setOnClickListener {
                    saveNumberToFile3(phoneNumbers[position])
                    if (!isCallActive) {
                        removeCard(position)
                        callNextNumber()
                        updateCount(textView2)
                    } else {
                        Toast.makeText(context, "Wait for the current call to end.", Toast.LENGTH_SHORT).show()
                    }
                }
                return view
            }
        }
        listViewNumbers.adapter = adapter


        btnUploadFile.setOnClickListener {
            pickFile()
        }

        btnStartDialing.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, CALL_PHONE_PERMISSION) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(CALL_PHONE_PERMISSION), REQUEST_CALL_PERMISSION)
            } else {
                startDialing()
            }
        }

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(mainExecutor, callStateCallback)
        } else {
            telephonyManager.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    super.onCallStateChanged(state, phoneNumber)
                    when (state) {
                        TelephonyManager.CALL_STATE_IDLE -> {
                            if (isCallActive) {
                                removeOverlay()
                                Log.e("connection", "Disconnected")
                               // Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show()
                            }
                            isCallActive = false
                        }
                        TelephonyManager.CALL_STATE_OFFHOOK -> {
                            if (!isCallActive) {
                                showOverlay()
                                Log.e("connection", "Connecting")
                               // Toast.makeText(this@MainActivity, "Connecting", Toast.LENGTH_SHORT).show()
                            }
                            isCallActive = true
                        }
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }

        val button7=findViewById<Button>(R.id.accept)
        button7.setOnClickListener{
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
        val button5=findViewById<Button>(R.id.dnp)
        button5.setOnClickListener{
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }
        val button4=findViewById<Button>(R.id.reject)
        button4.setOnClickListener{
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
        }

        MobileAds.initialize(this) {}

        val adView = findViewById<AdView>(R.id.adView2)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d%02d%02d", year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveNumberToFile(number: String, date: String) {
        try {
            val fileName = "Followup_$date.txt"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)

                if (file.exists()) {
                    contentResolver.openOutputStream(Uri.fromFile(file), "wa")?.use { outputStream ->
                        outputStream.write("$number\n".toByteArray())
                    }
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number appended to: $filePath")
                } else {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    }

                    val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write("$number\n".toByteArray())
                        }
                        val filePath = it.toString()
                        Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                        Log.d("FileSave", "Number saved to: $filePath")
                    }
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)

                if (file.exists()) {
                    file.appendText("$number\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number appended to: $filePath")
                } else {
                    file.createNewFile()
                    file.appendText("$number\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number saved to: $filePath")
                }
            }
        } catch (e: Exception) {
            //Toast.makeText(this, "Failed to save number: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("FileSave", "Error saving number: ${e.message}", e)
        }
    }



//    private fun saveNumberToFile(number: String) {
//        try {
//            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
//            val fileName = "Accept_$date.txt"
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//                val file = File(documentsDir, fileName)
//
//                if (file.exists()) {
//                    contentResolver.openOutputStream(Uri.fromFile(file), "wa")?.use { outputStream ->
//                        outputStream.write("$number\n".toByteArray())
//                    }
//                    val filePath = file.absolutePath
//                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_SHORT).show()
//                    Log.d("FileSave", "Number appended to: $filePath")
//                } else {
//                    val contentValues = ContentValues().apply {
//                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
//                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
//                    }
//
//                    val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
//                    uri?.let {
//                        contentResolver.openOutputStream(it)?.use { outputStream ->
//                            outputStream.write("$number\n".toByteArray())
//                        }
//                        val filePath = it.toString()
//                        Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_SHORT).show()
//                        Log.d("FileSave", "Number saved to: $filePath")
//                    }
//                }
//            } else {
//                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//                val file = File(documentsDir, fileName)
//
//                if (file.exists()) {
//                    file.appendText("$number\n")
//                    val filePath = file.absolutePath
//                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_SHORT).show()
//                    Log.d("FileSave", "Number appended to: $filePath")
//                } else {
//                    file.createNewFile()
//                    file.appendText("$number\n")
//                    val filePath = file.absolutePath
//                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_SHORT).show()
//                    Log.d("FileSave", "Number saved to: $filePath")
//                }
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "Failed to save number: ${e.message}", Toast.LENGTH_SHORT).show()
//            Log.e("FileSave", "Error saving number: ${e.message}", e)
//        }
//    }

    private fun saveNumberToFile2(number: String) {
        try {
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val fileName = "Reject_$date.txt"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)

                if (file.exists()) {
                    contentResolver.openOutputStream(Uri.fromFile(file), "wa")?.use { outputStream ->
                        outputStream.write("$number\n".toByteArray())
                    }
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number appended to: $filePath")
                } else {
                    // Create a new file if it doesn't exist
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    }

                    val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write("$number\n".toByteArray())
                        }
                        val filePath = it.toString()
                        Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                        Log.d("FileSave", "Number saved to: $filePath")
                    }
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)

                if (file.exists()) {
                    file.appendText("$number\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number appended to: $filePath")
                } else {
                    // Create a new file if it doesn't exist
                    file.createNewFile()
                    file.appendText("$number\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number saved to: $filePath")
                }
            }
        } catch (e: Exception) {
           // Toast.makeText(this, "Failed to save number: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("FileSave", "Error saving number: ${e.message}", e)
        }
    }

    private fun saveNumberToFile3(number: String) {
        try {
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val fileName = "DNP_$date.txt"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)

                if (file.exists()) {
                    contentResolver.openOutputStream(Uri.fromFile(file), "wa")?.use { outputStream ->
                        outputStream.write("$number\n".toByteArray())
                    }
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number appended to: $filePath")
                } else {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    }

                    val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write("$number\n".toByteArray())
                        }
                        val filePath = it.toString()
                        Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                        Log.d("FileSave", "Number saved to: $filePath")
                    }
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, fileName)

                if (file.exists()) {
                    file.appendText("$number\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number appended to: $filePath")
                } else {
                    file.createNewFile()
                    file.appendText("$number\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Number saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Number saved to: $filePath")
                }
            }
        } catch (e: Exception) {
            //Toast.makeText(this, "Failed to save number: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("FileSave", "Error saving number: ${e.message}", e)
        }
    }




    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/plain"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                loadPhoneNumbersFromFile(uri)
            }
        }
        if (requestCode == 123) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                //showOverlay()
            } else {
                Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPhoneNumbersFromFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.let { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                phoneNumbers.clear()
                reader.useLines { lines ->
                    lines.forEach { line ->
                        phoneNumbers.add(line.trim())
                    }
                }
                // Notify the adapter that data has changed
                (listViewNumbers.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                updateCount(textView2)
               // Toast.makeText(this, "Loaded ${phoneNumbers.size} numbers.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startDialing() {
        currentIndex = 0
        callNextNumber()
    }

    private fun callNextNumber() {
        if (currentIndex < phoneNumbers.size) {
            val phoneNumber = phoneNumbers[currentIndex]
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            try {
                startActivity(callIntent)
            } catch (e: SecurityException) {
                Toast.makeText(this, "Permission denied for calling.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "All calls completed.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startDialing()
        } else {
            Toast.makeText(this, "Permission required to make calls.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateCount(textView: TextView) {
        textView.text = "Total Numbers: ${phoneNumbers.size}"
    }

    private fun removeCard(position: Int) {
        if (position >= 0 && position < phoneNumbers.size) {
            phoneNumbers.removeAt(position)
            (listViewNumbers.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            Log.e("RemoveCard", "Removing item at position: $position")
        }
    }

    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = layoutInflater.inflate(R.layout.overlay_layout, null)

        val editText = overlayView?.findViewById<EditText>(R.id.editTextOverlay)
        val layoutParams = editText?.layoutParams
        layoutParams?.height = 200 // Set height to 200px
        editText?.layoutParams = layoutParams
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = 0

        windowManager?.addView(overlayView, params)
        val editTextOverlay = overlayView?.findViewById<EditText>(R.id.editTextOverlay)
        editTextOverlay?.apply {
            isFocusableInTouchMode = true
            isFocusable = true
            post {
                requestFocus()
            }
        }
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        windowManager?.updateViewLayout(overlayView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            val editText = it.findViewById<EditText>(R.id.editTextOverlay)
            val enteredText = editText.text.toString()
            val currentNumber = phoneNumbers[currentIndex]
            saveTextToFile(enteredText, currentNumber)

            // Remove the overlay
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    private fun saveTextToFile(text: String, fileName: String) {
        try {
            val sanitizedFileName = "$fileName.txt"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, sanitizedFileName)

                if (file.exists()) {
                    contentResolver.openOutputStream(Uri.fromFile(file), "wa")?.use { outputStream ->
                        outputStream.write("$text\n".toByteArray())
                    }
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Text saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Text appended to: $filePath")
                } else {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, sanitizedFileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    }

                    val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write("$text\n".toByteArray())
                        }
                        val filePath = it.toString()
                        Toast.makeText(this, "Text saved: $filePath", Toast.LENGTH_LONG).show()
                        Log.d("FileSave", "Text saved to: $filePath")
                    }
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDir, sanitizedFileName)

                if (file.exists()) {
                    file.appendText("$text\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Text saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Text appended to: $filePath")
                } else {
                    file.createNewFile()
                    file.appendText("$text\n")
                    val filePath = file.absolutePath
                    Toast.makeText(this, "Text saved: $filePath", Toast.LENGTH_LONG).show()
                    Log.d("FileSave", "Text saved to: $filePath")
                }
            }
        } catch (e: Exception) {
            //Toast.makeText(this, "Failed to save text: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("FileSave", "Error saving text: ${e.message}", e)
        }
    }

}