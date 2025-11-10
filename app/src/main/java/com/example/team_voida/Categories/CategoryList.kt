package com.example.team_voida.Categories

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil3.imageLoader
import coil3.util.DebugLogger
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Home.HomeBigSaleList
import com.example.team_voida.Home.HomeNewList
import com.example.team_voida.Home.HomePopularCall
import com.example.team_voida.Home.HomeSearchBar
import com.example.team_voida.Home.HomeTodaySaleList
import com.example.team_voida.Home.Popular
import com.example.team_voida.Notification.Notification
import com.example.team_voida.SearchResult.SearchProducts
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.ui.theme.BackGroundWhite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun CategoryList(
    navController: NavController,
    input: MutableState<String>,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    isWhichPart: MutableState<Int>,
    barPrice: MutableState<Float>,
    categoryCode: String,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
){

    var title: String = categoryCode

    val imageLoader = LocalContext.current.imageLoader.newBuilder()
        .logger(DebugLogger())
        .build()

    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            Log.e("123","on_pause")
        } else if(event == Lifecycle.Event.ON_STOP){
            Log.e("123","on_stop")
        } else if(event == Lifecycle.Event.ON_DESTROY){
            Log.e("123","on_destroy")
        } else if(event == Lifecycle.Event.ON_CREATE){
            Log.e("123","on_create")
        } else if(event == Lifecycle.Event.ON_START){
            Log.e("123","on_start")
            homeNavFlag.value =true
            basketFlag.value = false
            productFlag.value = false
            selectedIndex.value = 0

        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }
    val result: MutableState<List<Popular>?> = remember { mutableStateOf<List<Popular>?>(null) }


    runBlocking {
        val job = GlobalScope.launch {
            result.value = CategoryServer(categoryCode)
        }
    }


    val scrollState = rememberScrollState()

    if(result.value != null){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(BackGroundWhite)
                .verticalScroll(scrollState)

        ) {
            Notification(title + " 목록 화면입니다. 아래에 검색된 상품들을 만나보세요.")
            HomeSearchBar(
                navController,
                input
            )

            SearchProducts(
                result.value,
                navController,
                barPrice,
                productID,
                isItemWhichPart
            )
            Spacer(Modifier.height(30.dp))
        }
    } else{
        LoaderSet(semantics = "카테고리 상품 정보를 불러오는 중입니다. 잠시만 기다려주세요.")
    }

}