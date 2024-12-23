//package com.pavlov.MyShadowGallery.data.repository
//
//import org.junit.Assert.assertTrue
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertEquals
//import android.content.ContentResolver
//import android.content.Context
//import kotlinx.coroutines.test.runTest
//import org.junit.Rule
//import org.junit.Test
//import java.io.ByteArrayInputStream
//import java.io.File
//import java.io.FileNotFoundException
//import android.net.Uri
//import com.pavlov.MyShadowGallery.data.utils.ImageUriHelper
//import io.mockk.*
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.*
//import org.junit.Before
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.`when`
//
//@OptIn(ExperimentalCoroutinesApi::class)
//open class ImageRepositoryTest {
//
//    @get:Rule
//    val mainDispatcherRule = MainDispatcherRule()
//
//    private lateinit var repository: ImageRepository
//    private lateinit var mockContext: Context
//    private lateinit var mockContentResolver: ContentResolver
//    private lateinit var mockUriHelper: ImageUriHelper
//
//    @Before
//    fun setup() {
//        mockContext = mock(Context::class.java)
//        mockContentResolver = mock(ContentResolver::class.java)
//        mockUriHelper = mock(ImageUriHelper::class.java)
//        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
//        val testDir = File("build/testFolder")
//        testDir.mkdirs()
//        `when`(mockContext.filesDir).thenReturn(testDir)
//
//        repository = ImageRepository(mockContext, mockUriHelper)
//    }
//
//    @Test
//    fun `addImage - retries opening stream and succeeds on second try`() = runTest {
//        val testUri = Uri.parse("content://test/test.jpg")
//        val directoryName = "UPLOADED_BY_ME"
//
//        `when`(mockContentResolver.openInputStream(testUri))
//            .thenThrow(FileNotFoundException("test file not found"))
//            .thenReturn(ByteArrayInputStream("fake image data".toByteArray()))
//
//        repository.addImage(testUri, directoryName)
//
//        val savedFile = File(mockContext.filesDir, "$directoryName/${repository.getFileName(directoryName)}")
//        assertTrue("Файл должен был создаться", savedFile.exists())
//
//        val content = savedFile.readText()
//        assertEquals("fake image data", content)
//    }
//
//    @Test
//    fun `addImage - fails after three attempts`() = runTest {
//        val testUri = Uri.parse("content://test/test.jpg")
//        val directoryName = "UPLOADED_BY_ME"
//
//        `when`(mockContentResolver.openInputStream(testUri))
//            .thenThrow(FileNotFoundException("test file not found"))
//
//        repository.addImage(testUri, directoryName)
//
//        val savedFile = File(mockContext.filesDir, "$directoryName/${repository.getFileName(directoryName)}")
//        assertFalse("Файл не должен был создаться, т.к. все 3 попытки неудачны", savedFile.exists())
//    }
//}