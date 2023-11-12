package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton

private const val LOG_TAG = "_HOME PAGE"

class HomeActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchBtn: Button
    private lateinit var mostReviewsBtn: RadioButton
    private lateinit var bestOverallBtn: RadioButton
    private lateinit var highestRatedBtn: RadioButton
    private lateinit var savedPlacesBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Init
        searchEditText = findViewById(R.id.searchEditText)
        searchBtn = findViewById(R.id.searchBtn)
        mostReviewsBtn = findViewById(R.id.mostReviewsBtn)
        bestOverallBtn = findViewById(R.id.bestOverallBtn)
        highestRatedBtn = findViewById(R.id.highestRatedBtn)
        savedPlacesBtn = findViewById(R.id.savedPlacedBtn)
        val sharedPrefs = getSharedPreferences("HomeActivity", MODE_PRIVATE)

        searchEditText.addTextChangedListener(textWatcher)
        searchEditText.setText(sharedPrefs.getString("QUERY", "").toString())

        searchBtn.setOnClickListener{
            val resultsActivity = Intent(this@HomeActivity, ResultsActivity::class.java)

            val query = searchEditText.text.toString()
            resultsActivity.putExtra("QUERY", query)
            sharedPrefs.edit().putString("QUERY", query).apply()

            if (mostReviewsBtn.isChecked) {
                resultsActivity.putExtra("RADIO", 1)
            } else if (bestOverallBtn.isChecked) {
                resultsActivity.putExtra("RADIO", 2)
            } else if (highestRatedBtn.isChecked) {
                resultsActivity.putExtra("RADIO", 3)
            }

            startActivity(resultsActivity)
        }

        savedPlacesBtn.setOnClickListener {
//            startActivity(Intent(this@HomeActivity, SavedPlacesActivity::class.java))
        }
    }


    private val textWatcher : TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val query : String = searchEditText.text.toString()
            searchBtn.isEnabled = query.isNotBlank()
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}
