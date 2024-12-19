import android.content.Context
import android.net.Uri
import com.pavlov.MyShadowGallery.data.utils.ImageUriHelper
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ImageRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var imageUriHelper: ImageUriHelper
    private lateinit var imageRepository: ImageRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        imageUriHelper = mockk(relaxed = true)

        imageRepository = ImageRepository(context, imageUriHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addImage should save image and update flows`() = runTest {
        // Подготовка
        val testUri = mockk<Uri>(relaxed = true)
        val directoryName = "uploadedbyme"
        val fileName = "test_image.jpg"

        val file = mockk<File>(relaxed = true)
        every { imageUriHelper.getFileUri(fileName) } returns mockk()

        // Мокирование открытия InputStream
        val inputStream = mockk<InputStream>(relaxed = true)
        every { context.contentResolver.openInputStream(testUri) } returns inputStream

        // Мокирование FileOutputStream
        val outputStream = mockk<FileOutputStream>(relaxed = true)
        every { any<File>().outputStream() } returns outputStream

        // Выполнение
        imageRepository.addImage(testUri, directoryName, fileName)

        // Перемещение времени
        advanceUntilIdle()

        // Проверка
        verify { context.contentResolver.openInputStream(testUri) }
        verify { any<File>().outputStream() }
        verify { inputStream.copyTo(outputStream) }
        verify { inputStream.close() }
        verify { outputStream.close() }

        // Проверка вызова loadAllImages
        coVerify { imageRepository.loadAllImages() }
    }

    // Добавьте дополнительные тесты по необходимости
}