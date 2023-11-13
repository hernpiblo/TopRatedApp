package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val LOG_TAG = "_SAVED PAGE"

class SavedActivity : AppCompatActivity() {

    private lateinit var nameTextView : TextView
    private lateinit var savedRecyclerView : RecyclerView
    private lateinit var firebaseDatabase : FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var nameSort: RadioButton
    private lateinit var timeSort: RadioButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var sortOrderToggle: ToggleButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        // Init
        nameTextView = findViewById(R.id.nameTextView)
        savedRecyclerView = findViewById(R.id.savedRecyclerView)
        nameSort = findViewById(R.id.radioButtonName)
        timeSort = findViewById(R.id.radioButtonTime)
        radioGroup = findViewById(R.id.radioGroup)
        sortOrderToggle = findViewById(R.id.sortBtn)
        progressBar = findViewById(R.id.progressBarSaved)
        firebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseDb = firebaseDatabase.getReference("Places")
        firebaseAuth = FirebaseAuth.getInstance()

        nameTextView.text = "${firebaseAuth.currentUser!!.displayName}'s"


        var sortByName = true
        var sortIncreasing = true
        var places = mutableListOf<Place>()

        sortOrderToggle.setOnClickListener {
            places = sortPlaces(places, sortByName, !sortIncreasing)
            savedRecyclerView.adapter = PlacesAdapter(this@SavedActivity, places, firebaseDb)
            savedRecyclerView.layoutManager = LinearLayoutManager(this@SavedActivity)
            if (sortIncreasing) {
                sortOrderToggle.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(this, R.drawable.arrow_down), null, null)
            } else {
                sortOrderToggle.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(this, R.drawable.arrow_up), null, null)
            }
            sortIncreasing = !sortIncreasing
        }

        nameSort.setOnClickListener {
            if (!sortByName) {
                places = sortPlaces(places, true, sortIncreasing)
                savedRecyclerView.adapter = PlacesAdapter(this@SavedActivity, places, firebaseDb)
                savedRecyclerView.layoutManager = LinearLayoutManager(this@SavedActivity)
            }
            sortByName = true
        }

        timeSort.setOnClickListener {
            if (sortByName) {
                places = sortPlaces(places, false, sortIncreasing)
                savedRecyclerView.adapter = PlacesAdapter(this@SavedActivity, places, firebaseDb)
                savedRecyclerView.layoutManager = LinearLayoutManager(this@SavedActivity)
            }
            sortByName = false
        }

        // Populate RecyclerView
        firebaseDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressBar.isVisible = true
                for (snapshot in dataSnapshot.children) {
                    val name = snapshot.child("name").value.toString()
                    val id = snapshot.child("id").value.toString()
                    val rating = snapshot.child("rating").value.toString().toDouble()
                    val reviewsCount = snapshot.child("reviewsCount").value.toString().toInt()
                    val score = snapshot.child("score").value.toString().toInt()
                    val imageName = snapshot.child("imageName").value.toString()
                    val address = snapshot.child("address").value.toString()
                    val gMapsUrl = snapshot.child("gMapsUrl").value.toString()
                    val timestamp = snapshot.child("savedTimestamp").value.toString().toLong()
                    val place = Place(
                        name,
                        id,
                        rating,
                        reviewsCount,
                        score,
                        imageName,
                        address,
                        gMapsUrl,
                        timestamp
                    )
                    places.add(place)
                }

                places = sortPlaces(places, sortByName, sortIncreasing)

                savedRecyclerView.adapter = PlacesAdapter(this@SavedActivity, places, firebaseDb)
                savedRecyclerView.layoutManager = LinearLayoutManager(this@SavedActivity)
                progressBar.isVisible = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(LOG_TAG, "Error loading places", databaseError.toException())
            }
        })
    }

    fun sortPlaces(places : MutableList<Place>, sortByName : Boolean, sortIncreasing : Boolean) : MutableList<Place> {
        if (sortByName && sortIncreasing) {
            places.sortWith(compareBy{ it.name })
        } else if (!sortByName && sortIncreasing) {
            places.sortWith(compareBy{ it.timestamp })
        } else if (sortByName && !sortIncreasing) {
            places.sortWith(compareByDescending{ it.name })
        } else if (!sortByName && !sortIncreasing) {
            places.sortWith(compareByDescending{ it.timestamp })
        }
        return places
    }
}