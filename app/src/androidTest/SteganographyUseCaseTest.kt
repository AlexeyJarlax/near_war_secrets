import android.net.Uri
import com.pavlov.MyShadowGallery.data.repository.SteganographyRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SteganographyUseCaseTest {

    private lateinit var steganographyRepository: SteganographyRepository
    private lateinit var steganographyUseCase: SteganographyUseCase

    @Before
    fun setUp() {
        steganographyRepository = mockk()
        steganographyUseCase = SteganographyUseCase(steganographyRepository)
    }

    @Test
    fun `hideImage should call repository hideImageInMeme`() = runTest {
        // Подготовка
        val memeBitmap = mockk<android.graphics.Bitmap>(relaxed = true)
        val originalBitmap = mockk<android.graphics.Bitmap>(relaxed = true)
        every { steganographyRepository.hideImageInMeme(memeBitmap, originalBitmap) } returns mockk()

        // Выполнение
        steganographyUseCase.hideImage(memeBitmap, originalBitmap)

        // Проверка
        verify { steganographyRepository.hideImageInMeme(memeBitmap, originalBitmap) }
    }

    @Test
    fun `extractOriginalImage should call repository extractOriginalFromMeme`() = runTest {
        // Подготовка
        val memeUri = mockk<Uri>(relaxed = true)
        every { steganographyRepository.extractOriginalFromMeme(memeUri) } returns mockk()

        // Выполнение
        steganographyUseCase.extractOriginalImage(memeUri)

        // Проверка
        verify { steganographyRepository.extractOriginalFromMeme(memeUri) }
    }

    @Test
    fun `hasMarker should return true when marker matches`() {
        // Подготовка
        val bitmap = mockk<android.graphics.Bitmap>(relaxed = true)
        every { bitmap.width } returns 10
        every { bitmap.height } returns 10

        // Настройка пикселей для маркера
        val headerCode = SteganographyUseCase.SteganographyConstants.HEADER_CODE
        for (i in 0 until SteganographyUseCase.SteganographyConstants.HEADER_SIZE) {
            val x = i % bitmap.width
            val y = i / bitmap.width
            val bits = (headerCode shr ((SteganographyUseCase.SteganographyConstants.HEADER_SIZE - 1 - i) * 4)) and 0xF
            val pixel = (0xFF shl 24) or (bits shl 16) or (bits shl 8) or bits
            every { bitmap.getPixel(x, y) } returns pixel
        }

        // Выполнение
        val result = steganographyUseCase.hasMarker(bitmap)

        // Проверка
        assert(result)
    }

    @Test
    fun `hasMarker should return false when marker does not match`() {
        // Подготовка
        val bitmap = mockk<android.graphics.Bitmap>(relaxed = true)
        every { bitmap.width } returns 10
        every { bitmap.height } returns 10

        // Настройка пикселей без маркера
        for (i in 0 until SteganographyUseCase.SteganographyConstants.HEADER_SIZE) {
            val x = i % bitmap.width
            val y = i / bitmap.width
            val pixel = (0xFF shl 24) or (0x00 shl 16) or (0x00 shl 8) or 0x00
            every { bitmap.getPixel(x, y) } returns pixel
        }

        // Выполнение
        val result = steganographyUseCase.hasMarker(bitmap)

        // Проверка
        assert(!result)
    }
}