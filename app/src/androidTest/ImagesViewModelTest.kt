import android.net.Uri
import com.pavlov.MyShadowGallery.data.repository.ImageRepository
import com.pavlov.MyShadowGallery.domain.usecase.SteganographyUseCase
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImagesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var imageRepository: ImageRepository
    private lateinit var steganographyUseCase: SteganographyUseCase

    private lateinit var imagesViewModel: ImagesViewModel

    private val uploadedByMeFlow = MutableStateFlow<List<String>>(emptyList())
    private val showSaveDialogFlow = MutableStateFlow(false)
    private val selectedUriFlow = MutableStateFlow<Uri?>(null)
    private val isLoadingFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        imageRepository = mockk(relaxed = true)
        steganographyUseCase = mockk(relaxed = true)

        every { imageRepository.uploadedByMe } returns uploadedByMeFlow
        every { imageRepository.loadAllImages() } just Runs

        imagesViewModel = ImagesViewModel(imageRepository, steganographyUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addPhoto should call imageRepository addImage`() = runTest {
        // Подготовка
        val testUri = mockk<Uri>(relaxed = true)
        val directoryName = "UPLOADED_BY_ME"
        val fileName = "test_image.jpg"

        every { imageRepository.addImage(testUri, directoryName, any()) } just Runs

        // Выполнение
        imagesViewModel.addPhoto(testUri)

        // Перемещение времени
        advanceUntilIdle()

        // Проверка
        verify { imageRepository.addImage(testUri, directoryName, any()) }
    }

    @Test
    fun `deletePhoto should call imageRepository deleteImage`() = runTest {
        // Подготовка
        val fileName = "test_image.jpg"
        val directoryName = "UPLOADED_BY_ME"

        every { imageRepository.deleteImage(fileName, directoryName) } just Runs

        // Выполнение
        imagesViewModel.deletePhoto(fileName, directoryName)

        // Перемещение времени
        advanceUntilIdle()

        // Проверка
        verify { imageRepository.deleteImage(fileName, directoryName) }
    }

    @Test
    fun `switchCamera should call imageRepository switchCamera`() = runTest {
        // Выполнение
        imagesViewModel.switchCamera()

        // Перемещение времени
        advanceUntilIdle()

        // Проверка
        verify { imageRepository.switchCamera() }
    }

    // Добавьте дополнительные тесты по необходимости
}