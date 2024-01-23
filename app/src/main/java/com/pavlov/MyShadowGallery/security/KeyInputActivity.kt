package com.pavlov.MyShadowGallery.security

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.pavlov.MyShadowGallery.MainPageActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import kotlin.random.Random

class KeyInputActivity : AppCompatActivity() {

    private lateinit var keyName: EditText
    private lateinit var pencil: ImageButton
    private lateinit var generateButton: Button
    private lateinit var keyInputEditText: EditText
    private lateinit var keyInputEditTextInfo: TextView
    private lateinit var variableKey: CheckBox
    private lateinit var variableKeyText: TextView

    private lateinit var addNewKey: Button
    private lateinit var addNewKeyText: TextView
    private lateinit var saveNewKey: Button
    private lateinit var saveKeyText: TextView
    private lateinit var doNotUseKey: Button
    private lateinit var buttonOldKeyText: TextView

    private lateinit var availableKeysText: TextView
    private lateinit var oldKey1Text: TextView
    private lateinit var oldKey1Del: Button
    private lateinit var oldKey2Text: TextView
    private lateinit var oldKey2Del: Button
    private lateinit var oldKey3Text: TextView
    private lateinit var oldKey3Del: Button
    private lateinit var textOldKey: TextView

    private lateinit var keyInfo1: TextView
    private lateinit var keyInfo2: TextView

    private var confirmable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_input)

        // новый ключ
        keyName = findViewById(R.id.edit_text_key_name)
        pencil = findViewById(R.id.pencil)
        generateButton = findViewById(R.id.button_generate)
        keyInputEditText = findViewById(R.id.edit_text_key)
        keyInputEditTextInfo = findViewById(R.id.edit_text_key_info)
        variableKey = findViewById(R.id.variable_key)
        variableKeyText = findViewById(R.id.variable_key_text)

        // основные кнопки управления
        addNewKey = findViewById(R.id.add_new_key)
        addNewKeyText = findViewById(R.id.add_new_key_text)
        saveNewKey = findViewById(R.id.save_key)
        saveKeyText = findViewById(R.id.save_key_text)
        doNotUseKey = findViewById(R.id.button_old_key)
        buttonOldKeyText = findViewById(R.id.button_old_key_text)

        // старые ключи
        availableKeysText = findViewById(R.id.available_keys)
        oldKey1Text = findViewById(R.id.old_key_1)
        oldKey1Del = findViewById(R.id.old_key_1_del)
        oldKey2Text = findViewById(R.id.old_key_2)
        oldKey2Del = findViewById(R.id.old_key_2_del)
        oldKey3Text = findViewById(R.id.old_key_3)
        oldKey3Del = findViewById(R.id.old_key_3_del)
        textOldKey = findViewById(R.id.text_old_key)

        // справка
        keyInfo1 = findViewById(R.id.key_info1)
        keyInfo2 = findViewById(R.id.key_info2)

        keyName.setText(APKM(context = this).generateUniqueKeyName())

        if (APKM(context = this).countBigSecrets() > 0) {
            hideNewKeyScript()
            oldKeyScript()
            addNewKey.setOnClickListener {
                hideOldKeyScript()
                newKeyScript()
            }
        } else {
            newKeyScript()
        }

        keyName.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val input = SpannableStringBuilder()
            for (i in start until end) {
                val c = source[i]
                if (c.toString().matches(Regex(APK.REGEX))) {
                    input.append(c)
                } else {
                    variableKeyText.text = getString(R.string.invalid_character)
                }
            }
            input
        })

        keyInputEditText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val input = SpannableStringBuilder()
            for (i in start until end) {
                val c = source[i]
                if (c.toString().matches(Regex(APK.REGEX))) {
                    input.append(c)
                } else {
                    variableKeyText.text = getString(R.string.invalid_character)
                }
            }
            input
        })

        keyName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyValue = s?.toString()?.trim()
                if (keyValue?.isNotEmpty() == true) {
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        keyInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyValue = s?.toString()?.trim()

                if (keyValue?.isNotEmpty() == true) {
                    val byteCount = keyValue.toByteArray(Charsets.UTF_8).size
                    val remainingBytes = 16 - byteCount

                    val message = when {
                        remainingBytes > 0 -> {
                            getString(R.string.key_byte_count_message) + " $remainingBytes"
                        }

                        remainingBytes < 0 -> {
                            ""
                        }

                        else -> {
                            confirmable = true
                            "" // Для случая, когда количество байт равно 16
                        }
                    }

                    val containsInvalidCharacters = keyValue.contains(" ")
                    val isValid = !containsInvalidCharacters && remainingBytes >= 0

                    keyInputEditTextInfo.text = message
                    keyInputEditTextInfo.setTextColor(if (isValid) Color.GREEN else Color.RED)

                    // Делаем видимыми основные кнопки управления
                    addNewKey.visibility = View.GONE
                    addNewKeyText.visibility = View.GONE
                    saveNewKey.visibility = View.VISIBLE
                    saveKeyText.visibility = View.VISIBLE
                    doNotUseKey.visibility = View.VISIBLE
                    buttonOldKeyText.visibility = View.VISIBLE
                } else {
                    // Делаем невидимыми основные кнопки управления
                    addNewKey.visibility = View.GONE
                    addNewKeyText.visibility = View.GONE
                    saveNewKey.visibility = View.GONE
                    saveKeyText.visibility = View.GONE
                    doNotUseKey.visibility = View.GONE
                    buttonOldKeyText.visibility = View.GONE
                    confirmable = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Не используется
            }
        })

