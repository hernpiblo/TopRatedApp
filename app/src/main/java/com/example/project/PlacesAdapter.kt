package com.example.project

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LOG_TAG = "_PLACES ADAPTER"

class PlacesAdapter(private val appContext : Context, private val places: List<Place>) : RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {
    class ViewHolder(rootLayout: View): RecyclerView.ViewHolder(rootLayout) {
        val placeIndex : TextView = rootLayout.findViewById(R.id.placeIndex)
        val placeName : TextView = rootLayout.findViewById(R.id.placeName)
        val placeRating : TextView = rootLayout.findViewById(R.id.placeRating)
        val placeReviewsCount : TextView = rootLayout.findViewById(R.id.placeReviewsCount)
        val placeScore : TextView = rootLayout.findViewById(R.id.placeScore)
        val placeAddress : TextView = rootLayout.findViewById(R.id.placeAddress)
        val placeImage : ImageView = rootLayout.findViewById(R.id.placeImage)
        val placeImageProgressBar : ProgressBar = rootLayout.findViewById(R.id.imageProgressBar)
        val placeSaveBtn : ToggleButton = rootLayout.findViewById(R.id.placeSaveBtn2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(LOG_TAG, "inside onCreateViewHolder")
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val rootLayout: View = layoutInflater.inflate(R.layout.place_card_layout, parent, false)
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(LOG_TAG, "inside onBindViewHolder on position $position")
        val currentPlace = places[position]

        holder.placeIndex.text = (position + 1).toString()
        holder.placeName.text = currentPlace.name
        holder.placeAddress.text = currentPlace.address
        holder.placeRating.text = "Rating:      ${currentPlace.rating}"
        holder.placeReviewsCount.text = "Reviews:  ${currentPlace.reviewsCount}"
        holder.placeScore.text = "Score:        ${currentPlace.score}"

        // Image
        if (currentPlace.imageName.isNotEmpty()) {
            holder.placeImageProgressBar.isVisible = true
            if (currentPlace.imageName == "null") {
                holder.placeImage.setImageResource(R.drawable.missing_image)
            } else {
                Picasso.get().setIndicatorsEnabled(true)
                Picasso.get().load(ApiManager(appContext).getPlacePhoto(currentPlace.imageName)).into(holder.placeImage)
            }
            holder.placeImageProgressBar.isVisible = false
        }

        // onClick open Google Maps
        holder.itemView.setOnClickListener {
            Log.d(LOG_TAG, "Selected $currentPlace")
            val url =
                if (!currentPlace.gMapsUrl.startsWith("http://") && !currentPlace.gMapsUrl.startsWith("https://")) {
                    "http://${currentPlace.gMapsUrl}"
                } else {
                    currentPlace.gMapsUrl
                }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            appContext.startActivity(intent)
            Toast.makeText(appContext, "Opening Google Maps: $url", Toast.LENGTH_LONG).show()
        }

        // Save Button
        holder.placeSaveBtn.isChecked = false
        holder.placeSaveBtn.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(appContext, R.drawable.unsaved), null, null)
        holder.placeSaveBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.placeSaveBtn.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(appContext, R.drawable.saved), null, null)
            } else {
                holder.placeSaveBtn.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(appContext, R.drawable.unsaved), null, null)
            }
        }
    }
}