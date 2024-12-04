package com.pavlov.nearWarSecrets.ui.Images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesScreen(
    itemLoaderScreen: @Composable () -> Unit,
    extractedImagesScreen: @Composable () -> Unit
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .padding(top = 0.dp)
                    .height(60.dp),
                backgroundColor = Color.Black,
                contentColor = Color.White,
            ) {

                @Composable
                fun createTab(title: String, page: Int) {
                    Tab(
                        selected = pagerState.currentPage == page,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(page)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                createTab("Загруженные", 0)
                createTab("Полученные", 1)
            }

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
}