package com.pavlov.MyShadowGallery.util

/**  Генерация уникального имени файла  */

import android.content.Context
import timber.log.Timber
import java.io.File
import java.util.Locale

class NamingStyleManager(private val context: Context) {

    private val adjectives: List<String>
    private val nouns: List<String>

    init {
        val locale: Locale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }

        when (locale.language) {
            "ru" -> {
                adjectives = NameUtil.adjectives
                nouns = NameUtil.nouns
                Timber.d("=== Используются русские имена.")
            }
            "en" -> {
                adjectives = EnglishNameUtil.adjectives
                nouns = EnglishNameUtil.nouns
                Timber.d("=== Используются английские имена.")
            }
            "es" -> {
                adjectives = SpanishNameUtil.adjectives
                nouns = SpanishNameUtil.nouns
                Timber.d("=== Используются испанские имена.")
            }
            "zh" -> {
                adjectives = ChineseNameUtil.adjectives
                nouns = ChineseNameUtil.nouns
                Timber.d("=== Используются китайские имена.")
            }

            else -> {
                // Используем английские имена по умолчанию
                adjectives = EnglishNameUtil.adjectives
                nouns = EnglishNameUtil.nouns
                Timber.d("=== Язык не поддерживается. Используются английские имена по умолчанию.")
            }
        }
    }

    fun generateFileName(isEncrypted: Boolean, folder: File): String {
        if (adjectives.isEmpty() || nouns.isEmpty()) {
            Timber.tag("=== FileNameGeneration")
                .d("=== Empty arrays: adjectives=${adjectives.isEmpty()}, nouns=${nouns.isEmpty()}")
            return "FallbackFileName.unknown"
        }

        val randomName = "${adjectives.random()}_${nouns.random()}"
        var fileName = "${randomName}.unknown"

        if (isEncrypted) {
            fileName = "${fileName.substringBeforeLast(".")}.k"
        } else {
            fileName = "${fileName.substringBeforeLast(".")}.o"
        }

        if (folder.exists() || folder.mkdirs()) {
            var counter = 1
            var file = File(folder, fileName)

            Timber.tag("=== FileNameGeneration").d("=== Generated FileName: $fileName")

            while (file.exists()) {
                fileName = "${randomName}_$counter"
                if (isEncrypted) {
                    fileName = "${fileName}.k"
                } else {
                    fileName = "${fileName}.o"
                }
                file = File(folder, fileName)
                counter++

                Timber.tag("=== FileNameGeneration").d("=== Conflict! New FileName: $fileName")
            }
        }
        return applyNamingOption(fileName)
    }

    private fun applyNamingOption(fileName: String): String {
        return fileName
    }
}

object NameUtil {
    val adjectives = listOf(
        "Могучий", "Неудержимый", "Бесстрашный", "Героический", "Безупречный", "Непоколебимый",
        "Неуязвимый", "Подвижный", "Безжалостный", "Несокрушимый", "Несгибаемый", "Невозмутимый",
        "Непокорный", "Разъяренный", "Сильный", "Надежный", "Благородный", "Смелый", "Отважный",
        "Стойкий", "Бессмертный", "Ловкий", "Удивительный", "Исступленный", "Храбрый", "Траурный",
        "Забывчивый", "Юный", "Возросший", "Активный", "Неспящий", "Мягкий", "Ультра", "Грубый"
    )

    val nouns = listOf(
        "Герой", "Воин", "Защитник", "Титан", "Мститель", "Страж", "Чемпион", "Драконоборец",
        "Небожитель", "Каратель", "Избавитель", "Молот", "Меч", "Заклинатель", "Рыцарь", "Захватчик",
        "Разоритель", "Легенда", "Паладин", "Юстициар", "Патриций", "Темплар", "Надзиратель", "Блюститель",
        "Ревнитель", "Кинжал", "Бард", "Следопыт"
    )
}

object EnglishNameUtil {
    val adjectives = listOf(
        "Mighty", "Unstoppable", "Fearless", "Heroic", "Flawless", "Unshakable", "Invulnerable",
        "Agile", "Merciless", "Indomitable", "Unbending", "Imperturbable", "Unyielding", "Unconquerable",
        "Strong", "Reliable", "Noble", "Brave", "Daring", "Steadfast", "Immortal", "Agile", "Amazing",
        "Furious", "Courageous", "Mournful", "Forgetful", "Young", "Grown", "Active", "Sleepless", "Soft",
        "Ultra", "Rough"
    )

