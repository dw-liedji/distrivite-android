package com.datavite.distrivite.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalTabPager(modifier: Modifier = Modifier, tabItems:List<TabPagerItem>, content: @Composable (tabItems:List<TabPagerItem>, index:Int) -> Unit) {

    Surface (
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        var selectedTabIndex by remember {
            mutableIntStateOf(0)
        }

        val pagerState = rememberPagerState {
            tabItems.size
        }

        LaunchedEffect(selectedTabIndex){
            pagerState.animateScrollToPage(selectedTabIndex)
        }

        LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress){
            if (!pagerState.isScrollInProgress) selectedTabIndex = pagerState.currentPage
        }

        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                tabItems.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = tabItem.title) },

                        icon = {
                            Icon(imageVector =
                            if (index == selectedTabIndex) tabItem.selectedIcon
                            else tabItem.unSelectedIcon, contentDescription = tabItem.title)
                        }
                    )
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {index -> content(tabItems,index)
            }
        }
    }
}


