package com.pavlov.MyShadowGallery

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.pavlov.MyShadowGallery.file.ArtistNaming
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import com.pavlov.MyShadowGallery.util.AdapterForHistoryTracks
import com.pavlov.MyShadowGallery.util.ThemeManager
import java.util.concurrent.TimeUnit
import android.view.animation.AnimationUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SearchActivity : AppCompatActivity() {
    private lateinit var backgroundView: ImageView
    private var clickCounter = 0
    private lateinit var sharedPreferences: SharedPreferences
    private val iTunesSearch = "https://itunes.apple.com"
    private val retrofit =
        Retrofit.Builder().baseUrl(iTunesSearch).addConverterFactory(GsonConverterFactory.create())
            .build()
    private val iTunesSearchAPI = retrofit.create(iTunesApiService::class.java)
    private var hasFocus = true
    private lateinit var queryInput: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var searchIcon: ImageButton
    private lateinit var backButton: Button
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var adapterForAPITracks: AdapterForAPITracks
    private val cleanTrackList = ArrayList<TrackData>()
    private lateinit var utilErrorBox: View
    private lateinit var adapterForHistoryTracks: AdapterForHistoryTracks
    private lateinit var searchHistoryNotification: TextView
    private lateinit var killTheHistory: Button
    var countPass = 0
    var firstPass = ""
    var showBackBtn = false
    private lateinit var rotatingImageView: ImageView
    private var currentImage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        // Extracting flag from Intent
        val isPasswordExists = intent.getBooleanExtra("isPasswordExists", false)
        showBackBtn = intent.getBooleanExtra("showBackBtn", false)
        sharedPreferences =
            getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)

        setupOneLineViews()
        backToMain()
        callAdapterForHistoryTracks()
        setupRecyclerViewAndAdapter()
        queryTextChangedListener()
        queryInputListener(isPasswordExists)
        clearButton()
        killTheHistory()
        fillTrackAdapterWithOfferPlaylist()
        rotatingImageManager()
    } //конец онКриейт

    private fun checkMasterSSecret(
        password: String,
        isPasswordExists: Boolean
    ) { //проверка на режим пароль, маскировка
        val savedPassword =
            APKM(context = this).getMastersSecret(APK.KEY_SMALL_SECRET)
        if (savedPassword.isNullOrBlank()) {
            if (password == APK.DEFAULT_MIMIC_PASS) {
                goToMainActivity()
            } else {
                // ошибка парольки, запускаем поиск песен
                utilErrorBox.visibility = View.INVISIBLE
                clearTrackAdapter()
                preparingForSearch(password) { trackItems ->
                    val limitedTrackItems = trackItems.take(11) // Ограничение до 7 песен
                    adapterForAPITracks.updateList(limitedTrackItems)
                    adapterForAPITracks.setRecyclerView(trackRecyclerView)
                    trackRecyclerView.visibility = View.VISIBLE
                }
//                toastIt("${getString(R.string.search)} $password")
            }
        } else {
            if (password == savedPassword) {
                goToMainActivity()
            } else {
                // ошибка парольки, запускаем поиск песен
                utilErrorBox.visibility = View.INVISIBLE
                clearTrackAdapter()
                preparingForSearch(password){ trackItems ->
                    val limitedTrackItems = trackItems.take(11) // Ограничение до 7 песен
                    adapterForAPITracks.updateList(limitedTrackItems)
                    adapterForAPITracks.setRecyclerView(trackRecyclerView)
                    trackRecyclerView.visibility = View.VISIBLE
                }
//                toastIt("${getString(R.string.search)} $password")
            }

        }
    }

    private fun goToMainActivity() {
        val displayIntent = Intent(this, MainPageActivity::class.java)
        startActivity(displayIntent)
        finish()
    }

    private fun setupOneLineViews() {
        backgroundView = findViewById(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        backButton = findViewById(R.id.button_back_from_search_activity)
        backButton.visibility = if (showBackBtn) View.VISIBLE else View.GONE
        clearButton = findViewById(R.id.clearButton)
        searchIcon = findViewById(R.id.search_icon)
        queryInput = findViewById(R.id.search_edit_text)
        trackRecyclerView = findViewById(R.id.track_recycler_view)
        loadingIndicator = findViewById(R.id.loading_indicator)
        loadingIndicator.visibility = View.INVISIBLE
        utilErrorBox = findViewById<LinearLayout>(R.id.util_error_box)
        searchHistoryNotification = findViewById(R.id.you_were_looking_for)
        killTheHistory = findViewById(R.id.kill_the_history)
    }


    private fun fillTrackAdapterWithOfferPlaylist() {
        val randomArtist = ArtistNaming.getRandomArtist()
        queryInput.setText("")  // Очистите поле ввода
        preparingForSearch(randomArtist) { trackItems ->
            val limitedTrackItems = trackItems.take(7) // Ограничение до 7 песен
            adapterForAPITracks.updateList(limitedTrackItems)
            adapterForAPITracks.setRecyclerView(trackRecyclerView)
            trackRecyclerView.visibility = View.VISIBLE
            searchHistoryNotification.visibility = View.VISIBLE
            searchHistoryNotification.text = getString(R.string.you_can_like)
            killTheHistory.visibility = View.GONE
        }
    }

    private fun callAdapterForHistoryTracks() {
        adapterForHistoryTracks = AdapterForHistoryTracks(this, object : OnTrackItemClickListener {
            override fun onTrackItemClick(
                trackName: String, artistName: String, trackTimeMillis: Long, artworkUrl100: String
            ) {
                // повторный клик на треке в истории треков
                adapterForHistoryTracks.saveTrack(
                    trackName,
                    artistName,
                    trackTimeMillis,
                    artworkUrl100
                )
            }
        })
        adapterForHistoryTracks.setRecyclerView(trackRecyclerView)
    }

    private fun setupRecyclerViewAndAdapter() {
        val layoutManager = LinearLayoutManager(this)
        adapterForAPITracks =
            AdapterForAPITracks(this, cleanTrackList, object : OnTrackItemClickListener {
                override fun onTrackItemClick(
                    trackName: String,
                    artistName: String,
                    trackTimeMillis: Long,
                    artworkUrl100: String
                ) {
                    adapterForHistoryTracks.saveTrack(
                        trackName, artistName, trackTimeMillis, artworkUrl100
                    )
                    toast("${getString(R.string.added)} ${trackName}")
                    Log.d(
                        "=== SearchActivity",
                        "=== historyAdapter.saveTrack:${trackName}${artistName}"
                    )
                }
            })
        trackRecyclerView.layoutManager = layoutManager
        trackRecyclerView.adapter = adapterForAPITracks
    }

    private fun queryTextChangedListener() {
        queryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val searchText = queryInput.text.toString().trim()
                clearButton.visibility =
                    if (searchText.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                if (hasFocus && searchText.isEmpty()) {
                    // Пользователь удалил весь текст из поля ввода
//                    showHistoryViewsAndFillTrackAdapter()
                } else {
                    hideHistoryViewsAndClearTrackAdapter()
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        // ФОКУС И ВВОД
        queryInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && queryInput.text.isEmpty()) {
                // Пользователь вернулся к пустому полю ввода
//                showHistoryViewsAndFillTrackAdapter()
            } else if (queryInput.text.isNotEmpty()) {
                hideHistoryViewsAndClearTrackAdapter()
            }
        }
    }

    private fun showHistoryViewsAndFillTrackAdapter() {
        utilErrorBox.visibility = View.INVISIBLE
        backgroundView.visibility = View.INVISIBLE
        fillTrackAdapter()
        trackRecyclerView.visibility = View.VISIBLE
        searchHistoryNotification.visibility = View.VISIBLE
        searchHistoryNotification.text = getString(R.string.you_were_looking_for)
        killTheHistory.visibility = View.VISIBLE
    }

    private fun fillTrackAdapter() {
        clearTrackAdapter()
        adapterForHistoryTracks?.setRecyclerView(trackRecyclerView)
        adapterForHistoryTracks?.syncTracks()
//        trackRecyclerView.scrollToPosition(0)
    }

    private fun hideHistoryViewsAndClearTrackAdapter() {
        clearTrackAdapter()
        trackRecyclerView.visibility = View.INVISIBLE
        searchHistoryNotification.visibility = View.INVISIBLE
        killTheHistory.visibility = View.INVISIBLE

    }

    private fun clearTrackAdapter() {
        adapterForHistoryTracks.clearHistoryList() // чистит адаптер с историей
    }

    private fun killTheHistory() {
        killTheHistory.setOnClickListener {
            adapterForHistoryTracks.killHistoryList()
            hideHistoryViewsAndClearTrackAdapter()
        }
    }

    private fun queryInputListener(isPasswordExists: Boolean) {  // ВВОД пользователя
        queryInput.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val searchText = queryInput.text.toString().trim()
                if (searchText.isNotEmpty()) {
                    checkMasterSSecret(searchText, isPasswordExists)
                }
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(queryInput.windowToken, 0)
    }

    private fun preparingForSearch(searchText: String, callback: (List<TrackData>) -> Unit) {
        loadingIndicator.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            performSearch(searchText) { trackItems ->
                loadingIndicator.visibility = View.INVISIBLE
                callback(trackItems)
            }
        }, APK.SERVER_PROCESSING_MILLISECONDS)
    }

    private var lastQuery: String? = null
    private var lastCallback: ((List<TrackData>) -> Unit)? = null

    private fun performSearch(query: String, callback: (List<TrackData>) -> Unit) {
        lastQuery = query        // Сохраняем последний запрос и колбэк
        lastCallback = callback
        Log.d(
            "=== SearchActivity",
            "=== Запускаем метод performSearch с параметрами Query: $query и Callback"
        )
        iTunesSearchAPI.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(
                call: Call<TrackResponse>, response: Response<TrackResponse>
            ) {
                if (response.code() == 200) {
                    val trackResponse = response.body()
                    val trackData = if (trackResponse?.results?.isNotEmpty() == true) {
                        // Преобразуем результаты в список объектов TrackData
                        trackResponse.results.map { track ->
                            Log.d(
                                "=== SearchActivity",
                                "=== Метод performSearch => response.isSuccessful! track.trackName:${track.trackName}"
                            )
                            TrackData(
                                track.trackName,
                                track.artistName,
                                track.trackTimeMillis,
                                track.artworkUrl100
                            )
                        }
                    } else {
                        Log.d(
                            "=== SearchActivity",
                            "=== Метод performSearch => response.isSuccessful! => emptyList() таких песен нет"
                        )
                        solvingAbsentProblem() // вызываем заглушку о пустом листе запроса
                        emptyList()
                    }
                    callback(trackData)         // Вызываем колбэк с полученными данными
                    Log.d(
                        "=== SearchActivity",
                        "=== Метод performSearch => response.isSuccessful! => callback(trackData): $trackData"
                    )
                } else {
                    val error = when (response.code()) {
                        400 -> "${getString(R.string.error400)}"
                        401 -> "${getString(R.string.error401)}"
                        403 -> "${getString(R.string.error403)}"
                        404 -> "${getString(R.string.error404)}"
                        500 -> "${getString(R.string.error500)}"
                        503 -> "${getString(R.string.error503)}"
                        else -> "${getString(R.string.error0)}"
                    }
                    Log.d("=== SearchActivity", error)
                    toast(error)
                    onFailure(
                        call, Throwable(error)
                    )
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                solvingConnectionProblem()
                val trackData = emptyList<TrackData>()
                callback(trackData)
            }
        })
    }

    private fun solvingAbsentProblem() {
        loadingIndicator.visibility = View.INVISIBLE
        val errorIcon = findViewById<ImageView>(R.id.error_icon)
        val errorTextWeb = findViewById<TextView>(R.id.error_text_web)
        errorIcon.setImageResource(R.drawable.ic_error_notfound)
        errorTextWeb.text = resources.getString(R.string.nothing_was_found)
        val retryButton = findViewById<Button>(R.id.retry_button)
        retryButton.visibility = View.INVISIBLE // тут кнопка не нужна
        utilErrorBox.visibility = View.VISIBLE
        utilErrorBox.setOnClickListener {
            utilErrorBox.visibility = View.INVISIBLE
        }
    }

    private fun solvingConnectionProblem() {
        loadingIndicator.visibility = View.INVISIBLE
        val errorIcon = findViewById<ImageView>(R.id.error_icon)
        val errorTextWeb = findViewById<TextView>(R.id.error_text_web)
        errorIcon.setImageResource(R.drawable.ic_error_internet)
        errorTextWeb.text = resources.getString(R.string.error_connect)
        val retryButton = findViewById<Button>(R.id.retry_button)
        retryButton.visibility = View.VISIBLE
        utilErrorBox.visibility = View.VISIBLE

        retryButton.setOnClickListener {
            utilErrorBox.visibility = View.GONE
            retryButton.visibility = View.GONE
            clearButton()
        }

    }
    interface iTunesApiService {
        @GET("search?entity=song")
        fun search(@Query("term") text: String): Call<TrackResponse>
    }

    private fun clearButton() {
        clearButton.setOnClickListener {
            queryInput.text.clear()
            queryInput.requestFocus()
                    searchHistoryNotification.text = getString(R.string.you_were_looking_for)

        }
    }

    private fun backToMain() {
        backButton.setOnClickListener {
            finish()
        }
    }

    fun constructYoutubeLink(title: String, artist: String, duration: String): String {
        val query = "$title $artist"
        val encodedQuery = Uri.encode(query)
        val youtubeUrl = "https://www.youtube.com/results?search_query=$encodedQuery"
        return youtubeUrl
    }

    fun openYoutubeLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(intent)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun rotatingImageManager() {
        rotatingImageView = findViewById(R.id.rotatingImageView)
        if(!isInternetPermissionGranted()) {
            rotatingImageView.setImageResource(R.drawable.ic_error_internet)
        }
            rotatingImageView.setOnClickListener { changeImage() }
            rotatingImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation))
        }

    fun changeImage() {
        if(!isInternetPermissionGranted()) {
            if (currentImage == 1) {
                toast(getString(R.string.error_serch))
                rotatingImageView.setImageResource(R.drawable.ic_error_notfound)
                currentImage = 2
            } else {
                toast(getString(R.string.error_connect))
                rotatingImageView.setImageResource(R.drawable.ic_error_internet)
                currentImage = 1
            }
        } else {
            if (currentImage == 1) {
//                toast(getString(R.string.errorserch))
                rotatingImageView.setImageResource(R.drawable.ic_launcher_foreground)
                currentImage = 2
                showHistoryViewsAndFillTrackAdapter()
            } else {
//                toast(getString(R.string.error500))
                rotatingImageView.setImageResource(R.drawable.ic_audio)
                currentImage = 1
                fillTrackAdapterWithOfferPlaylist()
            }
        }
    }
    private fun isInternetPermissionGranted(): Boolean {
        try {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
        } catch (e: Exception) {
            return false
        }
    }
}

class UtilTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name_text_view)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name_text_view)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_duration_text_view)
    private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image_view)
    private val onTrackClickButton: LinearLayout = itemView.findViewById(R.id.util_item_track)
    private val playButton: ImageView = itemView.findViewById(R.id.button_right_arrow)

    fun bind(trackData: TrackData, trackItemClickListener: OnTrackItemClickListener) {
        trackNameTextView.text = trackData.trackName
        artistNameTextView.text = trackData.artistName
        trackTimeTextView.text = formatTrackDuration(trackData.trackTimeMillis)
        loadImage(trackData.artworkUrl100, artworkImageView)
        onTrackClickButton.setOnClickListener {
            trackItemClickListener.onTrackItemClick(
                trackData.trackName,
                trackData.artistName,
                trackData.trackTimeMillis,
                trackData.artworkUrl100
            )
        }
        playButton.setOnClickListener {
            val title = trackData.trackName
            val artist = trackData.artistName
            val duration = trackData.formatTrackDuration()
            val youtubeLink =
                (itemView.context as SearchActivity).constructYoutubeLink(title, artist, duration)
            (itemView.context as SearchActivity).openYoutubeLink(youtubeLink)
            onTrackClickButton.performClick()
        }
    }

    private fun formatTrackDuration(trackTimeMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(trackTimeMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(trackTimeMillis) - TimeUnit.MINUTES.toSeconds(
            minutes
        )
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(imageView).load(imageUrl).placeholder(R.drawable.ic_placeholder)
            .transform(RoundedCorners(APK.ALBUM_ROUNDED_CORNERS))
            .error(R.drawable.ic_placeholder)
            .into(imageView)
    }
}

class AdapterForAPITracks(
    private val context: Context,
    private var trackData: List<TrackData>,
    private val trackItemClickListener: OnTrackItemClickListener
) : RecyclerView.Adapter<UtilTrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtilTrackViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.util_item_track, parent, false)
        return UtilTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: UtilTrackViewHolder, position: Int) {
        holder.bind(trackData[position], trackItemClickListener)
    }

    override fun getItemCount(): Int {
        return trackData.size
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = this
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun updateList(newList: List<TrackData>) {
        trackData = newList
        notifyDataSetChanged()
    }

    fun clearList() {
        val newList: MutableList<TrackData> = mutableListOf()
        trackData = newList
        notifyDataSetChanged()
    }
}

data class TrackData(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String
) {
    fun formatTrackDuration(): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(trackTimeMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(trackTimeMillis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}

data class TrackResponse(val results: List<ITunesTrack>)
data class ITunesTrack(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String
)

interface OnTrackItemClickListener {
    fun onTrackItemClick(
        trackName: String, artistName: String, trackTimeMillis: Long, artworkUrl100: String
    )
}

