package com.practicum.kototeka

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import com.practicum.kototeka.KeyInputActivity.Companion.encryptionKey
import com.practicum.kototeka.util.NameUtil
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ItemLoaderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var photoList = ArrayList<String>()
    private var isPreviewVisible = false
    private lateinit var photoListAdapter: PhotoListAdapter
    private lateinit var imageDialog: Dialog

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

    var encryptionKey: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itemloader)
        encryptionKey = KeyInputActivity.encryptionKey
        requestPermissions()
        Timber.plant(Timber.DebugTree()) // для логирования багов

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
        val savedFiles = getPreviouslySavedFiles()
        photoList.addAll(savedFiles)

        val back = findViewById<Button>(R.id.button_back_from_loader) // НАЗАД
        back.setOnClickListener {
            finish()
        }

        val previewView = findViewById<PreviewView>(R.id.view_finder)
        recyclerView = findViewById(R.id.list_view_photos)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        photoListAdapter = PhotoListAdapter(
            this, photoList
        )
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
                buttonGallery.visibility =
                    View.VISIBLE
                buttonCamera.text =
                    getString(R.string.cam_bat_off)
                previewView.visibility =
                    if (isPreviewVisible) View.GONE else View.VISIBLE
                isPreviewVisible = !isPreviewVisible
                return@setOnClickListener

            } else { // Если превью не видимо
                buttonCapture.visibility =
                    View.VISIBLE
                buttonGallery.visibility = View.GONE
                buttonCamera.text =
                    getString(R.string.cam_bat_on)
                previewView.visibility =
                    if (isPreviewVisible) View.GONE else View.VISIBLE
                isPreviewVisible = !isPreviewVisible
            }

            val options = arrayOf<CharSequence>("Фронтальная камера", "Задняя камера")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Выбрать источник изображения")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        openCamera(CameraSelector.DEFAULT_FRONT_CAMERA, encryptionKey)
                    }

                    1 -> {
                        openCamera(CameraSelector.DEFAULT_BACK_CAMERA, encryptionKey)
                    }

                    else -> toast("Некорректный выбор")
                }
            }
            builder.show()
        }

        buttonGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val pickPhotoIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickPhotoIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(pickPhotoIntent, REQUEST_SELECT_PHOTO)
        } else {
            toast("Невозможно открыть галерею")
        }
    }

    private fun openCamera(cameraSelector: CameraSelector, encryptionKey: String) {
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
                    val randomName = "${NameUtil.adjectives.random()}\n${NameUtil.nouns.random()}"
                    fileName = "${randomName}.unknown"

                    if (encryptionKey.isNotEmpty()) {
                        fileName = fileName.substringBeforeLast(".")
                        fileName = "${fileName}.k"
                    } else {
                        fileName = fileName.substringBeforeLast(".")
                        fileName = "${fileName}.o"
                    }

                    val folder = getExternalFilesDir(null)
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
                                    if (encryptionKey.isNotEmpty()) {
                                        photoListAdapter.createThumbnail(
                                            this@ItemLoaderActivity,
                                            outputFile.toUri()
                                        )
                                        encryptImage(outputFile.toUri(), encryptionKey, fileName)
                                    } else {
                                        toast("Изображение сохранено без шифрования")
                                        photoList.add(
                                            0,
                                            fileName
                                        )
                                        photoListAdapter.notifyDataSetChanged()
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

    private fun encryptImage(imageUri: Uri, encryptionKey: String, fileName: String) {
        Timber.d("=== готовится к шифрованию, принимаем на вход fileName: ${fileName}")
        val inputStream = contentResolver.openInputStream(imageUri) ?: return
        var encryptedFile = File(getExternalFilesDir(null), "${fileName}k")
        if (File(getExternalFilesDir(null), "${fileName}k").exists()) {
            Timber.d("=== файл fileName существует, будет перезапись: ${fileName}k")
            val existingFile = File(getExternalFilesDir(null), "${fileName}k")
            existingFile.delete()
        }
        Timber.d("=== готовится к шифрованию: ${encryptedFile.name}")
        Timber.d("=== путь к зашифрованному файлу: ${encryptedFile.absolutePath}")
        val outputStream = FileOutputStream(encryptedFile)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        Timber.d("=== файл messageDigest: ${messageDigest}")
        val hashedKey = messageDigest.digest(encryptionKey.toByteArray())
        Timber.d("=== файл hashedKey: ${hashedKey}")
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val cipher = Cipher.getInstance("AES")
        Timber.d("=== файл cipher: ${cipher}")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            val encryptedBytes = cipher.update(buffer, 0, read)
            outputStream.write(encryptedBytes)
        }
        val encryptedBytes = cipher.doFinal()
        outputStream.write(encryptedBytes)

        if (File(getExternalFilesDir(null), fileName).exists()) {
            Timber.d("=== сейчас в директории существует файл fileName: ${fileName}")
        }
        if (File(getExternalFilesDir(null), "${fileName}k").exists()) {
            Timber.d("=== сейчас в директории существует файл {fileName}k: ${fileName}k")
        }

        val isEncryptedFileSaved = encryptedFile.exists()
        if (!isEncryptedFileSaved) {// Проверка на сохранение файла
            Timber.e("=== Ошибка сохранения зашифрованного файла")
            return
        }
        toast("Изображение зашифровано")
        Timber.d("=== Зашифрованный файл сохранен: ${encryptedFile.name}")
        Timber.d("=== Путь к зашифрованному файлу: ${encryptedFile.absolutePath}")
        //если задать имя fileName = "my_secret_photo.jpg", то файл будет сохранен в следующем виде:
        //storage/emulated/0/Android/data/[app_package_name]/files/my_secret_photo.jpg

        val originalFile = File(imageUri.path) // Удаление оригинального изображения
        val isOriginalFileDeleted = originalFile.delete()
        if (!isOriginalFileDeleted) { // Проверка на удаление оригинала
            Timber.e("=== Ошибка удаления оригинального файла")
            inputStream.close()        // Закрытие потоков
            outputStream.flush()
            outputStream.close()
        }
    }

    private fun getPreviouslySavedFiles(): List<String> { // наполнение списка для RecyclerView
        val savedFiles = mutableListOf<String>()
        val directory = this.getExternalFilesDir(null)
        if (directory != null && directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files.reversed()) {
                    if (file.extension != "kk") {
                        savedFiles.add(file.name)
                    }
                }
            }
        }
        return savedFiles
    }
}

