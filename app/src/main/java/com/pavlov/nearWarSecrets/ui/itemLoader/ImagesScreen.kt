package com.pavlov.nearWarSecrets.ui.itemLoader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.*
import androidx.compose.material.Text
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesScreen(
    itemLoaderScreen: @Composable () -> Unit,
    extractedImagesScreen: @Composable () -> Unit
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope() // Для запуска корутин

    Column {
        // Вкладки
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    coroutineScope.launch { // Запуск корутины для scrollToPage
                        pagerState.scrollToPage(0)
                    }
                }
            ) {
                Text("Загрузчик")
            }
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch { // Запуск корутины для scrollToPage
                        pagerState.scrollToPage(1)
                    }
                }
            ) {
                Text("Извлеченные изображения")
            }
        }
        // Содержимое страниц
        HorizontalPager(
            count = 2,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> itemLoaderScreen()
                1 -> extractedImagesScreen()
            }
        }
    }
}