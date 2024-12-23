//package com.pavlov.MyShadowGallery.com.pavlov.MyShadowGallery.data.repository
//
//import android.content.ContentResolver
//import android.content.Context
//import android.graphics.Bitmap
//import android.net.Uri
//import androidx.core.content.FileProvider
//import com.pavlov.MyShadowGallery.data.repository.SteganographyRepository
//import com.pavlov.MyShadowGallery.data.repository.StegoEvent
//import junit.framework.TestCase.assertTrue
//import junit.framework.TestCase.assertFalse
//import junit.framework.TestCase.assertEquals
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.toList
//import kotlinx.coroutines.test.runTest
//import org.junit.Before
//import org.junit.Test
//import org.mockito.ArgumentMatchers.any
//import org.mockito.ArgumentMatchers.eq
//import org.mockito.Mockito
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.`when`
//import java.io.File
//import java.io.InputStream
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class SteganographyRepositoryTest {
//
//    private lateinit var context: Context
//    private lateinit var contentResolver: ContentResolver
//    private lateinit var repository: SteganographyRepository
//
//    @Before
//    fun setUp() {
//        context = mock(Context::class.java)
//        contentResolver = mock(ContentResolver::class.java)
//        `when`(context.contentResolver).thenReturn(contentResolver)
//        val testCacheDir = File("build/testCache").apply { mkdirs() }
//        `when`(context.cacheDir).thenReturn(testCacheDir)
//        Mockito.mockStatic(FileProvider::class.java).use { mockedFileProvider ->
//
//            val fakeUri = Uri.parse("content://fake_generated_uri")
//            mockedFileProvider.`when`<Any> {
//                FileProvider.getUriForFile(
//                    eq(context),
//                    Mockito.anyString(),
//                    any(File::class.java)
//                )
//            }.thenReturn(fakeUri)
//
//            repository = SteganographyRepository(context)
//        }
//    }
//
//    @Test
//    fun `hideImageInMeme - success flow`() = runTest {
//        val memeBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
//        val originalBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
//
//        val flow = repository.hideImageInMeme(memeBitmap, originalBitmap)
//        val events = flow.toList()
//
//        assertTrue("Список событий должен быть не пустым", events.isNotEmpty())
//
//        val lastEvent = events.last()
//        assertTrue("Последнее событие должно быть Success", lastEvent is StegoEvent.Success)
//    }
//
//    @Test
//    fun `hideImageInMeme - error when meme is too small`() = runTest {
//        val memeBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
//        val originalBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
//
//        val events = repository.hideImageInMeme(memeBitmap, originalBitmap).toList()
//
//        val lastEvent = events.last()
//        assertTrue("Последнее событие должно быть Error", lastEvent is StegoEvent.Error)
//        val errorMsg = (lastEvent as StegoEvent.Error).message
//        assertEquals("Мем изображение слишком маленькое для скрытия данных", errorMsg)
//    }
//
//    @Test
//    fun `extractOriginalFromMeme - fail if InputStream is null`() = runTest {
//        val memeUri = Uri.parse("content://fake_meme_uri")
//        `when`(contentResolver.openInputStream(eq(memeUri))).thenReturn(null)
//        val events = repository.extractOriginalFromMeme(memeUri).toList()
//        val lastEvent = events.last()
//        assertTrue("Должно быть событие Error, т.к. bitmap не декодировался", lastEvent is StegoEvent.Error)
//    }
//
//    @Test
//    fun `extractOriginalFromMeme - success flow`() = runTest {
//        val memeUri = Uri.parse("content://fake_meme_uri")
//        val fakePngBytes = javaClass.getResourceAsStream("/testdata/blank.png")?.readBytes()
//        val inputStream = mock(InputStream::class.java)
//
//        `when`(inputStream.read(any(ByteArray::class.java), Mockito.anyInt(), Mockito.anyInt()))
//            .thenAnswer { invocation ->
//                val buffer = invocation.arguments[0] as ByteArray
//                val offset = invocation.arguments[1] as Int
//                val length = invocation.arguments[2] as Int
//                if (fakePngBytes == null) return@thenAnswer -1
//                var bytesCopied = 0
//                while (bytesCopied < length && bytesCopied < fakePngBytes.size) {
//                    buffer[offset + bytesCopied] = fakePngBytes[bytesCopied]
//                    bytesCopied++
//                }
//                if (bytesCopied > 0) bytesCopied else -1
//            }
//
//
//        `when`(contentResolver.openInputStream(eq(memeUri))).thenReturn(inputStream)
//
//        val events = repository.extractOriginalFromMeme(memeUri).toList()
//
//        assertTrue("Список событий не пуст", events.isNotEmpty())
//
//        val lastEvent = events.last()
//
//        if (lastEvent is StegoEvent.Error) {
//            println("extractOriginalFromMeme вернул Error: ${lastEvent.message}")
//        } else if (lastEvent is StegoEvent.Success) {
//            println("extractOriginalFromMeme Успешно: ${lastEvent.uri}")
//        }
//    }
//}
