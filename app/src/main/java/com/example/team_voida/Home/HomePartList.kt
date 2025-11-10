package com.example.team_voida.Home

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.CreateAccount.CheckEmail
import com.example.team_voida.Notification.Notification
import com.example.team_voida.SearchResult.SearchProducts
import com.example.team_voida.ui.theme.BackGroundWhite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun HomePartList(
    navController: NavController,
    input: MutableState<String>,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    isWhichPart: MutableState<Int>,
    barPrice: MutableState<Float>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
){

    var title: String = ""
    when(isWhichPart.value){
        1 -> {
            title = "실시간 인기"
        }
        2 -> {
            title = "많이 담는 특가"
        }
        3 -> {
            title = "하루 특가"
        }
        4 -> {
            title = "인기 신상품"
        }

    }
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

    var result: List<Popular>? = null

    runBlocking {
        val job = GlobalScope.launch {
            when(isWhichPart.value){
                1 -> {
                    result = HomePopularList()
                    isItemWhichPart.value = 1
                }
                2 ->{
                    result = HomeBigSaleList()
                    isItemWhichPart.value = 2
                }
                3 -> {
                    result = HomeTodaySaleList()
                    isItemWhichPart.value = 3
                }
                4 -> {
                    result = HomeNewList()
                    isItemWhichPart.value = 4
                }
            }
        }
    }

    Thread.sleep(1000L)

    val scrollState = rememberScrollState()
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
            result,
            navController,
            barPrice,
            productID = productID,
            isItemWhichPart = isItemWhichPart
        )
        Spacer(Modifier.height(30.dp))
    }
}