fun Activity.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

//==================================================================================================
class PhotoListAdapter(private val context: Context, private val photoList: MutableList<String>) :
    RecyclerView.Adapter<PhotoListAdapter.ViewHolder>() {

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

        Glide.with(context)
            .load(File(context.getExternalFilesDir(null), photoList[position]))
            .fitCenter()
            .placeholder(android.R.drawable.ic_lock_idle_lock)
            .transform(RoundedCorners(16))
            .into(imageView)


        val thumbnailName = photoList[position]
        dataSecTextView.text =
            dateFormat.format(File(context.getExternalFilesDir(null), thumbnailName).lastModified())
        textViewName.text = thumbnailName

        holder.itemView.setOnClickListener {

            val decryptionKey = encryptionKey
            val encryptedFileName = photoList[position]
            val encryptedFile = File(context.getExternalFilesDir(null), encryptedFileName)
            Timber.d("=== Glide : encryptedFile: ${encryptedFile.name}")
            val decryptedFile =
                File(context.getExternalFilesDir(null), encryptedFileName.replace(".p", ".kk"))
            Timber.d("=== Glide : decryptedFile: ${decryptedFile.name}")

            if (encryptedFileName.endsWith(".o", true) ||
                encryptedFileName.endsWith(".jpg", true) ||
                encryptedFileName.endsWith(".jpeg", true) ||
                encryptedFileName.endsWith(".png", true) ||
                encryptedFileName.endsWith(".gif", true) ||
                encryptedFileName.endsWith(".bmp", true) ||
                encryptedFileName.endsWith(".webp", true) ||
                encryptedFileName.endsWith(".unknown", true)
            ) {
                imageDialog = Dialog(context)
                imageDialog?.setContentView(R.layout.util_dialog_image_view)
                val imageViewDialog = imageDialog?.findViewById(R.id.image_view_dialog) as PhotoView
                val glideRequest = Glide.with(context)
                    .load(Uri.fromFile(encryptedFile))
                    .fitCenter()
                    .transform(RoundedCorners(32))
                glideRequest.into(imageViewDialog)
                imageDialog?.show()
                imageViewDialog.setOnClickListener {
                    imageDialog?.dismiss()
                }
            } else if (encryptedFileName.endsWith(".p", true)) { // кодированные пикчи
                try {
                    decryptImage(decryptedFile, decryptionKey)
                } catch (e: Exception) {
                    context.toast("Ошибка. Возможно ключ был изменён")
                }
            } else {
                context.toast("Ошибка. Возможно формат изображения не соответствует")
            }
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    private fun decryptImage(file: File, decryptionKey: String) {
        Timber.d("=== Начало декодирования. файл file: ${file.name}")
        val encryptedBytes = file.readBytes()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        Timber.d("=== файл messageDigest: ${messageDigest}")
        val hashedKey = messageDigest.digest(decryptionKey.toByteArray())
        Timber.d("=== файл hashedKey: $hashedKey")
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val cipher = Cipher.getInstance("AES")
        Timber.d("=== файл cipher: ${cipher}")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        val decryptedBitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
        val matrix = Matrix()
        matrix.postRotate(90f)
        val rotatedBitmap = Bitmap.createBitmap(
            decryptedBitmap,
            0,
            0,
            decryptedBitmap.width,
            decryptedBitmap.height,
            matrix,
            true
        )
        Timber.d("=== Успешный конец декодирования. файл rotatedBitmap: ${rotatedBitmap}")
        context.toast("Дешифрование ${file.name} выполнено успешно")
        imageDialog = Dialog(context)
        imageDialog?.setContentView(R.layout.util_dialog_image_view)
        val imageViewDialog = imageDialog?.findViewById(R.id.image_view_dialog) as PhotoView
        val glideRequest = Glide.with(context)
            .load(rotatedBitmap)
            .fitCenter()
            .transform(RoundedCorners(8))
        glideRequest.into(imageViewDialog)
        imageViewDialog.setImageBitmap(rotatedBitmap)
        imageDialog?.show()
        Timber.d("=== Вывод дешифрованного изображения через Dialog")
        imageViewDialog.setOnClickListener {
            imageDialog?.dismiss()
        }
    }

    fun createThumbnail(context: Context, imageUri: Uri) {
        val requestOptions = RequestOptions().override(100, 100)
        Glide.with(context)
            .asBitmap()
            .load(imageUri)
            .apply(requestOptions)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val thumbnailName = saveThumbnailWithRandomFileName(context, resource, imageUri)
                    if (thumbnailName.isNotEmpty()) {
                        photoList.add(0, thumbnailName)
                        notifyDataSetChanged()
                        context.toast("Превью сохранено")
                        deleteOriginalImage(imageUri)
                    } else {
                        context.toast("Ошибка: Не удалось сохранить превью")
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun saveThumbnailWithRandomFileName(
        context: Context,
        thumbnail: Bitmap,
        imageUri: Uri
    ): String {
        val fileName = File(imageUri.path).name

        val fileExtension = fileName.substringAfterLast(".")
        val previewFileName = if (fileExtension.isNotEmpty()) {
            val fileNameWithoutExtension = fileName.substringBeforeLast(".")
            "${fileNameWithoutExtension}.p"
        } else {
            "Пустая превьюшка"
        }
        val file = File(context.getExternalFilesDir(null), previewFileName)
        try {
            val outputStream = FileOutputStream(file)
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return previewFileName
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun deleteOriginalImage(imageUri: Uri) {
        val file = File(imageUri.path)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}





