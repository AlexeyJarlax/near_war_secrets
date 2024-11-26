package com.pavlov.nearWarSecrets.util

object APK { // AppPreferencesKey Internal - доступно только в модуле
    // хранилища SharedPreferences
    const val PREFS_NAME = "my_prefs_new" // открытое хранилище
    const val MY_SECRETS_PREFS_NAME = "secret_shared_prefs_new" // защищенное хранилище

    // SharedPreferences
    const val KEY_FIRST_RUN = "first_app_run_new" // первый запуск ?
    const val KEY_HISTORY_LIST = "key_for_history_list_new"
    const val DEFAULT_KEY = "default_key_new"
    const val KEY_EXIST_OF_PASSWORD = "parolchik_new"
    const val KEY_EXIST_OF_MIMICRY = "mimicry_new"
    const val KEY_EXIST_OF_ENCRYPTION_K = "exists_of_encryption_kluchik_new" // исключаем из кода
    const val KEY_USE_THE_ENCRYPTION_K = "use_the_encryption_key_new"

    // ENCRYPTED SharedPreferences
    const val KEY_SMALL_SECRET = "my_secret_new"  // короткий секретик
    const val KEY_BIG_SECRET_NAME1 = "my_big_secret_name_1_new"  // 1
    const val KEY_BIG_SECRET1 = "my_big_secret1_new"  // 1
    const val KEY_BIG_SECRET_NAME2 = "my_big_secret_name_2_new"  // 2
    const val KEY_BIG_SECRET2 = "my_big_secret2_new"  // 2
    const val KEY_BIG_SECRET_NAME3 = "my_big_secret_name_3_new"  // 3
    const val KEY_BIG_SECRET3 = "my_big_secret3_new"  // 3
    const val KEY_COUNT_TRY = "how_many_try_to_pass=30df_new"  // счетчик

    // константы
    const val ALBUM_ROUNDED_CORNERS = 8
    const val SERVER_PROCESSING_MILLISECONDS: Long = 1500
    const val LOAD_PROCESSING_MILLISECONDS: Long = 800
    const val HISTORY_TRACK_LIST_SIZE = 25
    const val DEFAULT_PREVIEW_SIZE = 30
    const val DEFAULT_MIMIC_PASS: String = "000"
    
    const val REGEX = "[a-zA-Z0-9.,!?@#\$%^&*()_+-=:;<>{}\\[\\]\"'\\\\/]+"


    // переключатели состояний SharedPreferences
    const val KEY_NIGHT_MODE = "night_mode_new"
    const val KEY_USER_SWITCH = "user_mode_new"

    const val KEY_DELETE_AFTER_SESSION = "delete_ek_when_closing_new"
    const val KEY_PREVIEW_SIZE_SEEK_BAR = "seek_bar_new"
    const val PREF_LANGUAGE_KEY = "selected_language_new"
    const val FILE_NAME_KEY = "file_naming_new"

    //  в рамках сессии
    var KEY_MIMICRY_THIS_SESSION_SWITCH = false
    var EXCLAMATION: Boolean = false
}