    val nouns = listOf(
        "Hero", "Warrior", "Defender", "Titan", "Avenger", "Guardian", "Champion", "DragonSlayer",
        "Skyward", "Punisher", "Deliverer", "Hammer", "Sword", "Spellbinder", "Knight", "Invader",
        "Ravager", "Legend", "Paladin", "Justiciar", "Patrician", "Templar", "Overseer", "Warden",
        "Zealot", "Dagger", "Bard", "Tracker"
    )
}

object SpanishNameUtil {
    val adjectives = listOf(
        "Poderoso", "Imparable", "Intrépido", "Heroico", "Impecable", "Inquebrantable", "Invulnerable",
        "Ágil", "Despiadado", "Incorruptible", "Inflexible", "Imperturbable", "Incorregible", "Airado",
        "Fuerte", "Confiable", "Noble", "Valiente", "Audaz", "Firme", "Inmortal", "Hábil", "Asombroso",
        "Enloquecido", "Bravo", "Conmemorativo", "Olvidadizo", "Joven", "Maduro", "Activo", "Insomne",
        "Suave", "Ultra", "Rudo"
    )

    val nouns = listOf(
        "Héroe", "Guerrero", "Defensor", "Titán", "Vengador", "Guardián", "Campeón", "Cazadragones",
        "Celestial", "Castigador", "Libertador", "Martillo", "Espada", "Invocador", "Caballero", "Conquistador",
        "Destructor", "Leyenda", "Paladín", "Justiciero", "Patricio", "Templario", "Supervisor", "Custodio",
        "Exigente", "Daga", "Bardo", "Rastreador"
    )
}

object ChineseNameUtil {
    val adjectives = listOf(
        "强大的", "不可阻挡的", "无畏的", "英勇的", "无瑕的",
        "坚定不移的", "无懈可击的", "敏捷的", "无情的", "不可战胜的",
        "坚不可摧的", "沉着的", "坚定不移的", "不可征服的", "强大的",
        "可靠的", "高贵的", "勇敢的", "大胆的", "坚定的",
        "不朽的", "灵活的", "惊人的", "狂怒的", "勇敢的",
        "悲痛的", "健忘的", "年轻的", "成熟的", "活跃的",
        "不眠的", "柔软的", "超级的", "粗糙的"
    )

    val nouns = listOf(
        "英雄", "战士", "防御者", "泰坦", "复仇者",
        "守护者", "冠军", "屠龙者", "天际", "判官",
        "解救者", "锤子", "剑", "咒术师", "骑士",
        "侵略者", "毁灭者", "传奇", "圣骑士", "法官",
        "贵族", "圣殿骑士", "监察官", "典狱长", "狂热者",
        "匕首", "吟游诗人", "追踪者"
    )
}

object AllegedlySistemNameUtil {
    val adjectives = listOf(
        "CPU", "GPU", "RAM", "ROM", "BIOS", "OS", "HDD", "SSD", "USB", "LAN", "WAN", "ISP", "VPN",
        "URL", "HTML", "CSS", "JavaScript", "API", "GUI", "IDE", "cd", "ef", "gh", "ij", "kl", "mn",
        "op", "qr", "st", "uv", "wx", "yz", "abc", "def", "ghi", "jkl", "mno", "pqr", "stu", "vwx",
        "mnop", "qrstu", "vwxyz", "Central", "Processing", "Graphics", "Operative", "Read-Only",
        "Basic", "Input", "Operating", "Hard", "Solid", "Universal", "Local", "Wide",
        "Internet", "Service", "Virtual", "Uniform", "Hypertext", "Cascading", "Programming",
        "Application", "Interface", "Graphical", "Integrated", "Development"
    )

    val nouns = listOf(
        "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "zero", "byte", "data", "code", "task", "file", "user", "name", "log", "error",
        "warn", "info", "debug", "config", "screen", "input", "output", "device", "drive", "block",
        "net", "work", "port", "signal", "power", "memory", "disk", "system", "kernel", "mode",
        "alert", "index", "queue", "stack", "table", "hash", "index", "link", "node", "leaf",
        "yza", "bcd", "efg", "hij", "klm", "nop", "qrs", "tuv", "wxy", "zab", "cde", "fgh", "ijkl",
        "IoE", "BIOS", "CMOS", "HTML5", "CLI", "WPA", "WEP", "URL", "LAN",
        "WAN", "MAN", "RAM", "CPU", "GPU", "DNS", "GUI", "SSL", "OSI",
        "SMTP", "POP3", "IMAP", "HTTP", "HTTPS", "TCP-IP", "API", "SDK"
    )
}