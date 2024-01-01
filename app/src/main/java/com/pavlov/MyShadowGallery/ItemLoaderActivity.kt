package com.pavlov.MyShadowGallery

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.github.chrisbanes.photoview.PhotoView
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.FileProviderAdapter
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper
import android.widget.Adapter
import androidx.constraintlayout.widget.ConstraintLayout
import com.pavlov.MyShadowGallery.util.Encryption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ItemLoaderActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    var photoList = ArrayList<String>()
    private var isPreviewVisible = false
    private lateinit var photoListAdapter: PhotoListAdapter
    private var imageDialog: Dialog? = null
    private lateinit var encryption: Encryption
    private var outputGalleryFile: File? = null
    private lateinit var frameLayout2: FrameLayout
    private lateinit var loadingIndicator2: ProgressBar
    private lateinit var frameLayout3: FrameLayout
    private var isItFrontCamera = false
    private var imageDialogAcceptance = false
    private lateinit var buttonForCover2: Button
    private lateinit var buttonCapture: Button
    private lateinit var buttonGallery: Button
    private lateinit var buttonCameraSet: ConstraintLayout

    companion object {
        const val REQUEST_PERMISSIONS = 1
    }

    val permission1: String = Manifest.permission.READ_EXTERNAL_STORAGE
    val permission2: String = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val permission3: String = Manifest.permission.CAMERA

    @RequiresApi(Build.VERSION_CODES.R)
    val permission4: String = Manifest.permission.MANAGE_EXTERNAL_STORAGE

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val permission5: String = Manifest.permission.READ_MEDIA_IMAGES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itemloader)
        requestPermissions()
        Timber.plant(Timber.DebugTree()) // для логирования багов
        encryption = Encryption(this)
        val savedFiles = encryption.getPreviouslySavedFiles()
        for (fileUri in savedFiles) {
            val uri: Uri = Uri.parse(fileUri)
            encryption.addPhotoToList(0, uri)
        }
        photoListAdapter = PhotoListAdapter(this, encryption)
        sharedPreferences =
            getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)

        frameLayout2 = findViewById(R.id.frameLayout2)
        loadingIndicator2 = findViewById(R.id.loading_indicator2)
        buttonForCover2 = findViewById(R.id.button_for_cover2)
        frameLayout3 = findViewById(R.id.frameLayout3)
        buttonCapture = findViewById<Button>(R.id.button_capture)
        buttonGallery = findViewById<Button>(R.id.button_gallery)
        buttonGallery.setOnClickListener {
            openImagePicker()
        }
        buttonCameraSet = findViewById(R.id.button_camera_set)
        // Проверяем, был ли передан флаг о режиме Галереи
        val hideConstraintLayout = intent.getBooleanExtra("hideConstraintLayout", false)
        if (hideConstraintLayout) {
            buttonCameraSet.visibility = View.GONE
        }
    }

    private fun requestPermissions() {
        val permissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(permission1, permission2, permission3, permission4, permission5)
        } else {
            arrayOf(permission1, permission2, permission3)
        }
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSIONS) {
            var allPermissionsGranted = true

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                showToast("Доступ к камере и галерее получен")
            } else {
            }
        }

        // Получение пути к внутренней директории и сохранение для photoList
        val savedFiles = encryption.getPreviouslySavedFiles()
        photoList.addAll(savedFiles)

        val back = findViewById<Button>(R.id.button_back_from_loader) // НАЗАД
        back.setOnClickListener {
            finish()
        }

        val previewView = findViewById<PreviewView>(R.id.view_finder)
        recyclerView = findViewById(R.id.list_view_photos)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = photoListAdapter

        imageDialog = Dialog(
            this, android.R.style.Theme_Black_NoTitleBar_Fullscreen
        )  // открытие изображения в новом окне
        imageDialog!!.setContentView(R.layout.util_dialog_image_view)

        val buttonCamera = findViewById<Button>(R.id.button_camera)
        val buttonFlip = findViewById<Button>(R.id.flip)

        buttonCamera.setOnClickListener { // пользователь жмакает кнопку КАМЕРА,он хочет изменить её статус
            if (isPreviewVisible) { // Если видео-искатель включен (=это не первый запуск, так как первый с выключенным видеоискателем)
                buttonCapture.visibility = View.GONE
                buttonGallery.visibility = View.VISIBLE
                buttonFlip.visibility = View.GONE
                buttonCamera.text = getString(R.string.cam_bat_off)
                previewView.visibility = if (isPreviewVisible) View.GONE else View.VISIBLE
                isPreviewVisible = !isPreviewVisible
                return@setOnClickListener

            } else { // Если превью не видимо
                buttonCapture.visibility = View.VISIBLE
                buttonGallery.visibility = View.GONE
                buttonFlip.visibility = View.VISIBLE
                buttonCamera.text = getString(R.string.cam_bat_on)
                previewView.visibility = if (isPreviewVisible) View.GONE else View.VISIBLE
                isPreviewVisible = !isPreviewVisible
            }
            openCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        }
    }

    private fun openCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val viewFinder = findViewById<PreviewView>(R.id.view_finder)
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            var currentCameraSelector = cameraSelector

            try {
                cameraProvider.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

                cameraProvider.bindToLifecycle(
                    this, currentCameraSelector, preview, imageCapture
                )

                buttonCapture.visibility = View.VISIBLE
                var outputFile: File
                var fileName = ""

                val buttonFlip = findViewById<Button>(R.id.flip)
                buttonFlip.setOnClickListener {
                    currentCameraSelector =
                        if (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }
                    isItFrontCamera = currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this, currentCameraSelector, preview, imageCapture
                    )
                }

                buttonCapture.setOnClickListener {
                    showLoadingIndicator()
                    val folder = applicationContext.filesDir
                    var existOrNot: Boolean = sharedPreferences.getBoolean(
                        AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
                        false
                    )
                    fileName = FileProviderAdapter.generateFileName(application, existOrNot, folder)
                    outputFile = File(folder, fileName)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(this),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                var rotationAngle = 0 // угол, определяющий градус вращения пикчи
                                // диалоговое окно после клика (развернуть или удалить пикчу)
                                imageDialog = Dialog(this@ItemLoaderActivity)
                                imageDialog?.setContentView(R.layout.util_dialog_image_view)
                                val imageViewDialog =
                                    imageDialog?.findViewById<PhotoView>(R.id.image_view_dialog)

                                // Проверка наличия imageViewDialog и outputFile
                                if (imageViewDialog != null && outputFile.exists()) {
                                    // Загрузка изображения в PhotoView с использованием Glide
                                    Glide.with(this@ItemLoaderActivity)
                                        .load(outputFile)
                                        .into(imageViewDialog)
                                }
                                val buttonsForImageDialog =
                                    imageDialog?.findViewById<ConstraintLayout>(R.id.buttons_for_image_dialog)
                                val btnShare =
                                    imageDialog?.findViewById<Button>(R.id.image_dialog_btn_share)
                                val imageDialogFileName =
                                    imageDialog?.findViewById<TextView>(R.id.imageDialogFileName)
                                imageDialogFileName?.text = "${fileName}"
                                val imageDialogAcceptanceButton =
                                    imageDialog?.findViewById<Button>(R.id.image_dialog_acceptance_button)
                                val btnTernLeft =
                                    imageDialog?.findViewById<Button>(R.id.image_dialog_tern_left)
                                val btnFAQ =
                                    imageDialog?.findViewById<Button>(R.id.image_dialog_faq)
                                val btnTernRight =
                                    imageDialog?.findViewById<Button>(R.id.image_dialog_tern_right)
                                val btnDelete =
                                    imageDialog?.findViewById<Button>(R.id.image_dialog_del_pct)
                                val buttonForCover3 =
                                    imageDialog?.findViewById<Button>(R.id.button_for_cover3)
                                buttonForCover3?.text = "Ожидайте..."
                                val loadingIndicator3 =
                                    imageDialog?.findViewById<ProgressBar>(R.id.loading_indicator3)

                                imageDialog?.show()
                                imageDialogFileName?.visibility = View.VISIBLE
                                buttonsForImageDialog?.visibility = View.VISIBLE
                                btnShare?.visibility = View.INVISIBLE
                                imageDialogAcceptanceButton?.visibility = View.VISIBLE

                                fun rotatingMethod() { // диалог с вращением
                                    if (buttonForCover3 != null) {
                                        buttonForCover3.visibility = View.VISIBLE
                                    }
                                    if (loadingIndicator3 != null) {
                                        loadingIndicator3.visibility = View.VISIBLE
                                    }
                                    MainScope().launch { // КАРУТИН

                                        val rotBitmap = if (isItFrontCamera) {
                                            FileProviderAdapter.rotateImageByKorutin(
                                                outputFile,
                                                rotationAngle
                                            )
                                        } else {
                                            FileProviderAdapter.rotateImageByKorutin(
                                                outputFile,
                                                rotationAngle
                                            )
                                        }
                                        if (imageViewDialog != null) {
                                            imageViewDialog.setImageBitmap(rotBitmap)
                                        }

                                        Handler(Looper.getMainLooper()).postDelayed(
                                            {
                                                if (buttonForCover3 != null) {
                                                    buttonForCover3.visibility = View.GONE
                                                }
                                                if (loadingIndicator3 != null) {
                                                    loadingIndicator3.visibility = View.GONE
                                                }
                                            },
                                            AppPreferencesKeys.LOAD_PROCESSING_MILLISECONDS
                                        ) // завершение индикатора
                                    }
                                }

                                btnTernLeft?.setOnClickListener { // диалог с вращением налево
                                    if (rotationAngle == 0) {
                                        if (isItFrontCamera) {
                                            rotationAngle -= 90
                                        } else {
                                            rotationAngle -= 270
                                        }
                                        rotatingMethod()
                                        rotatingMethod()
                                    }
                                    rotationAngle -= 90
                                    rotatingMethod()
                                }

                                btnTernRight?.setOnClickListener { // диалог с вращением направо
                                    if (rotationAngle == 0) {
                                        if (isItFrontCamera) {
                                            rotationAngle += 270
                                        } else {
                                            rotationAngle += 90
                                        }
                                        rotatingMethod()
                                        rotatingMethod()
                                    }
                                    rotationAngle += 90
                                    rotatingMethod()
                                }

                                btnFAQ?.setOnClickListener {
                                    // тут будет справка по меню
                                }

                                btnDelete?.setOnClickListener { // удаляем пикчу и выходим из imageDialog
                                    showToast("Удаляю изображение, ожидайте...")
                                    imageDialogAcceptance = false
                                    FileProviderAdapter.deleteFile(
                                        outputFile.name,
                                        this@ItemLoaderActivity
                                    )
                                    imageDialog?.dismiss()
                                    hideLoadingIndicator(isItFrontCamera) // завершение индикатора
                                }

                                imageDialogAcceptanceButton?.setOnClickListener {  // сохраняем пикчу и выходим из imageDialog
                                    imageDialog?.dismiss()
                                    imageDialogAcceptance = true
                                    if (sharedPreferences.getBoolean(
                                            AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
                                            false
                                        )
                                    ) {

                                        MainScope().launch {  // в фоновом потоке, Корутина
                                            showToast("Добавляю файл, ожидайте...")

                                            val rotationDegrees = when (currentCameraSelector) {
                                                CameraSelector.DEFAULT_FRONT_CAMERA -> {
                                                    if (rotationAngle == 0) {
                                                        270
                                                    } else {
                                                        rotationAngle
                                                    }
                                                }

                                                CameraSelector.DEFAULT_BACK_CAMERA -> {
                                                    if (rotationAngle == 0) {
                                                        -270
                                                    } else {
                                                        rotationAngle
                                                    }
                                                }

                                                else -> rotationAngle
                                            }

                                            val rotatedBitmap =
                                                FileProviderAdapter.rotateImageByKorutin(
                                                    outputFile,
                                                    rotationDegrees
                                                )

                                            val imageFile =
                                                FileProviderAdapter.bitmapToFileByKorutin(
                                                    rotatedBitmap,
                                                    application,
                                                    outputFile.name
                                                )

                                            // Явное освобождение ресурсов
                                            FileProviderAdapter.recycleBitmap(rotatedBitmap)

                                            val fileUri =
                                                FileProviderAdapter.getUriForFile(
                                                    application,
                                                    imageFile
                                                )
                                            encryption.createThumbnail(application, fileUri)

                                            try {
                                                encryption.encryptImage(fileUri, fileName)

                                                // Удаление временного файла
                                                val fileToDelete =
                                                    File(application.filesDir, imageFile.name)
                                                if (fileToDelete.exists()) {
                                                    fileToDelete.delete()
                                                }
                                            } catch (e: Exception) {
                                                showToast("Ошибка: Не удалось завершить шифрование")
                                            }
                                            hideLoadingIndicator(isItFrontCamera) // завершение индикатора
                                        } // завершение корутины
                                    } else {
                                        if (rotationAngle == 0) {
                                            showToast("Сохранил без шифрования")
                                            encryption.addPhotoToList(0, outputFile.toUri())
                                            notifyDSC()
                                            hideLoadingIndicator(isItFrontCamera) // завершение индикатора
                                        } else {
                                            MainScope().launch { // КАРУТИН
                                                val rotBitmap =
                                                    FileProviderAdapter.rotateImageByKorutin(
                                                        outputFile,
                                                        rotationAngle
                                                    )

                                                FileProviderAdapter.deleteFile(
                                                    outputFile.name,
                                                    this@ItemLoaderActivity
                                                )
                                                outputFile =
                                                    FileProviderAdapter.bitmapToFileByKorutin(
                                                        rotBitmap,
                                                        this@ItemLoaderActivity,
                                                        outputFile.name
                                                    )
                                                FileProviderAdapter.recycleBitmap(rotBitmap)
                                                showToast("Сохранил без шифрования")
                                                encryption.addPhotoToList(0, outputFile.toUri())
                                                notifyDSC()
                                                hideLoadingIndicator(isItFrontCamera) // завершение индикатора
                                            }
                                        }
                                    }
                                }

                            }

                            override fun onError(exception: ImageCaptureException) {
                                showToast("Ошибка сохранения изображения")
                            }
                        })

                }
            } catch (e: Exception) {
                showToast("Ошибка: Не удалось открыть камеру")
            }
        }, ContextCompat.getMainExecutor(this))

    }

    fun notifyDSC() {
        photoListAdapter.notifyDataSetChanged()
    }


    private
    val PICK_IMAGE_REQUEST = 1

    private fun openImagePicker() {
        val imagePickerIntent = Intent(Intent.ACTION_PICK)
        imagePickerIntent.type = "image/*"
        startActivityForResult(imagePickerIntent, PICK_IMAGE_REQUEST)
    }

    private fun handleSelectedImage(uri: Uri) {
        showToast("Загружаю изображение")

        val folder = applicationContext.filesDir
        var existOrNot: Boolean = sharedPreferences.getBoolean(
            AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
            false
        )
        val fileName =
            FileProviderAdapter.generateFileName(application, existOrNot, folder)

        val outputFile = File(folder, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        if (sharedPreferences.getBoolean(
                AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
                false
            )
        ) {
            encryption.createThumbnail(this@ItemLoaderActivity, outputFile.toUri())
            encryption.encryptImage(outputFile.toUri(), fileName)
        } else {
            showToast("Изображение сохранено без шифрования")
            encryption.addPhotoToList(0, outputFile.toUri())
            notifyDSC()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        handleSelectedImage(uri)
                    }
                }
            }
        }
    }

    // Метод для отображения индикатора загрузки
    fun showLoadingIndicator() {
//        frameLayout2.setEnabled(false)
        buttonForCover2.visibility = View.VISIBLE
        loadingIndicator2.visibility = View.VISIBLE
    }

    // Метод для скрытия индикатора загрузки
    fun hideLoadingIndicator(cornerLeft: Boolean) {
//        showToast("Ожидайте завершение процесса...")
        val multiplier = if (cornerLeft) 3 else 1
        Handler(Looper.getMainLooper()).postDelayed({
            buttonForCover2.visibility = View.INVISIBLE
            loadingIndicator2.visibility = View.INVISIBLE
        }, AppPreferencesKeys.LOAD_PROCESSING_MILLISECONDS * multiplier)
    }

    fun Activity.showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}

