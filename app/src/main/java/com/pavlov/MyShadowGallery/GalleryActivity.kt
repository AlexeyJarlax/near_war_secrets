package com.pavlov.MyShadowGallery

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView


class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        recyclerView = findViewById(R.id.list_view_photos_gal)
        photoList = mutableListOf()

        val back = findViewById<Button>(R.id.button_back_from_gal) // НАЗАД
        back.setOnClickListener {
            finish()
        }

    }


}

