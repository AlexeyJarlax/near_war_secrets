package com.pavlov.MyShadowGallery.util

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pavlov.MyShadowGallery.OnTrackItemClickListener
import com.pavlov.MyShadowGallery.AdapterForAPITracks
import com.pavlov.MyShadowGallery.TrackData

class AdapterForHistoryTracks(
    private val context: Context,
    private val trackItemClickListener: OnTrackItemClickListener
) {

    private val appPreferencesMethods = APKM(context)

    private val adapterForHistoryTracks: AdapterForAPITracks =
        AdapterForAPITracks(context, mutableListOf(), trackItemClickListener)

    fun setRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = adapterForHistoryTracks
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun saveTrack(
        trackName: String,
        artistName: String,
        trackTimeMillis: Long,
        artworkUrl100: String
    ) {
        val trackList = getTrackListFromSharedPreferences()
        trackList.removeAll { it.trackName == trackName && it.artistName == artistName }
        trackList.add(0, Track(trackName, artistName, trackTimeMillis, artworkUrl100))
        if (trackList.size > APK.HISTORY_TRACK_LIST_SIZE) {
            trackList.subList(APK.HISTORY_TRACK_LIST_SIZE, trackList.size).clear()
        }

        saveTrackListToSharedPreferences(trackList)
        adapterForHistoryTracks.updateList(trackList.map { it.toTrackData() })
    }

    fun syncTracks() {
        val trackList = getTrackListFromSharedPreferences()
        if (trackList.isNotEmpty()) {
            adapterForHistoryTracks.updateList(trackList.map { it.toTrackData() })
        }
    }

    private fun saveTrackListToSharedPreferences(trackList: List<Track>) {
        val jsonString = Gson().toJson(trackList)
        appPreferencesMethods.saveObjectToSharedPreferences(APK.KEY_HISTORY_LIST, jsonString)
    }

//    private fun getTrackListFromSharedPreferences(): MutableList<Track> {
//        val jsonString = appPreferencesMethods.getObjectFromSharedPreferences<String>(APK.KEY_HISTORY_LIST)
//        val type = object : TypeToken<List<Track>>() {}.type
//        return Gson().fromJson(jsonString, type) ?: mutableListOf()
//    }

    private fun getTrackListFromSharedPreferences(): MutableList<Track> {
        val jsonString = appPreferencesMethods.getObjectFromSharedPreferences<String>(APK.KEY_HISTORY_LIST)
        val typeToken = object : TypeToken<List<Track>>() {}.type
        return Gson().fromJson(jsonString, typeToken) ?: mutableListOf()
    }

    fun clearHistoryList() {
        adapterForHistoryTracks.clearList()
        adapterForHistoryTracks.notifyDataSetChanged()
    }

    fun killHistoryList() {
        appPreferencesMethods.delStringFromSharedPreferences(APK.KEY_HISTORY_LIST)
    }

    data class Track(
        val trackName: String,
        val artistName: String,
        val trackTimeMillis: Long,
        val artworkUrl100: String
    ) {
        fun toTrackData() = TrackData(trackName, artistName, trackTimeMillis, artworkUrl100)
    }
}