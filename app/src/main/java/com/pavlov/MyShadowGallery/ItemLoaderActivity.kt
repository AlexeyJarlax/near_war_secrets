package com.pavlov.MyShadowGallery

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
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
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.file.FileProviderAdapter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getString
import com.pavlov.MyShadowGallery.util.Encryption
import com.pavlov.MyShadowGallery.file.NamingStyleManager
import com.pavlov.MyShadowGallery.util.APKM
import com.pavlov.MyShadowGallery.util.hideLoadingIndicator
import com.pavlov.MyShadowGallery.util.showLoadingIndicator
import com.pavlov.MyShadowGallery.util.showManualKeyInputDialog
import com.pavlov.MyShadowGallery.util.showToast
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
    private lateinit var constraintLayout3: ConstraintLayout
    private var isItFrontCamera = false
    private var imageDialogAcceptance = false
    private lateinit var buttonForCover2: Button

    private lateinit var buttonCapture: Button
    private lateinit var buttonGallery: Button
    private lateinit var buttonCameraSet: ConstraintLayout

    companion object {
        const val REQUEST_PERMISSIONS = 1
    }

    private val permission1: String = Manifest.permission.READ_EXTERNAL_STORAGE
    private val permission2: String = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val permission3: String = Manifest.permission.CAMERA

