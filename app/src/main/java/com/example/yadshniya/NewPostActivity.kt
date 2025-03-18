package com.example.yadshniya

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.Model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject


class NewPostActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val TAG = "NewPostActivity"  // Define a tag for filtering logs
    private lateinit var pickImageButton: ImageButton

    private lateinit var imageSelectionCallBack: ActivityResultLauncher<Intent>
    private var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)
        setUi()

        val descriptionEditText = findViewById<EditText>(R.id.ed1)
        val pickPriceButton = findViewById<Button>(R.id.pickPrice)
        val priceRecommendationTextView = findViewById<TextView>(R.id.txt3)

        val postButton = findViewById<Button>(R.id.postButton)
        postButton.setOnClickListener() {
            post()
        }

        // Initially, set the Pick Price button visibility to GONE
        pickPriceButton.visibility = View.GONE
        priceRecommendationTextView.visibility = View.GONE

        // Add TextWatcher to the description EditText
        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Check if description is not empty
                if (s != null && s.isNotEmpty()) {
                    pickPriceButton.visibility = View.VISIBLE // Show the button
                    priceRecommendationTextView.visibility = View.VISIBLE
                } else {
                    pickPriceButton.visibility = View.GONE // Hide the button
                    priceRecommendationTextView.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        pickPriceButton.setOnClickListener {
            val description = descriptionEditText.text.toString()
            if (description.isNotEmpty()) {
                fetchPriceRecommendation(description, priceRecommendationTextView)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
      private fun fetchPriceRecommendation(itemDescription: String, textView: TextView) {

        val apiKey = BuildConfig.GEMINI_API_KEY
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "Item: $itemDescription. Provide a price range in shekels, for example: 100₪-200₪, with no description. if you don't have an answer like this return \"refine description\"")
                                })
                            })
                        })
                    })
                }

                val body = requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-thinking-exp:generateContent?key=${apiKey}") // Replace with your API key!
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    Log.d("API Response", jsonResponse.toString())

                    val candidates = jsonResponse.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        if (parts.length() > 0) {
                            val recommendedPrice = parts.getJSONObject(0).getString("text")

                            withContext(Dispatchers.Main) {
                                textView.text = recommendedPrice
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                textView.text = "No text found in response"
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            textView.text = "No candidates found"
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Failed to fetch price. Response code: ${response.code}")
                        textView.text = "Failed to fetch price. Response code: ${response.code}"

                    }
                    Log.e("API Error", "Response code: ${response.code}, body: $responseBody")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    textView.text = "Error fetching price: ${e.message}"
                }
                Log.e("API Exception", "Error: ${e.message}", e)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        imageSelectionCallBack.launch(intent)
    }

    private fun getImageSize(uri: Uri?): Long {
        val inputStream = contentResolver.openInputStream(uri!!)
        return inputStream?.available()?.toLong() ?: 0
    }

    private fun defineImageSelectionCallBack() {
        imageSelectionCallBack =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                try {
                    val imageUri: Uri? = result.data?.data
                    if (imageUri != null) {
                        val imageSize = getImageSize(imageUri)
                        val maxCanvasSize = 5 * 1024 * 1024 // 5MB
                        if (imageSize > maxCanvasSize) {
                            Toast.makeText(
                                this@NewPostActivity,
                                "Selected image is too large",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            pickImageButton.setImageURI(imageUri)
                            imageURI = imageUri
                            pickImageButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))

                        }

                    } else {
                        Toast.makeText(this@NewPostActivity, "No Image Selected", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@NewPostActivity, "Error processing result", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setUi (){
        pickImageButton = findViewById(R.id.PicButtonNewPostScreen)

        defineImageSelectionCallBack()
        pickImageButton.setOnClickListener {
            Log.i("buttonClick", "pick profile pick button in signup screen clicked")
            openGallery()
        }

    }

    private fun post (){
        val description = findViewById<EditText>(R.id.ed1).text.toString()
        val price = findViewById<TextView>(R.id.ed3).text.toString()
        val location = findViewById<EditText>(R.id.ed2).text.toString()
        val img = findViewById<ImageButton>(R.id.PicButtonNewPostScreen)

        if (description.isEmpty() || price.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        } else {

//            TODO: add user
            val drawable = img.drawable
            if (drawable is BitmapDrawable) {
                val imageBitmap = drawable.bitmap
                instance().createPost(Post(description, location, price), imageBitmap) {

                }
            }
        }

    }



}