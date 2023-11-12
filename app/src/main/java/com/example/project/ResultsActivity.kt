package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LOG_TAG = "_RESULTS PAGE"

class ResultsActivity : AppCompatActivity() {

    private lateinit var searchTermTextView : TextView
    private lateinit var savedPlacesBtn: Button
    private lateinit var mostReviewsBtn: RadioButton
    private lateinit var bestOverallBtn: RadioButton
    private lateinit var highestRatedBtn: RadioButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var resultsRecyclerView : RecyclerView
    private lateinit var progressBar : ProgressBar
    private lateinit var noPlaceFoundText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Init
        searchTermTextView = findViewById(R.id.queryText)
        savedPlacesBtn = findViewById(R.id.savedBtn)
        mostReviewsBtn = findViewById(R.id.mostReviewsBtn)
        bestOverallBtn = findViewById(R.id.bestOverallBtn)
        highestRatedBtn = findViewById(R.id.highestRatedBtn)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        noPlaceFoundText = findViewById(R.id.noPlaceText)
        radioGroup = findViewById(R.id.radioGroup)

        // TextView
        val query = intent.getStringExtra("QUERY").toString()
        searchTermTextView.text = query

        // Radio
        val queryType = intent.getIntExtra("RADIO", 1)
        setRadioButtons(queryType)

        getPlaces(query, queryType)
    }


    private fun getPlaces(query : String, queryType : Int) {
        Log.d(LOG_TAG, "getPlaces($query, $queryType)")

        progressBar.isVisible = true

        // Coroutines
        CoroutineScope(Dispatchers.IO).launch {
           val places = ApiManager(this@ResultsActivity).getPlaces(query, queryType)

            withContext(Dispatchers.Main) {
                if (places.isEmpty()) {
                    resultsRecyclerView.isVisible = false
                    noPlaceFoundText.isVisible = true
                } else {
                    // RecyclerView
                    resultsRecyclerView.adapter = PlacesAdapter(this@ResultsActivity, places)
                    resultsRecyclerView.layoutManager = LinearLayoutManager(this@ResultsActivity)
                }
                progressBar.isVisible = false
                mostReviewsBtn.setOnClickListener { updateRecyclerView(places, 1) }
                bestOverallBtn.setOnClickListener { updateRecyclerView(places, 2) }
                highestRatedBtn.setOnClickListener { updateRecyclerView(places, 3) }
            }
        }
    }


    private fun updateRecyclerView(places : MutableList<Place>, queryType : Int) {
        val newPlaces = ApiManager(this@ResultsActivity).sortPlaces(places, queryType)
        resultsRecyclerView.adapter = PlacesAdapter(this@ResultsActivity, newPlaces)
        resultsRecyclerView.layoutManager = LinearLayoutManager(this@ResultsActivity)
    }


    private fun setRadioButtons(index : Int) {
        if (index == 1) {
            mostReviewsBtn.isChecked = true
            bestOverallBtn.isChecked = false
            highestRatedBtn.isChecked = false
        } else if (index == 2) {
            mostReviewsBtn.isChecked = false
            bestOverallBtn.isChecked = true
            highestRatedBtn.isChecked = false
        } else if (index == 3) {
            mostReviewsBtn.isChecked = false
            bestOverallBtn.isChecked = false
            highestRatedBtn.isChecked = true
        }
    }
}