//    @RequiresApi(Build.VERSION_CODES.R)
//    val permission4: String = Manifest.permission.MANAGE_EXTERNAL_STORAGE

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val permission5: String = Manifest.permission.READ_MEDIA_IMAGES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itemloader)
        requestPermissions()
        encryption = Encryption(this)
        val savedFiles = encryption.getPreviouslySavedFiles()
        for (fileUri in savedFiles) {
            val uri: Uri = Uri.parse(fileUri)
            encryption.addPhotoToList(0, uri)
        }
        photoListAdapter = PhotoListAdapter(this, this, encryption)
        sharedPreferences =
            getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)

        frameLayout2 = findViewById(R.id.frameLayout2)
        loadingIndicator2 = findViewById(R.id.loading_indicator2)
        buttonForCover2 = findViewById(R.id.button_for_cover2)
        buttonForCover2.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                buttonForCover2.visibility = View.GONE
                loadingIndicator2.visibility = View.GONE
            }, 2000)
        }
        constraintLayout3 = findViewById(R.id.constraintLayout3)
        constraintLayout3.setOnClickListener {
            constraintLayout3.visibility = View.GONE
        }
        constraintLayout3.performClick()
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
        val permissions: Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            arrayOf(permission1, permission2, permission3, permission4, permission5)
                arrayOf(permission1, permission2, permission3, permission5)
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
                showToast(getString(R.string.access_obtained))
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
//        recyclerView.addItemDecoration(ExclamationMarkItemDecoration())

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
            val imageCapture =
                ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
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
                    var existOrNot =
                        APKM(context = this).getBooleanFromSPK(APK.KEY_USE_THE_ENCRYPTION_K)
                    fileName = NamingStyleManager(application).generateFileName(existOrNot, folder)
                    outputFile = File(folder, fileName)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                    imageCapture.takePicture(outputOptions,
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
                                    Glide.with(this@ItemLoaderActivity).load(outputFile)
                                        .into(imageViewDialog)
                                }

                                val lastModified = outputFile.lastModified()
                                val dateFormat =
                                    SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                                val formattedDate = dateFormat.format(Date(lastModified))

                                val btnShare =
                                    imageDialog?.findViewById<Button>(R.id.image_dialog_btn_share)
                                val imageDialogFileName =
                                    imageDialog?.findViewById<TextView>(R.id.imageDialogFileName)
                                imageDialogFileName?.text = "${fileName}"
                                val imageDialogFileDate =
                                    imageDialog?.findViewById<TextView>(R.id.imageDialogFileDate)
                                imageDialogFileDate?.text = "${formattedDate}"
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
                                val miniButtonForCover =
                                    imageDialog?.findViewById<Button>(R.id.mini_button_for_cover)
                                val buttonForCover3 =
                                    imageDialog?.findViewById<Button>(R.id.button_for_cover3)
                                buttonForCover3?.text = getString(R.string.wait)
                                val loadingIndicator3 =
                                    imageDialog?.findViewById<ProgressBar>(R.id.loading_indicator3)

                                imageDialog?.show()
                                imageDialogFileName?.visibility = View.VISIBLE
                                imageDialogFileDate?.visibility = View.VISIBLE
                                btnShare?.visibility = View.INVISIBLE
                                btnFAQ?.visibility = View.VISIBLE
                                imageDialogAcceptanceButton?.visibility = View.VISIBLE
                                btnTernLeft?.visibility = View.VISIBLE
                                btnTernRight?.visibility = View.VISIBLE
                                btnDelete?.visibility = View.VISIBLE
                                loadingIndicator3?.visibility = View.INVISIBLE ?: View.GONE

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
                                                outputFile, rotationAngle
                                            )
                                        } else {
                                            FileProviderAdapter.rotateImageByKorutin(
                                                outputFile, rotationAngle
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
                                            }, APK.LOAD_PROCESSING_MILLISECONDS
                                        ) // завершение индикатора
                                    }
                                }
                                miniButtonForCover?.setOnClickListener {
                                    btnDelete?.performClick()
                                }

                                loadingIndicator3?.setOnClickListener {
                                    btnDelete?.performClick()
                                }

                                imageViewDialog?.setOnClickListener {
                                    btnDelete?.performClick()
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
                                    val faqIntent =
                                        Intent(this@ItemLoaderActivity, FAQActivity::class.java)
                                    startActivity(faqIntent)
                                }

                                btnDelete?.setOnClickListener { // удаляем пикчу и выходим из imageDialog
                                    showToast(getString(R.string.wait))
                                    imageDialogAcceptance = false
                                    FileProviderAdapter.deleteFile(
                                        outputFile.name, this@ItemLoaderActivity
                                    )
                                    imageDialog?.dismiss()
                                    hideLoadingIndicator(isItFrontCamera) // завершение индикатора
                                    buttonForCover2.performClick()
                                }

                                imageDialogAcceptanceButton?.setOnClickListener {  // сохраняем пикчу и выходим из imageDialog
                                    imageDialog?.dismiss()
                                    imageDialogAcceptance = true
                                    if (APKM(context = this@ItemLoaderActivity).getBooleanFromSPK(
                                            APK.KEY_USE_THE_ENCRYPTION_K
                                        )
                                    ) {

                                        MainScope().launch {  // в фоновом потоке, Корутина
                                            showToast(getString(R.string.wait))

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
                                                    outputFile, rotationDegrees
                                                )

                                            val imageFile =
                                                FileProviderAdapter.bitmapToFileByKorutin(
                                                    rotatedBitmap, application, outputFile.name
                                                )

                                            // Явное освобождение ресурсов
                                            FileProviderAdapter.recycleBitmap(rotatedBitmap)

                                            val fileUri = FileProviderAdapter.getUriForFile(
                                                application, imageFile
                                            )
                                            encryption.createThumbnail(application, fileUri)

                                            try {
                                                encryption.encryptImage(
                                                    fileUri,
                                                    fileName,
                                                    APKM(context = this@ItemLoaderActivity).getDefauldKey()
                                                )

                                                // Удаление временного файла
                                                val fileToDelete =
                                                    File(application.filesDir, imageFile.name)
                                                if (fileToDelete.exists()) {
                                                    fileToDelete.delete()
                                                }
                                            } catch (e: Exception) {
                                                showToast(getString(R.string.encryption_error))
                                            }
                                            hideLoadingIndicator(isItFrontCamera) // завершение индикатора

                                        } // завершение корутины
                                    } else {
                                        if (rotationAngle == 0) {
                                            showToast(getString(R.string.enception_no))
                                            encryption.addPhotoToList(0, outputFile.toUri())
                                            notifyDSC()
                                            hideLoadingIndicator(isItFrontCamera) // завершение индикатора
                                        } else {
                                            MainScope().launch { // КАРУТИН
                                                val rotBitmap =
                                                    FileProviderAdapter.rotateImageByKorutin(
                                                        outputFile, rotationAngle
                                                    )

                                                FileProviderAdapter.deleteFile(
                                                    outputFile.name, this@ItemLoaderActivity
                                                )
                                                outputFile =
                                                    FileProviderAdapter.bitmapToFileByKorutin(
                                                        rotBitmap,
                                                        this@ItemLoaderActivity,
                                                        outputFile.name
                                                    )
                                                FileProviderAdapter.recycleBitmap(rotBitmap)
                                                showToast(getString(R.string.enception_no))
                                                encryption.addPhotoToList(0, outputFile.toUri())
                                                notifyDSC()
                                                hideLoadingIndicator(isItFrontCamera) // завершение индикатора
                                            }
                                        }
                                    }
                                    buttonForCover2.performClick()

                                }

                            }

                            override fun onError(exception: ImageCaptureException) {
                                showToast(getString(R.string.save_error))
                            }
                        })

                }
            } catch (e: Exception) {
                showToast(getString(R.string.camera_error))
            }
        }, ContextCompat.getMainExecutor(this))

    }

    fun notifyDSC() {
        photoListAdapter.notifyDataSetChanged()
    }


    private val PICK_IMAGE_REQUEST = 1

    private fun openImagePicker() {
        val imagePickerIntent = Intent(Intent.ACTION_PICK)
        imagePickerIntent.type = "image/*"
        startActivityForResult(imagePickerIntent, PICK_IMAGE_REQUEST)
    }

    private fun handleSelectedImage(uri: Uri) {
        showToast(getString(R.string.download))

        val folder = applicationContext.filesDir
        var existOrNot = APKM(context = this).getBooleanFromSPK(APK.KEY_USE_THE_ENCRYPTION_K)
        val fileName = NamingStyleManager(application).generateFileName(existOrNot, folder)

        val outputFile = File(folder, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        if (APKM(context = this).getBooleanFromSPK(APK.KEY_USE_THE_ENCRYPTION_K)) {
            encryption.createThumbnail(this@ItemLoaderActivity, outputFile.toUri())
            encryption.encryptImage(
                outputFile.toUri(),
                fileName,
                APKM(context = this@ItemLoaderActivity).getDefauldKey()
            )
        } else {
            showToast(getString(R.string.enception_no))
            encryption.addPhotoToList(0, outputFile.toUri())
            notifyDSC()
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
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
}

//==================================================================================================

open class PhotoListAdapter(
    private val activity: Activity,
    private val context: Context,
    private val encryption: Encryption
) : RecyclerView.Adapter<PhotoListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view_photo)
        val textViewName: TextView = itemView.findViewById(R.id.text_view_name_preview)
        val dataSecTextView: TextView = itemView.findViewById(R.id.data_sec_text_view_preview)
        val linearUnit: LinearLayout = itemView.findViewById(R.id.linear_unit)
//        val alarm: TextView = itemView.findViewById(R.id.alarm)
    }

    private var imageDialog: Dialog? = null
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())
    private lateinit var rotatedBitmap: Bitmap

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.util_item_photo_unit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val textViewName = holder.textViewName
        val imageView = holder.imageView
        val dataSecTextView = holder.dataSecTextView
        val linearUnit1 = holder.linearUnit

        val fileName = encryption.getPhotoList()[position]

        Glide.with(context).load(File(context.applicationContext.filesDir, fileName)).fitCenter()
            .placeholder(android.R.drawable.ic_lock_idle_lock).transform(RoundedCorners(8))
            .into(imageView)

        val thumbnailName = fileName
        dataSecTextView.text = dateFormat.format(
            File(
                context.applicationContext.filesDir, thumbnailName
            ).lastModified()
        )
        textViewName.text = thumbnailName

        if (fileName.endsWith(".share")) {
//            imageView.setBackgroundColor(Color.argb(1, 222, 222, 222)) // устанавливаем альфа = 1 (полная видимость)
        } else {
            linearUnit1.setBackgroundColor(
                Color.argb(
                    0,
                    111,
                    111,
                    111
                )
            ) // устанавливаем альфа = 0 (полная прозрачность)
        }

        holder.itemView.setOnClickListener {
            activity.showLoadingIndicator()
            val encryptedFileName = fileName
            var encryptedFile = File(context.applicationContext.filesDir, encryptedFileName)
//            val decryptedFile = File(
//                context.applicationContext.filesDir, encryptedFileName.replace(".p", ".kk")
//            )
            val decryptedFile = File(
                context.applicationContext.filesDir, when {
                    encryptedFileName.endsWith(".p1") -> encryptedFileName.replace(".p1", ".kk")
                    encryptedFileName.endsWith(".p2") -> encryptedFileName.replace(".p2", ".kk")
                    encryptedFileName.endsWith(".p3") -> encryptedFileName.replace(".p3", ".kk")
                    else -> encryptedFileName  // Если файл не имеет ожидаемого расширения
                }
            )

            imageDialog = Dialog(context)
            imageDialog?.setContentView(R.layout.util_dialog_image_view)

            val imageViewDialog = imageDialog?.findViewById(R.id.image_view_dialog) as PhotoView

            val buttonForCover3 = imageDialog?.findViewById<Button>(R.id.button_for_cover3)

            val imageDialogFileName = imageDialog?.findViewById<TextView>(R.id.imageDialogFileName)
            imageDialogFileName?.text = "${textViewName.text}"

            val imageDialogFileDate =
                imageDialog?.findViewById<TextView>(R.id.imageDialogFileDate)
            imageDialogFileDate?.text = "${dataSecTextView.text}"

            val imageDialogKey =
                imageDialog?.findViewById<TextView>(R.id.imageDialogKey)
            when {
                fileName.endsWith("p1", ignoreCase = true) -> {
                    imageDialogKey?.text = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME1)
                }
                fileName.endsWith("p2", ignoreCase = true) -> {
                    imageDialogKey?.text = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME2)
                }
                fileName.endsWith("p3", ignoreCase = true) -> {
                    imageDialogKey?.text = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET_NAME3)
                }
                else -> {
                    // Для любых других файлов
                    imageDialogKey?.text = ""
                }
            }

            val btnShare = imageDialog?.findViewById<Button>(R.id.image_dialog_btn_share)

            val imageDialogAcceptanceButton =
                imageDialog?.findViewById<Button>(R.id.image_dialog_acceptance_button)

            val btnTernLeft = imageDialog?.findViewById<Button>(R.id.image_dialog_tern_left)

            val btnFAQ = imageDialog?.findViewById<Button>(R.id.image_dialog_faq)
            btnFAQ?.setOnClickListener {
                val displayIntent = Intent(context, FAQActivity::class.java)
                activity.startActivity(displayIntent)
            }
            val btnTernRight = imageDialog?.findViewById<Button>(R.id.image_dialog_tern_right)
            val btnDelete = imageDialog?.findViewById<Button>(R.id.image_dialog_del_pct)

            val miniButtonForCover =
                imageDialog?.findViewById<Button>(R.id.mini_button_for_cover)

            val buttonForCover2 = imageDialog?.findViewById<Button>(R.id.button_for_cover2)

            val loadingIndicator2 = imageDialog?.findViewById<ProgressBar>(R.id.loading_indicator2)

            btnShare?.visibility = View.VISIBLE
            imageDialogAcceptanceButton?.visibility = View.GONE
            btnTernLeft?.visibility = View.INVISIBLE
            btnTernRight?.visibility = View.INVISIBLE
            imageDialogFileDate?.visibility = View.VISIBLE
            btnFAQ?.visibility = View.VISIBLE
            btnDelete?.visibility = View.VISIBLE
            imageDialogKey?.visibility = View.VISIBLE

            if (buttonForCover2 != null) {
                buttonForCover2.setOnClickListener {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (buttonForCover2 != null) {
                            buttonForCover2.visibility = View.GONE
                        }
                        if (loadingIndicator2 != null) {
                            loadingIndicator2.visibility = View.GONE
                        }
                    }, 2000)
                }
            }

            if (miniButtonForCover != null) {
                miniButtonForCover.setOnClickListener {
                    imageDialog?.dismiss()
                    buttonForCover2?.performClick()
                }
            }

            fun performShowLikeItIs() { // метод на отображение нешифрованных
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
                imageDialogFileDate?.visibility = View.VISIBLE

                miniButtonForCover?.setOnClickListener {
                    imageDialog?.dismiss()
                    buttonForCover2?.performClick()
                }

                imageViewDialog.setOnClickListener {
                    imageDialog?.dismiss()
                    buttonForCover2?.performClick()
                }
            }

            fun performShowDecryptedOne(number: Int) { // метод на отображение шифрованных, number - номер ключа
                try {
                    rotatedBitmap =
                        encryption.decryptImage(decryptedFile, APKM(context).getKeyByNumber(number))

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
                        }, APK.LOAD_PROCESSING_MILLISECONDS)
                    }

                    miniButtonForCover?.setOnClickListener {
                        imageDialog?.dismiss()
                        buttonForCover2?.performClick()
                    }

                    imageDialog?.show()
                    imageDialogFileName?.visibility = View.VISIBLE
