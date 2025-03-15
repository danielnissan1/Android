package com.example.yadshniya

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlin.math.log

class NewPostActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val TAG = "NewPostActivity"  // Define a tag for filtering logs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)

        val descriptionEditText = findViewById<EditText>(R.id.ed1)
        val pickPriceButton = findViewById<Button>(R.id.pickPrice)
        val priceRecommendationTextView = findViewById<TextView>(R.id.txt3)

        listModels()

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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "Item: $itemDescription. Provide a price range in shekels, for example: 100₪-200₪, with no description. if you don't have an answer like this return \"please refine your description\"")
                                })
                            })
                        })
                    })
                }

                val body = requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-thinking-exp:generateContent?key=AIzaSyCUJ6OmWfZqX1biRXhxLMiC4x1Bjke9608") // Replace with your API key!
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

    private fun listModels() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models?key=AIzaSyCUJ6OmWfZqX1biRXhxLMiC4x1Bjke9608") // Replace with your API key!
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    Log.d("List Models", responseBody)
                } else {
                    Log.e("List Models Error", "Response code: ${response.code}, body: $responseBody")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("List Models Exception", "Error: ${e.message}", e)
            }
        }
    }
}