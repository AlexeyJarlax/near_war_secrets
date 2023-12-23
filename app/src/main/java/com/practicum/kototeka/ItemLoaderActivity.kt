package com.practicum.kototeka

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
import com.practicum.kototeka.util.AppPreferencesKeys
import com.practicum.kototeka.util.AppPreferencesKeysMethods
import com.practicum.kototeka.util.NameUtil
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ItemLoaderActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    var photoList = ArrayList<String>()
    private var isPreviewVisible = false
    private lateinit var photoListAdapter: PhotoListAdapter
    private lateinit var imageDialog: Dialog
    private lateinit var encryption: Encryption
    private var outputGalleryFile: File? = null


    companion object {
        const val REQUEST_PERMISSIONS = 1
        const val REQUEST_SELECT_PHOTO = 2
        const val REQUEST_KEY_INPUT = 3
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
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val buttonGallery = findViewById<Button>(R.id.button_gallery)
        buttonGallery.setOnClickListener {
//            openFilePicker()
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
                toast("Доступ к камере и галерее получен")
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
        imageDialog.setContentView(R.layout.util_dialog_image_view)

        val buttonCamera = findViewById<Button>(R.id.button_camera)
        val buttonCapture = findViewById<Button>(R.id.button_capture)
        val buttonGallery = findViewById<Button>(R.id.button_gallery)

        buttonCamera.setOnClickListener { // пользователь жмакает кнопку КАМЕРА,он хочет изменить её статус
            if (isPreviewVisible) { // Если видео-искатель включен (=это не первый запуск, так как первый с выключенным видеоискателем)
                buttonCapture.visibility = View.GONE
                buttonGallery.visibility = View.VISIBLE
                buttonCamera.text = getString(R.string.cam_bat_off)
                previewView.visibility = if (isPreviewVisible) View.GONE else View.VISIBLE
                isPreviewVisible = !isPreviewVisible
                return@setOnClickListener

            } else { // Если превью не видимо
                buttonCapture.visibility = View.VISIBLE
                buttonGallery.visibility = View.GONE
                buttonCamera.text = getString(R.string.cam_bat_on)
                previewView.visibility = if (isPreviewVisible) View.GONE else View.VISIBLE
                isPreviewVisible = !isPreviewVisible
            }

            val options = arrayOf<CharSequence>("Фронтальная камера", "Задняя камера")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Выбрать источник изображения")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        openCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    }

                    1 -> {
                        openCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    }

                    else -> toast("Некорректный выбор")
                }
            }
            builder.show()
        }

    }

    private fun openCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val viewFinder = findViewById<PreviewView>(R.id.view_finder)
            val imageCapture =
                ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
            try {
                cameraProvider.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                var outputFile: File
                val buttonCapture = findViewById<Button>(R.id.button_capture)
                buttonCapture.visibility = View.VISIBLE
                var fileName = ""

                buttonCapture.setOnClickListener {
//                    val folder = getExternalFilesDir(null)
//                    fileName = generateFileName()
                    val randomName = "${NameUtil.adjectives.random()}\n${NameUtil.nouns.random()}"
                    fileName = "${randomName}.unknown"
                    if (sharedPreferences.getBoolean(
                            AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
                            false
                        )
                    ) {
                        fileName = fileName.substringBeforeLast(".")
                        fileName = "${fileName}.k"
                    } else {
                        fileName = fileName.substringBeforeLast(".")
                        fileName = "${fileName}.o"
                    }
                    val folder = getExternalFilesDir(null)

                    if (folder != null) {
                        if (!folder.exists()) {
                            folder.mkdirs()// Папка не существует
                        }
                    }
                    if (folder != null) {
                        var counter = 1
                        var file = File(folder, fileName)
                        while (file.exists()) {
                            fileName = "${fileName}_$counter"
                            file = File(folder, fileName)
                            counter++
                        }

                        outputFile = File(folder, fileName)
                        val outputOptions =
                            ImageCapture.OutputFileOptions.Builder(outputFile).build()

                        imageCapture.takePicture(outputOptions,
                            ContextCompat.getMainExecutor(this),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    if (sharedPreferences.getBoolean(
                                            AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
                                            false
                                        )
                                    ) {
                                        encryption.createThumbnail(
                                            this@ItemLoaderActivity, outputFile.toUri()
                                        )
                                        encryption.encryptImage(
                                            outputFile.toUri(), fileName
                                        )
                                    } else {
                                        toast("Изображение сохранено без шифрования")
                                        encryption.addPhotoToList(0, outputFile.toUri())
                                        notifyDSC()
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    toast("Ошибка сохранения изображения")
                                }
                            })
                    } else {
                        toast("Ошибка: Не удалось получить папку для сохранения изображения")
                    }
                }
            } catch (e: Exception) {
                toast("Ошибка: Не удалось открыть камеру")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun notifyDSC() {
        photoListAdapter.notifyDataSetChanged()
    }
//    private fun generateFileName(): String {
//        val randomName = "${NameUtil.adjectives.random()}\n${NameUtil.nouns.random()}"
//        var fileName = "${randomName}.unknown"
//
//        if (sharedPreferences.getBoolean(
//                AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_KLUCHIK,
//                false
//            )
//        ) {
//            fileName = fileName.substringBeforeLast(".")
//            fileName = "${fileName}.k"
//        } else {
//            fileName = fileName.substringBeforeLast(".")
//            fileName = "${fileName}.o"
//        }
//
//        val folder = getExternalFilesDir(null)
//
//        if (folder != null) {
//            if (!folder.exists()) {
//                folder.mkdirs()
//            }
//        }
//
//        if (folder != null) {
//            var counter = 1
//            var file = File(folder, fileName)
//
//            while (file.exists()) {
//                fileName = "${fileName}_$counter"
//                file = File(folder, fileName)
//                counter++
//            }
//        } else {
//            toast("Ошибка: Не удалось получить папку для сохранения файла")
//        }
//
//        return fileName
//    }

//    private val PICK_FILE_REQUEST = 1
//    private val PICK_IMAGE_REQUEST = 2

// ... (ваш существующий код)


//    private fun openFilePicker() {
//        val filePickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        filePickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
//        filePickerIntent.type = "*/*"  // Любые типы файлов
//        val imagePickerIntent = Intent(Intent.ACTION_PICK)
//        imagePickerIntent.type = "image/*"
//        val chooserIntent = Intent.createChooser(
//            filePickerIntent,
//            "Выберите файл или изображение"
//        )
//        chooserIntent.putExtra(
//            Intent.EXTRA_INITIAL_INTENTS,
//            arrayOf(imagePickerIntent)
//        )
//        startActivityForResult(chooserIntent, PICK_FILE_REQUEST)
//    }
//
//// ... (ваш существующий код)
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            PICK_FILE_REQUEST -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    data?.data?.let { uri ->
//                        handleSelectedFile(uri)
//                    }
//                }
//            }
//            PICK_IMAGE_REQUEST -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    data?.data?.let { uri ->
//                        handleSelectedImage(uri)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun handleSelectedFile(uri: Uri) {
//        toast("Грузим файл")
//        generateFileName()
//
////        val textView = findViewById<TextView>(R.id.selected_file_path)
////        textView.text = uri.toString()
//        // Добавьте здесь свою логику обработки файла
//    }
//
//    private fun handleSelectedImage(uri: Uri) {
//        // Обрабатывайте выбранный URI изображения по мере необходимости
//        // Например, вы можете отобразить выбранное изображение в ImageView
////        val imageView = findViewById<ImageView>(R.id.selected_image_view)
////        imageView.setImageURI(uri)
//        toast("Грузим пикчу")
//        // Добавьте здесь свою логику обработки изображения
//    }
//}
}
fun Activity.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

//==================================================================================================

class PhotoListAdapter(
    private val context: Context, private val encryption: Encryption
) : RecyclerView.Adapter<PhotoListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view_photo)
        val textViewName: TextView = itemView.findViewById(R.id.text_view_name_preview)
        val dataSecTextView: TextView = itemView.findViewById(R.id.data_sec_text_view_preview)
    }

    //    private lateinit var encryption: Encryption
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

        Glide.with(context).load(File(context.getExternalFilesDir(null), fileName)).fitCenter()
            .placeholder(android.R.drawable.ic_lock_idle_lock).transform(RoundedCorners(16))
            .into(imageView)

        val thumbnailName = fileName
        dataSecTextView.text =
            dateFormat.format(File(context.getExternalFilesDir(null), thumbnailName).lastModified())
        textViewName.text = thumbnailName

        holder.itemView.setOnClickListener {

            // val decryptionKey = AppPreferencesKeys.ENCRYPTION_KEY
            val encryptedFileName = fileName
            val encryptedFile = File(context.getExternalFilesDir(null), encryptedFileName)
            Timber.d("=== Glide : encryptedFile: ${encryptedFile.name}")
            val decryptedFile =
                File(context.getExternalFilesDir(null), encryptedFileName.replace(".p", ".kk"))
            Timber.d("=== Glide : decryptedFile: ${decryptedFile.name}")

            if (encryptedFileName.endsWith(".o", true) || encryptedFileName.endsWith(
                    ".jpg", true
                ) || encryptedFileName.endsWith(".jpeg", true) || encryptedFileName.endsWith(
                    ".png", true
                ) || encryptedFileName.endsWith(".gif", true) || encryptedFileName.endsWith(
                    ".bmp", true
                ) || encryptedFileName.endsWith(
                    ".webp", true
                ) || encryptedFileName.endsWith(".unknown", true)
            ) {
                imageDialog = Dialog(context)
                imageDialog?.setContentView(R.layout.util_dialog_image_view)
                val imageViewDialog = imageDialog?.findViewById(R.id.image_view_dialog) as PhotoView
                val glideRequest = Glide.with(context).load(Uri.fromFile(encryptedFile)).fitCenter()
                    .transform(RoundedCorners(32))
                glideRequest.into(imageViewDialog)
                imageDialog?.show()
                imageViewDialog.setOnClickListener {
                    imageDialog?.dismiss()
                }
            } else if (encryptedFileName.endsWith(".p", true)) { // кодированные пикчи
                try {
                    var rotatedBitmap = encryption.decryptImage(decryptedFile)
                    imageDialog = Dialog(context)
                    imageDialog?.setContentView(R.layout.util_dialog_image_view)
                    val imageViewDialog =
                        imageDialog?.findViewById(R.id.image_view_dialog) as PhotoView
                    val glideRequest = Glide.with(context).load(rotatedBitmap).fitCenter()
                        .transform(RoundedCorners(8))
                    glideRequest.into(imageViewDialog)
                    imageViewDialog.setImageBitmap(rotatedBitmap)
                    imageDialog?.show()
                    Timber.d("=== Вывод дешифрованного изображения через Dialog")
                    imageViewDialog.setOnClickListener {
                        imageDialog?.dismiss()
                    }
                } catch (e: Exception) {
                    // Отобразить сообщение об ошибке
                    context.toast("Ошибка. Возможно ключ был изменён")
                }
            } else {
                // Отобразить сообщение об ошибке
                context.toast("Ошибка. Возможно формат изображения не соответствует")
            }
        }
    }

    override fun getItemCount(): Int {
        return encryption.getPhotoList().size
    }

    private fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}