// чек бокс на удаление ключей ********************************************************************

        val isSaveAfterSession =
            !APKM(context = this).getBooleanFromSPK(APK.KEY_DELETE_AFTER_SESSION)

        variableKey.isChecked = isSaveAfterSession
        if(!isSaveAfterSession) {
            variableKeyText.text = getString(R.string.do_not_save_key_in_sistem3)
            saveNewKey.text = "⏳"
            saveKeyText.text = getString(R.string.save_key_on_session)
        }

        variableKey.setOnCheckedChangeListener { buttonView, isChecked ->
            // Сохраняем состояние в SharedPreferences
            APKM(context = this).saveBooleanToSPK(APK.KEY_DELETE_AFTER_SESSION, !isChecked)

            // Меняем текст в зависимости от нового состояния isChecked
            if (isChecked) {
                variableKeyText.text = getString(R.string.do_not_save_key_in_sistem2)
                saveNewKey.text = "💾"
                saveKeyText.text = getString(R.string.save_key)
            } else {
                variableKeyText.text = getString(R.string.do_not_save_key_in_sistem3)
                saveNewKey.text = "⏳"
                saveKeyText.text = getString(R.string.save_key_on_session)
            }
        }

        generateButton.setOnClickListener {
            val keyEditText = findViewById<EditText>(R.id.edit_text_key)
            val generatedKey = generateRandomKey(16) // Генерация 16-битного случайного ключа
            keyEditText.setText(generatedKey)
        }

        saveNewKey.setOnClickListener { // сохранить ключ
            if (APKM(context = this).getBooleanFromSPK(APK.KEY_DELETE_AFTER_SESSION)) {
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET_NAME1)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET1)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET_NAME2)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET2)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET_NAME3)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET3)
            }
            val nameKeyValue = keyName.text.toString().trim()
            val keyValue = keyInputEditText.text.toString().trim()
            confirm(keyValue, nameKeyValue)
        }

        doNotUseKey.setOnClickListener { // не использовать ключ шифрования
            APKM(context = this).saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, false)
            APKM(context = this).delMastersSecret(APK.DEFAULT_KEY)
            Toast.makeText(this, R.string.encryption_key_not_set, Toast.LENGTH_SHORT).show()
            exit()
        }

