package com.example.team_voida.Profile

import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.team_voida.Basket.ComposableLifecycle

@Composable
fun Address(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    orderNumber: MutableState<String>
){

    val scrollState = rememberScrollState()


    // 유저 정보 페이지에 해당하는 하단 네비 Flag Bit 활성화
    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            Log.e("123", "on_pause")
        } else if (event == Lifecycle.Event.ON_STOP) {
            Log.e("123", "on_stop")
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            Log.e("123", "on_destroy")
        } else if (event == Lifecycle.Event.ON_CREATE) {
            Log.e("123", "on_create")
        } else if (event == Lifecycle.Event.ON_START) {
            Log.e("123", "on_start")
            basketFlag.value = false
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value = 4
        } else if (event == Lifecycle.Event.ON_RESUME) {
            Log.e("123", "on_resume")
        }
    }
}