package com.example.project

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject

private const val LOG_TAG = "_API MANAGER"

class ApiManager(appContext : Context) {
    private val okHttpClient : OkHttpClient
    private val googleApiKey = appContext.getString(R.string.google_api_key)
    private val geocodingApiKey = appContext.getString(R.string.geocoding_api_key)


    init {
        val builder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)
        okHttpClient = builder.build()
    }


    fun getPlaces(query : String, queryType : Int) : MutableList<Place> {
        val latLng = getLatLong(query)
        return getPlacesSearch(query, queryType, latLng)
    }


    private fun getPlacesSearch(query : String, queryType : Int, latLng : List<Double>) : MutableList<Place> {
        val places : MutableList<Place> = mutableListOf()
        val currentIds : MutableSet<String> = mutableSetOf()

        val urlText = "https://places.googleapis.com/v1/places:searchText"
        val jsonText = """
            {
                "textQuery" : "Popular places in $query",
                "rankPreference" : "RELEVANCE",
                "minRating" : 4.0,
                "maxResultCount" : 20
            }
            """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = jsonText.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(urlText)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", googleApiKey)
            .addHeader(
                "X-Goog-FieldMask",
                "places.displayName.text,places.photos,places.formattedAddress,places.id,places.rating,places.userRatingCount,places.googleMapsUri"
            )
            .build()

        val response = okHttpClient.newCall(request).execute()
        Log.d(LOG_TAG, "getPlaces Response: $response")

        if (!response.isSuccessful) {
            Log.d(LOG_TAG, "getPlaces UNSUCCESSFUL")
            Log.d(LOG_TAG, response.message)
        } else {
            val responseBody : String ? = response.body?.string()
            val placesJson = JSONObject(responseBody).getJSONArray("places")

            for (i in 0 until placesJson.length()){
                val currentPlaceJson = placesJson.getJSONObject(i)

                val name = currentPlaceJson.getJSONObject("displayName").getString("text")
                val id = currentPlaceJson.getString("id")
                val rating = currentPlaceJson.getString("rating").toDouble()
                val reviewsCount = currentPlaceJson.getString("userRatingCount").toInt()
                val score = (rating * reviewsCount).toInt()
                val imageName = currentPlaceJson.getJSONArray("photos").getJSONObject(0).getString("name")
                val address  = currentPlaceJson.getString("formattedAddress")
                val gMapsUrl = currentPlaceJson.getString("googleMapsUri")

                val currentPlace = Place(name, id, rating, reviewsCount, score, imageName, address, gMapsUrl, null)

                if (currentIds.add(id)) {
                    places.add(currentPlace)
                }
            }
        }


        val urlLocation = "https://places.googleapis.com/v1/places:searchNearby"
        val jsonLoc = """
            {
                "rankPreference" : "POPULARITY",
                "maxResultCount" : 20,
                "locationRestriction": {
                    "circle": {
                      "center": {
                        "latitude": ${latLng[0]},
                        "longitude": ${latLng[1]}
                      },
                      "radius": 15000.0
                    }
                  }
            }
            """.trimIndent()

        val mediaTypeLoc = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBodyLoc = jsonLoc.toRequestBody(mediaTypeLoc)

        val requestLoc = Request.Builder()
            .url(urlLocation)
            .post(requestBodyLoc)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", googleApiKey)
            .addHeader(
                "X-Goog-FieldMask",
                "places.displayName.text,places.photos,places.formattedAddress,places.id,places.rating,places.userRatingCount,places.googleMapsUri"
            )
            .build()

        val responseLoc = okHttpClient.newCall(requestLoc).execute()
        Log.d(LOG_TAG, "getPlaces Response: $responseLoc")

        if (!responseLoc.isSuccessful) {
            Log.d(LOG_TAG, "getPlaces UNSUCCESSFUL")
            Log.d(LOG_TAG, responseLoc.message)
        } else {
            val responseLocBody : String ? = responseLoc.body?.string()
            Log.d(LOG_TAG, responseLocBody.toString())
            val placesLocJson = JSONObject(responseLocBody).getJSONArray("places")

            for (i in 0 until placesLocJson.length()){
                val currentPlaceLocJson = placesLocJson.getJSONObject(i)

                val name = currentPlaceLocJson.getJSONObject("displayName").getString("text")
                val id = currentPlaceLocJson.getString("id")
                val ratingString = currentPlaceLocJson.optString("rating")
                val rating = if (ratingString.isNotBlank()) {
                    ratingString.toDouble()
                } else { 0.0 }
                val reviewsCountString = currentPlaceLocJson.optString("userRatingCount")
                val reviewsCount = if (reviewsCountString.isNotBlank()) {
                    reviewsCountString.toInt()
                } else { 0 }
                val score = (rating * reviewsCount).toInt()
                val imageName = currentPlaceLocJson.getJSONArray("photos").getJSONObject(0).getString("name")
                val address  = currentPlaceLocJson.getString("formattedAddress")
                val gMapsUrl = currentPlaceLocJson.getString("googleMapsUri")

                val currentPlaceLoc = Place(name, id, rating, reviewsCount, score, imageName, address, gMapsUrl, null)

                if (currentIds.add(id)) {
                    places.add(currentPlaceLoc)
                }
            }
        }
        return sortPlaces(places, queryType)
    }


    private fun getLatLong(city : String) : List<Double> {
        val url = "https://api.api-ninjas.com/v1/geocoding?city=$city"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("X-Api-Key", geocodingApiKey)
            .build()

        val response = okHttpClient.newCall(request).execute()
        Log.d(LOG_TAG, "getLatLong Response: $response")
        if (!response.isSuccessful) {
            Log.d(LOG_TAG, "getLatLong UNSUCCESSFUL")
            return emptyList()
        } else {
            val responseBody : String ? = response.body?.string()
            Log.d(LOG_TAG, responseBody.toString())
            val location = JSONArray(responseBody).getJSONObject(0)
            return listOf(location.getDouble("latitude"), location.getDouble("longitude"))
        }
    }


    fun sortPlaces(places : MutableList<Place>, queryType: Int) : MutableList<Place>{
        if (queryType == 1) {
            places.sortWith(compareByDescending { it.reviewsCount })
        } else if (queryType == 2) {
            places.sortWith(compareByDescending { it.score })
        } else if (queryType == 3) {
            places.sortWith(compareByDescending { it.rating })
        }
        return places
    }


    fun getPlacePhoto(imageName : String) : String {
        return "https://places.googleapis.com/v1/$imageName/media?key=$googleApiKey&maxHeightPx=150&maxWidthPx=150"
    }
}