package com.practicum.kototeka

// Памятка о содержании файла:
//SearchActivity - активити и вся обработка поискового запроса юзера.
//UtilTrackViewHolder - холдер для RecyclerView, отображающий информацию о треках.
//UtilTrackAdapter - адаптер для RecyclerView, отображающий информацию о треках.
//iTunesApiService - интерфейс для iTunes Search API.
//TrackResponse - класс данных, представляющий ответ от iTunes Search API.
//ITunesTrack - класс данных для преобразования ответа iTunes Search API в список объектов TrackData.
//TrackData - класс данных, представляющий список треков на устройстве.

// Этапы поиска:
//1. этап: считываем ввод в queryInput.setOnEditorActionListener и queryInput.addTextChangedListener ===> запуск 2 этапа
//2. этап: передаем searchText в fun preparingForSearch для активации loadingIndicator и блокировки кнопок ===> запуск 3 этапа
//3. этап: передаем searchText в fun performSearch => вызываем TrackResponse => заполняем TrackData  ===> вывод списка песен, соответствующих запросу
//3.1 : performSearch => [возникла ошибка с вызовом TrackResponse] => Запускаем метод solvingConnectionProblem() ===> Запускаем повторно 2 этап

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.kototeka.util.ThemeManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchActivity : AppCompatActivity() {

    private val iTunesSearch = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesSearch)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val iTunesSearchAPI = retrofit.create(iTunesApiService::class.java)
    private lateinit var queryInput: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var trackAdapter: UtilTrackAdapter
    private val originalTracks = ArrayList<TrackData>()
    private lateinit var utilErrorBox: View


    companion object {
        private const val PREF_SEARCH_HISTORY = "SearchHistory"
        private const val PREF_KEY_SEARCH_HISTORY = "search_history"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree()) // для логирования ошибок
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        var backgroundView = findViewById<ImageView>(R.id.background_image)
        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
        setupViews()
        backToMain()
        setupTrackRecyclerViewAndTrackAdapter()
    }

    private fun setupViews() {
        clearButton = findViewById(R.id.clearButton)
        queryInput = findViewById(R.id.search_edit_text)
        loadingIndicator = findViewById(R.id.loading_indicator)
        loadingIndicator.visibility = View.GONE
        clearButton.setOnClickListener {
            queryInput.text.clear()
        }

        queryInput.setOnEditorActionListener { textView, actionId, keyEvent -> // заполнение с виртуальной клавиатуры
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val searchText = queryInput.text.toString().trim()
                if (searchText.isNotEmpty()) {
                    utilErrorBox = findViewById<LinearLayout>(R.id.util_error_box)
                    utilErrorBox.visibility = View.GONE // исчезновение сообщения с ошибкой
                    preparingForSearch(searchText)
                    showToast("Поиск: $searchText")
                }
                hideKeyboard()
                true
            } else {
                false
            }
        }

        queryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                clearButton.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
                if (searchText.isEmpty()) {
                    trackAdapter.updateList(originalTracks)
                    trackRecyclerView.scrollToPosition(0)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun backToMain() {
        val backButton = findViewById<Button>(R.id.button_back_from_search_activity)
        backButton.setOnClickListener {
            finish()
        }
    }


    private fun setupTrackRecyclerViewAndTrackAdapter() {
        trackRecyclerView = findViewById(R.id.track_recycler_view)
        val layoutManager = LinearLayoutManager(this)
        trackAdapter = UtilTrackAdapter(this, originalTracks)
        trackRecyclerView.layoutManager = layoutManager
        trackRecyclerView.adapter = trackAdapter
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(queryInput.windowToken, 0)
    }

    private fun clearSearchFieldAndHideKeyboard() {
        queryInput.text.clear()
        hideKeyboard()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun preparingForSearch(searchText: String) {
        loadingIndicator.visibility = View.VISIBLE
        clearButton.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            performSearch(searchText) { trackItems ->
                loadingIndicator.visibility = View.GONE
                clearButton.isEnabled = true
                trackAdapter.updateList(trackItems)
            }
        }, 1500)
    }

    private var lastQuery: String? = null
    private var lastCallback: ((List<TrackData>) -> Unit)? = null
    private fun performSearch(query: String, callback: (List<TrackData>) -> Unit) {
        // Сохраняем последний запрос и колбэк
        lastQuery = query
        lastCallback = callback
        Timber.d("Запускаем метод performSearch с параметрами Query: $query и Callback")

        // Выполняем поиск с использованием iTunesSearchAPI
        iTunesSearchAPI.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(
                call: Call<TrackResponse>,
                response: Response<TrackResponse>
            ) {
                if (response.code() == 200) {
                    val trackResponse = response.body()
                    val trackData = if (trackResponse?.results?.isNotEmpty() == true) {
                        // Преобразуем результаты в список объектов TrackData
                        trackResponse.results.map { track ->
                            Timber.d("Метод performSearch => response.isSuccessful! track.trackName:${track.trackName}")
                            TrackData(
                                track.trackName,
                                track.artistName,
                                track.trackTimeMillis,
                                track.artworkUrl100
                            )
                        }
                    } else {
                        Timber.d("Метод performSearch => response.isSuccessful! => emptyList() таких песен нет")
                        solvingAbsentProblem() // вызываем заглушку о пустом листе запроса
                        emptyList()
                    }
                    // Вызываем колбэк с полученными данными
                    callback(trackData)
                    Timber.d("Метод performSearch => response.isSuccessful! => callback(trackData): $trackData")
                } else {
                    val error = when (response.code()) {
                        400 -> "400 (Bad Request) - ошибка запроса"
                        401 -> "401 (Unauthorized) - неавторизованный запрос"
                        403 -> "403 (Forbidden) - запрещенный запрос"
                        404 -> "404 (Not Found) - не найдено"
                        500 -> "500 (Internal Server Error) - внутренняя ошибка сервера"
                        503 -> "503 (Service Unavailable) - сервис временно недоступен"
                        else -> "(unspecified error) - неустановленная ошибка"
                    }
                    Timber.d(error)
                    showToast(error)
                    onFailure(call, Throwable(error)) // Вызываем onFailure с информацией об ошибке
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
        loadingIndicator.visibility = View.GONE
        utilErrorBox = findViewById<LinearLayout>(R.id.util_error_box)
        val errorIcon = findViewById<ImageView>(R.id.error_icon)
        val errorTextWeb = findViewById<TextView>(R.id.error_text_web)
        errorIcon.setImageResource(R.drawable.ic_error_notfound)
        errorTextWeb.text = resources.getString(R.string.nothing_was_found)
        val retryButton = findViewById<Button>(R.id.retry_button)
        retryButton.visibility = View.GONE // тут кнопка не нужна
        utilErrorBox.visibility = View.VISIBLE
        utilErrorBox.setOnClickListener {
            utilErrorBox.visibility = View.GONE
        }
    }

    private fun solvingConnectionProblem() {
        loadingIndicator.visibility = View.GONE
        utilErrorBox = findViewById<LinearLayout>(R.id.util_error_box)
        val errorIcon = findViewById<ImageView>(R.id.error_icon)
        val errorTextWeb = findViewById<TextView>(R.id.error_text_web)
        errorIcon.setImageResource(R.drawable.ic_error_internet)
        errorTextWeb.text = resources.getString(R.string.error_text_web)
        val retryButton = findViewById<Button>(R.id.retry_button)
        retryButton.visibility = View.VISIBLE
        utilErrorBox.visibility = View.VISIBLE

        retryButton.setOnClickListener {
            lastQuery?.let { query ->
                lastCallback?.let { callback ->
                    preparingForSearch(query)
                }
            }
            utilErrorBox.visibility = View.GONE
        }
    }


    interface iTunesApiService {
        @GET("search?entity=song")
        fun search(@Query("term") text: String): Call<TrackResponse>
    }

    override fun onStop() {
        super.onStop()
        val preferences: SharedPreferences =
            getSharedPreferences(PREF_SEARCH_HISTORY, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove(PREF_KEY_SEARCH_HISTORY)
        editor.apply()
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
}

class UtilTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name_text_view)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name_text_view)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_duration_text_view)
    private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image_view)
    private val playButton: ImageView = itemView.findViewById(R.id.button_right_arrow)
    companion object {
        private const val ALBUM_ROUNDED_CORNERS = 8
    }

    fun bind(trackData: TrackData) {
        trackNameTextView.text = trackData.trackName
        artistNameTextView.text = trackData.artistName
        trackTimeTextView.text = formatTrackDuration(trackData.trackTimeMillis)
        loadImage(trackData.artworkUrl100, artworkImageView)
        playButton.setOnClickListener {
            val title = trackData.trackName
            val artist = trackData.artistName
            val duration = trackData.formatTrackDuration()
            val youtubeLink = (itemView.context as SearchActivity).constructYoutubeLink(title, artist, duration)
            (itemView.context as SearchActivity).openYoutubeLink(youtubeLink)
        }
    }

    private fun formatTrackDuration(trackTimeMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(trackTimeMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(trackTimeMillis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(imageView)
            .load(imageUrl)
            .placeholder(R.drawable.ic_audio)
            .transform(RoundedCorners(ALBUM_ROUNDED_CORNERS))
            .error(R.drawable.ic_error_internet)
            .into(imageView)
    }
}

class UtilTrackAdapter(
    private val context: Context,
    private var trackData: List<TrackData>,

    ) : RecyclerView.Adapter<UtilTrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtilTrackViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.util_item_track, parent, false)
        return UtilTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: UtilTrackViewHolder, position: Int) {
        holder.bind(trackData[position])
    }

    override fun getItemCount(): Int {
        return trackData.size
    }

    fun updateList(newList: List<TrackData>) {
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