//==================================================================================================

open class PhotoListAdapter(
    private val context: Context, private val encryption: Encryption
) : RecyclerView.Adapter<PhotoListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view_photo)
        val textViewName: TextView = itemView.findViewById(R.id.text_view_name_preview)
        val dataSecTextView: TextView = itemView.findViewById(R.id.data_sec_text_view_preview)
    }

    private var imageDialog: Dialog? = null
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.util_item_photo_unit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageView = holder.imageView
        val textViewName = holder.textViewName
        val dataSecTextView = holder.dataSecTextView
        val fileName = encryption.getPhotoList()[position]

        Glide.with(context).load(File(context.applicationContext.filesDir, fileName))
            .fitCenter()
            .placeholder(android.R.drawable.ic_lock_idle_lock).transform(RoundedCorners(8))
            .into(imageView)

        val thumbnailName = fileName
        dataSecTextView.text =
            dateFormat.format(
                File(
                    context.applicationContext.filesDir,
                    thumbnailName
                ).lastModified()
            )
        textViewName.text = thumbnailName

        holder.itemView.setOnClickListener {
            val encryptedFileName = fileName
            var encryptedFile = File(context.applicationContext.filesDir, encryptedFileName)
            val decryptedFile =
                File(
                    context.applicationContext.filesDir,
                    encryptedFileName.replace(".p", ".kk")
                )

            imageDialog = Dialog(context)
            imageDialog?.setContentView(R.layout.util_dialog_image_view)

            val imageViewDialog =
                imageDialog?.findViewById(R.id.image_view_dialog) as PhotoView

            val buttonsForImageDialog =
                imageDialog?.findViewById<ConstraintLayout>(R.id.buttons_for_image_dialog)

            val buttonForCover3 = imageDialog?.findViewById<Button>(R.id.button_for_cover3)

            val imageDialogFileName =
                imageDialog?.findViewById<TextView>(R.id.imageDialogFileName)
            imageDialogFileName?.text = "${encryptedFileName}"

            val btnShare = imageDialog?.findViewById<Button>(R.id.image_dialog_btn_share)
            val imageDialogAcceptanceButton =
                imageDialog?.findViewById<Button>(R.id.image_dialog_acceptance_button)
            val btnTernLeft = imageDialog?.findViewById<Button>(R.id.image_dialog_tern_left)

            val btnFAQ = imageDialog?.findViewById<Button>(R.id.image_dialog_faq)
            btnFAQ?.setOnClickListener {
                // тут будет справка по меню
            }
            val btnTernRight = imageDialog?.findViewById<Button>(R.id.image_dialog_tern_right)
            val btnDelete = imageDialog?.findViewById<Button>(R.id.image_dialog_del_pct)

            btnShare?.visibility = View.VISIBLE
            imageDialogAcceptanceButton?.visibility = View.GONE
            btnTernLeft?.visibility = View.INVISIBLE
            btnTernRight?.visibility = View.INVISIBLE

            btnDelete?.setOnClickListener { // удаляем пикчу и выходим из imageDialog
                context.showToast("Удаляю изображение, ожидайте...")
                FileProviderAdapter.deleteFile(
                    encryptedFile.name,
                    context.applicationContext
                )
                FileProviderAdapter.deleteFile(
                    decryptedFile.name,
                    context.applicationContext
                )

                (context as Activity).finish()
                val intent = Intent(context, ItemLoaderActivity::class.java)
                context.startActivity(intent)
                imageDialog?.dismiss()
//                        hideLoadingIndicator(isItFrontCamera) // завершение индикатора
            }

            if (encryptedFileName.endsWith(".o", true) || encryptedFileName.endsWith(
                    ".jpg",
                    true
                ) ||
                encryptedFileName.endsWith(".jpeg", true) || encryptedFileName.endsWith(
                    ".png",
                    true
                ) ||
                encryptedFileName.endsWith(".gif", true) || encryptedFileName.endsWith(
                    ".bmp",
                    true
                ) ||
                encryptedFileName.endsWith(".webp", true) || encryptedFileName.endsWith(
                    ".unknown",
                    true
                )
            ) {

                val glideRequest =
                    Glide.with(context).load(Uri.fromFile(encryptedFile)).fitCenter()
                        .centerCrop()
                        .transform(RoundedCorners(8))
                glideRequest.into(imageViewDialog)

                btnShare?.setOnClickListener {
                    if (buttonForCover3 != null) {
                        buttonForCover3.visibility = View.VISIBLE
                    }

                    shareAnyOthertedImage(
                        Uri.fromFile(encryptedFile),
                        "",
                        buttonForCover3!!,
                    )
                }
                imageDialog?.show()

                imageDialogFileName?.visibility = View.VISIBLE
                buttonsForImageDialog?.visibility = View.VISIBLE
                imageViewDialog.setOnClickListener {
                    imageDialog?.dismiss()
                }
            } else if (encryptedFileName.endsWith(".p", true)) {
                try {
                    var rotatedBitmap = encryption.decryptImage(decryptedFile)

                    val glideRequest = Glide.with(context).load(rotatedBitmap).fitCenter()
                        .transform(RoundedCorners(8))
                    glideRequest.into(imageViewDialog)
                    imageViewDialog.setImageBitmap(rotatedBitmap)

                    btnShare?.setOnClickListener {
                        if (buttonForCover3 != null) {
                            buttonForCover3.visibility = View.VISIBLE
                        }

                        showShareOptionsDialog(
                            decryptedFile,
                            rotatedBitmap,
                            encryptedFileName,
                            encryptedFile,
                            buttonForCover3!!
                        )
                    }

                    buttonForCover3?.setOnClickListener {
                        Handler(Looper.getMainLooper()).postDelayed({
                            buttonForCover3.visibility = View.GONE

                        }, AppPreferencesKeys.LOAD_PROCESSING_MILLISECONDS)
                    }

                    imageDialog?.show()
                    imageDialogFileName?.visibility = View.VISIBLE
                    buttonsForImageDialog?.visibility = View.VISIBLE
                    Timber.d("=== Вывод дешифрованного изображения через Dialog")
                    imageViewDialog.setOnClickListener {
                        imageDialog?.dismiss()
                        recycleBitmap(rotatedBitmap) // явное удаление rotatedBitmap
                        val fileNameWithExtension = "${encryptedFileName}eekaboo"
//                        context.toast("Удаляю экземпляр ${fileNameWithExtension}")
                        FileProviderAdapter.deleteFile(
                            fileNameWithExtension,
                            context
                        )  // явное удаление файла, в который превратили битмапу
                    }
                } catch (e: Exception) {
                    // Отобразить сообщение об ошибке
                    context.showToast("Ошибка. Возможно ключ был изменён")
                }
            } else {
                // Отобразить сообщение об ошибке
                context.showToast("Ошибка. Возможно формат изображения не соответствует")
            }
        }
    }

    private fun shareDecryptedImage(
        imageUri: Uri, encryptedFileName: String, buttonForCover3: Button,
//        loadingIndicator3: ProgressBar
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"

        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        context.startActivity(Intent.createChooser(shareIntent, "Поделиться изображением"))

        Handler(Looper.getMainLooper()).postDelayed({
            buttonForCover3.visibility = View.GONE
//            loadingIndicator3.visibility = View.GONE
        }, AppPreferencesKeys.LOAD_PROCESSING_MILLISECONDS)
    }

    private fun shareAnyOthertedImage(
        imageUri: Uri, fileNameWithExtension: String, buttonForCover3: Button,
//        loadingIndicator3: ProgressBar
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"

        // Используйте метод из ImageUtils для получения безопасного URI файла
        val contentUri = FileProviderAdapter.getUriForFile(context, File(imageUri.path!!))

        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        context.startActivity(Intent.createChooser(shareIntent, "Поделиться изображением"))

        Handler(Looper.getMainLooper()).postDelayed({
            buttonForCover3.visibility = View.GONE
//            loadingIndicator3.visibility = View.GONE
        }, AppPreferencesKeys.LOAD_PROCESSING_MILLISECONDS)
    }

    private fun showShareOptionsDialog(
        decryptedFile: File,
        decryptedBitmap: Bitmap,
        encryptedFileName: String,
        encryptedFile: File, // тут привью
        buttonForCover3: Button,
//        loadingIndicator3: ProgressBar
    ) {
        val options =
            arrayOf("Зашифрованное изображение", "Расшифрованное изображение", "Миниатюра")
        val fileNameWithExtension = "${encryptedFileName}eekaboo"
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выберите файл для отправки")

        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    shareAnyOthertedImage(
                        Uri.fromFile(decryptedFile),
                        "",
                        buttonForCover3,
//                        loadingIndicator3
                    )
                }

                1 -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        context.showToast("Создаю экземпляр: $fileNameWithExtension")
                        context.showToast("Ожидайте...")
                        val decryptedFile =
                            FileProviderAdapter.bitmapToFileByKorutin(
                                decryptedBitmap,
                                context,
                                fileNameWithExtension
                            )
                        val decryptedUri =
                            FileProviderAdapter.getUriForFile(context, decryptedFile)
                        shareDecryptedImage(
                            decryptedUri,
                            encryptedFileName,
                            buttonForCover3,
//                        loadingIndicator3
                        )
                        recycleBitmap(decryptedBitmap)// Явное освобождение памяти из decryptedBitmap
                    }
                }

                2 -> {
                    shareAnyOthertedImage(
                        Uri.fromFile(encryptedFile),
                        "",
                        buttonForCover3,
//                        loadingIndicator3
                    )
                }

            }
        }

        builder.show()
    }

    private fun recycleBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    override fun getItemCount(): Int {
        return encryption.getPhotoList().size
    }


    private fun Context.showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}