//                    buttonsForImageDialog?.visibility = View.VISIBLE
                    Log.d(
                        "=== PhotoListAdapter",
                        "=== Вывод дешифрованного изображения через Dialog"
                    )
                    imageViewDialog.setOnClickListener { // клик пор полю для выхода
                        imageDialog?.dismiss()
                        delPeekaboo(encryptedFileName)
                        buttonForCover2?.performClick()
                    }
                } catch (e: Exception) {
                    // Отобразить сообщение об ошибке
                    activity.showToast(context.getString(R.string.decryption_error))
                    activity.showToast(context.getString(R.string.error_key))
                }
            }

            btnDelete?.setOnClickListener { // удаляем пикчу и выходим из imageDialog
                activity.showToast(context.getString(R.string.wait))
                delPeekaboo(encryptedFileName)
                FileProviderAdapter.deleteFile(
                    encryptedFile.name, context.applicationContext
                )
                FileProviderAdapter.deleteFile(
                    decryptedFile.name, context.applicationContext
                )
                (context as Activity).finish()
                val intent = Intent(context, ItemLoaderActivity::class.java)
                context.startActivity(intent)
                imageDialog?.dismiss()
                buttonForCover2?.performClick()
            }

            // обработка клика по фотокарточке
            if (encryptedFileName.endsWith(".o", true) || encryptedFileName.endsWith(
                    ".jpg", true
                ) || encryptedFileName.endsWith(".jpeg", true) || encryptedFileName.endsWith(
                    ".png", true
                ) || encryptedFileName.endsWith(".gif", true) || encryptedFileName.endsWith(
                    ".bmp", true
                ) || encryptedFileName.endsWith(".webp", true) || encryptedFileName.endsWith(
                    ".unknown", true
                )
            ) {
                performShowLikeItIs()

            } else if (encryptedFileName.endsWith(".share", true)) {
                performShowLikeItIs()
                showShareImageDialog(encryptedFile)

            } else if (encryptedFileName.endsWith(".p1", true)) {
                performShowDecryptedOne(1)
            } else if (encryptedFileName.endsWith(".p2", true)) {
                performShowDecryptedOne(2)
            } else if (encryptedFileName.endsWith(".p3", true)) {
                performShowDecryptedOne(3)
            }
            activity.hideLoadingIndicator(true)
        }
    }

    private fun delPeekaboo(encryptedFileName: String) {// явное удаление битмапы и peekaboo
        if (::rotatedBitmap.isInitialized) {
            recycleBitmap(rotatedBitmap) // явное удаление rotatedBitmap
        }
        val fileNameWithExtension = "${encryptedFileName}eekaboo"
        FileProviderAdapter.deleteFile(fileNameWithExtension, context)
    }

    private fun shareIncryptedImage( // шифровки
        imageUri: Uri, fileNameWithExtension: String, buttonForCover3: Button,
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        val contentUri = FileProviderAdapter.getUriForFile(context, File(imageUri.path!!))
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        val appId = "com.pavlov.MyShadowGallery"
        val appLinkText = context.resources.getString(R.string.share_app_text2, appId)
        shareIntent.putExtra(Intent.EXTRA_TEXT, appLinkText)
        context.startActivity(
            Intent.createChooser(
                shareIntent, context.getString(R.string.share_the_img)
            )
        )
        Handler(Looper.getMainLooper()).postDelayed({
            buttonForCover3.visibility = View.GONE
        }, APK.LOAD_PROCESSING_MILLISECONDS)
    }

    private fun shareDecryptedImage(
        imageUri: Uri, encryptedFileName: String, buttonForCover3: Button,
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        context.startActivity(
            Intent.createChooser(
                shareIntent, context.getString(R.string.share_the_img)
            )
        )
        Handler(Looper.getMainLooper()).postDelayed({
            buttonForCover3.visibility = View.GONE
        }, APK.LOAD_PROCESSING_MILLISECONDS)
    }

    private fun shareAnyOthertedImage(
        imageUri: Uri, fileNameWithExtension: String, buttonForCover3: Button,
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        // метод из ImageUtils для получения безопасного URI файла
        val contentUri = FileProviderAdapter.getUriForFile(context, File(imageUri.path!!))
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        context.startActivity(
            Intent.createChooser(
                shareIntent, context.getString(R.string.share_the_img)
            )
        )
        Handler(Looper.getMainLooper()).postDelayed({
            buttonForCover3.visibility = View.GONE
        }, APK.LOAD_PROCESSING_MILLISECONDS)
    }

    private fun showShareOptionsDialog(
        decryptedFile: File,
        decryptedBitmap: Bitmap,
        encryptedFileName: String,
        encryptedFile: File, // тут привью
        buttonForCover3: Button,
//        loadingIndicator3: ProgressBar
    ) {
        val options = arrayOf(
            context.getString(R.string.encrepted_img),
            context.getString(R.string.decripted_img),
            context.getString(R.string.timber_img)
        )
        val fileNameWithExtension = "${encryptedFileName}eekaboo"
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.chuse_img))

        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    shareIncryptedImage(
                        Uri.fromFile(decryptedFile),
                        "",
                        buttonForCover3,
//                        loadingIndicator3
                    )
                }

                1 -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        activity.showToast(context.getString(R.string.wait))
                        activity.showToast(fileNameWithExtension)
                        val decryptedFile = FileProviderAdapter.bitmapToFileByKorutin(
                            decryptedBitmap, context, fileNameWithExtension
                        )
                        val decryptedUri = FileProviderAdapter.getUriForFile(context, decryptedFile)
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

    private fun showShareImageDialog(encryptedFile: File) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.saving_option))

        val options = arrayOf(
            context.getString(R.string.saving_option1),
            context.getString(R.string.saving_option2),
            context.getString(R.string.saving_option3),
            context.getString(R.string.saving_option5)
        )

        builder.setItems(options) { _, which ->
            when (which) {
                0 -> { // сохраняем без шифрования
                    createFileFrom(encryptedFile, ".o")
                    deleteFile(encryptedFile)
                    closeContext()
                }

                1 -> { // сохраняем c шифрованием
                    val outputFile = createFileFrom(encryptedFile, ".k")
                    val uri = Uri.fromFile(outputFile)
                    encryption.createThumbnail(context.applicationContext, uri)
                    try {
                        if (outputFile != null) {
                            encryption.encryptImage(
                                uri,
                                outputFile.name,
                                APKM(context).getDefauldKey()
                            )
                        }
                    } catch (e: Exception) {
                        activity.showToast(context.getString(R.string.encryption_error))
                    }
                    deleteFile(encryptedFile)
                    closeContext()
                }

                2 -> {  // сохраняем для расшифровки моим ключом
                    showShareImageDialogINSIDE { selectedKey, selectedValue ->
                        try {
                            // Добавьте обработку значения
                            var newEncryption = APKM(context).getDefauldKey()
                            var newDecryption = APKM(context).getDefauldKey()
                            when (selectedValue) {
                                1 -> {
                                    newEncryption =
                                        APKM(context).getMastersSecret(APK.KEY_BIG_SECRET1)
                                    newDecryption = newEncryption
                                }

                                2 -> {
                                    newEncryption =
                                        APKM(context).getMastersSecret(APK.KEY_BIG_SECRET2)
                                    newDecryption = newEncryption
                                }

                                3 -> {
                                    newEncryption =
                                        APKM(context).getMastersSecret(APK.KEY_BIG_SECRET3)
                                    newDecryption = newEncryption
                                }

                                4 -> {
                                    newEncryption = selectedKey
                                }

                                else -> {
                                    activity.showToast(context.getString(R.string.decryption_error))
                                }
                            }

                            rotatedBitmap = encryption.decryptImage(
                                encryptedFile, newEncryption
                            )

                            MainScope().launch {
                                activity.showLoadingIndicator()// в фоновом потоке, Корутина
                                activity.showToast(context.getString(R.string.wait))

                                val imageFile =
                                    FileProviderAdapter.bitmapToFileByKorutin(
                                        rotatedBitmap,
                                        context.applicationContext,
                                        encryptedFile.name
                                    )

                                // Явное освобождение ресурсов
                                FileProviderAdapter.recycleBitmap(rotatedBitmap)

                                val fileUri = FileProviderAdapter.getUriForFile(
                                    context.applicationContext, imageFile
                                )
                                encryption.createThumbnail(context.applicationContext, fileUri)
                                val newName = imageFile.nameWithoutExtension + ".k"
                                try {
                                    encryption.encryptImage(
                                        fileUri,
                                        newName,
                                        newDecryption
                                    )

                                    // Удаление временного файла
                                    val fileToDelete =
                                        File(context.applicationContext.filesDir, imageFile.name)
                                    if (fileToDelete.exists()) {
                                        fileToDelete.delete()
                                    }
                                } catch (e: Exception) {
                                    activity.showToast(context.getString(R.string.encryption_error))
                                }
                                activity.hideLoadingIndicator(true) // завершение индикатора
                                val intent = Intent(context, ItemLoaderActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                            } // завершение корутины
                            notifyDataSetChanged()
                        } catch (e: Exception) {
                            activity.showToast(context.getString(R.string.decryption_error))
                        }
                        closeContext()
                    }
                }

                3 -> { // удаляем
                    deleteFile(encryptedFile)
                    closeContext()
                }
            }
        }

        builder.show()
    }

    private fun showShareImageDialogINSIDE(onKeySelected: (String, Int) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.choose_encryption_key))
        val apkManager = APKM(context)
        val name1 = apkManager.getMastersSecret(APK.KEY_BIG_SECRET_NAME1)
        val name2 = apkManager.getMastersSecret(APK.KEY_BIG_SECRET_NAME2)
        val name3 = apkManager.getMastersSecret(APK.KEY_BIG_SECRET_NAME3)
        // Создаем список для хранения непустых и не-null значений
        val keyList = mutableListOf<String>()
        if (name1.isNotEmpty()) {
            keyList.add(name1)
        }
        if (name2.isNotEmpty()) {
            keyList.add(name2)
        }
        if (name3.isNotEmpty()) {
            keyList.add(name3)
        }
        // Проверяем, есть ли хотя бы один предопределенный ключ
        if (keyList.isNotEmpty()) {
            // Добавляем вариант ввода вручную
            keyList.add(context.getString(R.string.enter_encryption_key1))

            val keys = keyList.toTypedArray()

            builder.setItems(keys) { _, which ->
                when (which) {
                    keys.size - 1 -> {
                        // Вариант ввода ключа вручную
                        activity.showManualKeyInputDialog(context.getString(R.string.enter_encryption_key2)) { enteredKey ->
                            onKeySelected(enteredKey, 4) // 4 - пользователь ввел ключ сам
                        }
                    }

                    else -> {
                        // Выбран один из предопределенных ключей
                        val selectedKey = keys[which]
                        val selectedValue = when (which) {
                            0 -> 1 // name1
                            1 -> 2 // name2
                            2 -> 3 // name3
                            else -> 0 // По умолчанию
                        }
                        onKeySelected(selectedKey, selectedValue)
                    }
                }
            }
            builder.show()
        } else {
            // Обработка ситуации, когда нет предопределенных ключей
            activity.showManualKeyInputDialog(context.getString(R.string.enter_encryption_key2)) { enteredKey ->
                onKeySelected(enteredKey, 4) // 4 - пользователь ввел ключ сам
            }
        }
    }


    private fun createFileFrom(encryptedFile: File, expansion: String): File? {
        Log.d("=== PhotoListAdapter", "=== Option 0 selected")
        activity.showToast(context.getString(R.string.download))
        val folder = context.applicationContext.filesDir
        Log.d("=== PhotoListAdapter", "=== Files directory: ${folder.absolutePath}")
        val fileName = removeFileExtension(encryptedFile.name)
        Log.d("=== PhotoListAdapter", "=== Generated file name: $fileName")
        val outputFile = File(folder, "$fileName$expansion")
        Log.d(
            "=== PhotoListAdapter",
            "=== Output file path: ${outputFile.absolutePath}"
        )
        val uri = Uri.fromFile(encryptedFile)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        if (outputFile.exists()) {
            Log.d("=== PhotoListAdapter", "=== Output file exists")
            val updatedUri = outputFile.toUri()
            Log.d("=== PhotoListAdapter", "=== Updated URI: $updatedUri")
            encryption.addPhotoToList(0, updatedUri)
            activity.showToast(context.getString(R.string.done))
            return outputFile
        } else {
            Log.e("=== PhotoListAdapter", "=== Output file does not exist")
            activity.showToast(context.getString(R.string.save_error))
            return null
        }

    }


    private fun deleteFile(encryptedFile: File) {
        activity.showToast(context.getString(R.string.wait))
        delPeekaboo(encryptedFile.name)
        FileProviderAdapter.deleteFile(
            encryptedFile.name, context.applicationContext
        )
    }

    fun closeContext() {
        (context as Activity).finish()
        val intent = Intent(context, ItemLoaderActivity::class.java)
        context.startActivity(intent)
        imageDialog?.dismiss()
    }


    private fun removeFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf(".")
        return if (lastDotIndex == -1) {
            fileName
        } else {
            fileName.substring(0, lastDotIndex)
        }
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


//    private fun Context.showToast(text: String) {
//        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
//    }

}