//старые ключи ************************************************************************************
        if (APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET_NAME1).isNotBlank()) {
            oldKey1Text.visibility = View.VISIBLE
            oldKey1Del.visibility = View.VISIBLE
            oldKey1Text.text = APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET_NAME1)
            oldKey1Text.setOnClickListener {
                APKM(context = this).saveCounter(APK.DEFAULT_KEY, 1)
                exit()
            }
            oldKey1Del.setOnClickListener {
                oldKey1Text.visibility = View.GONE
                oldKey1Del.visibility = View.GONE
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET_NAME1)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET1)
                Toast.makeText(this, R.string.key_delited, Toast.LENGTH_SHORT).show()
                clearList()
            }
        }

        if (APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET_NAME2).isNotBlank()) {
            oldKey2Text.visibility = View.VISIBLE
            oldKey2Del.visibility = View.VISIBLE
            oldKey2Text.text = APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET_NAME2)
            oldKey2Text.setOnClickListener {
                APKM(context = this).saveCounter(APK.DEFAULT_KEY, 2)
                exit()
            }
            oldKey2Del.setOnClickListener {
                oldKey2Text.visibility = View.GONE
                oldKey2Del.visibility = View.GONE
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET_NAME2)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET2)
                Toast.makeText(this, R.string.key_delited, Toast.LENGTH_SHORT).show()
                clearList()
            }
        }

        if (APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET_NAME3).isNotBlank()) {
            oldKey3Text.visibility = View.VISIBLE
            oldKey3Del.visibility = View.VISIBLE
            oldKey3Text.text = APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET_NAME3)
            oldKey3Text.setOnClickListener {
                APKM(context = this).saveCounter(APK.DEFAULT_KEY, 3)
                exit()
            }
            oldKey3Del.setOnClickListener {
                oldKey3Text.visibility = View.GONE
                oldKey3Del.visibility = View.GONE
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET_NAME3)
                APKM(context = this).delMastersSecret(APK.KEY_BIG_SECRET3)
                Toast.makeText(this, R.string.key_delited, Toast.LENGTH_SHORT).show()
                clearList()
            }
        }

        var clickCount1 = 0
        keyInfo1.setOnClickListener {
            when (clickCount1 % 2) {
                0 -> keyInfo1.text = getString(R.string.about_app_long6)
                1 -> keyInfo1.text = getString(R.string.what_key_sistem_to_choose)
            }
            clickCount1++
        }

        var clickCount2 = 0
        keyInfo2.setOnClickListener {
            when (clickCount2 % 2) {
                0 -> keyInfo2.text = getString(R.string.how_does_the_key_works_details)
                1 -> keyInfo2.text = getString(R.string.how_does_the_key_works)
            }
            clickCount2++
        }
    } // конец онКриейт

    private fun newKeyScript() {
        // новый ключ
        keyName.visibility = View.VISIBLE
        pencil.visibility = View.VISIBLE
        generateButton.visibility = View.VISIBLE
        keyInputEditText.visibility = View.VISIBLE
        variableKey.visibility = View.VISIBLE
        variableKeyText.visibility = View.VISIBLE
    }

    private fun hideNewKeyScript() {
        keyName.visibility = View.GONE
        pencil.visibility = View.GONE
        generateButton.visibility = View.GONE
        keyInputEditText.visibility = View.GONE
        variableKey.visibility = View.GONE
        variableKeyText.visibility = View.GONE
    }

    private fun oldKeyScript() {
        // основные кнопки управления
        addNewKey.visibility = View.VISIBLE
        addNewKeyText.visibility = View.VISIBLE
        doNotUseKey.visibility = View.VISIBLE
        buttonOldKeyText.visibility = View.VISIBLE

        // старые ключи
        availableKeysText.visibility = View.VISIBLE
        textOldKey.visibility = View.VISIBLE
    }

    private fun hideOldKeyScript() {
        // основные кнопки управления
        addNewKey.visibility = View.GONE
        addNewKeyText.visibility = View.GONE

        // старые ключи
        availableKeysText.visibility = View.GONE
        oldKey1Text.visibility = View.GONE
        oldKey1Del.visibility = View.GONE
        oldKey2Text.visibility = View.GONE
        oldKey2Del.visibility = View.GONE
        oldKey3Text.visibility = View.GONE
        oldKey3Del.visibility = View.GONE
        textOldKey.visibility = View.GONE
    }

    private fun clearList() {
        if (APKM(context = this).countBigSecrets() <= 0) {
            availableKeysText.visibility = View.GONE
            textOldKey.visibility = View.GONE
        }
    }

    private fun confirm(keyValue: String, nameKeyValue: String) {
        APKM(context = this).saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, true)
        if (keyValue.isNotEmpty()) {
            val secret1 = APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET1)
            val secret2 = APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET2)
            val secret3 = APKM(context = this).getMastersSecret(APK.KEY_BIG_SECRET3)
            if (secret1.isNullOrBlank()) {
                APKM(context = this).saveMastersSecret(nameKeyValue, APK.KEY_BIG_SECRET_NAME1)
                APKM(context = this).saveMastersSecret(keyValue, APK.KEY_BIG_SECRET1)
                APKM(context = this).saveIntToSharedPreferences(APK.DEFAULT_KEY, 1)
            } else if (secret2.isNullOrBlank()) {
                APKM(context = this).saveMastersSecret(nameKeyValue, APK.KEY_BIG_SECRET_NAME2)
                APKM(context = this).saveMastersSecret(keyValue, APK.KEY_BIG_SECRET2)
                APKM(context = this).saveIntToSharedPreferences(APK.DEFAULT_KEY, 2)
            } else if (secret3.isNullOrBlank()) {
                APKM(context = this).saveMastersSecret(nameKeyValue, APK.KEY_BIG_SECRET_NAME3)
                APKM(context = this).saveMastersSecret(keyValue, APK.KEY_BIG_SECRET3)
                APKM(context = this).saveIntToSharedPreferences(APK.DEFAULT_KEY, 3)
            } else {
                Toast.makeText(this, R.string.no_available_keys, Toast.LENGTH_LONG).show()
                Toast.makeText(this, R.string.delit_one_keys, Toast.LENGTH_LONG).show()
            }
            APKM(context = this).saveBooleanToSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, true)
            APKM(context = this).saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, true)
            Toast.makeText(this, R.string.encryption_key_set, Toast.LENGTH_SHORT).show()
            exit()

    } else
    {
        Toast.makeText(this, R.string.encryption_key_not_set, Toast.LENGTH_SHORT).show()
//            exit()
    }
}

private fun generateRandomKey(length: Int): String {
    val chars =
        ('a'..'f') + ('0'..'9') // Допустимые символы для ключа (шестнадцатеричные цифры a-f и цифры 0-9)
    val random = Random.Default
    val key = StringBuilder()

    repeat(length) {
        val randomChar = chars[random.nextInt(chars.size)]
        key.append(randomChar)
    }

    return key.toString()
}

override fun onBackPressed() { // юзер сбегает
    super.onBackPressed()
    doNotUseKey.performClick()
}

private fun exit() {
    val displayIntent = Intent(this, MainPageActivity::class.java)
    startActivity(displayIntent)
}
}