package com.example.team_voida.Nav

import android.util.Log
import android.view.KeyEvent
import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.team_voida.CreateAccount.CreateAccount
import com.example.team_voida.CreateAccount.CreateAccountNaming
import com.example.team_voida.Home.Home
import com.example.team_voida.Login.Login
import com.example.team_voida.Start.Guide
import com.example.team_voida.Start.Start

// 시작화면에서 사용되는 Navigation
@Composable
fun StartNav(){

    val navController = rememberNavController()


    var input = remember { mutableStateOf("") }

    val context = LocalContext.current
    val view = LocalView.current

    // 각각 회원가입 또는 로그인에서 사용되는 입력 정보 저장
    val email = remember { mutableStateOf("") }
    val pw = remember { mutableStateOf("") }
    val rePw = remember { mutableStateOf("") }
    val cell = remember { mutableStateOf("") }

    // check git hub


    val upPressed = remember { mutableStateOf(false) }
    val downPressed = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = ViewCompat.OnUnhandledKeyEventListenerCompat { _, event ->
            val keyCode = event.keyCode
            val action = event.action

            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        upPressed.value = true
                    } else if (action == KeyEvent.ACTION_UP) {
                        upPressed.value = false
                    }
                }

                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        downPressed.value = true
                    } else if (action == KeyEvent.ACTION_UP) {
                        downPressed.value = false
                    }
                }
            }

            if (upPressed.value && downPressed.value) {
                Log.e("hi","hello")
                true  // 이벤트 소비
            } else {
                false  // 다른 이벤트 처리 허용
            }
        }

        ViewCompat.addOnUnhandledKeyEventListener(view, listener)

        onDispose {
            ViewCompat.removeOnUnhandledKeyEventListener(view, listener)
        }
    }

    // StartNav와 관련된 페이지, (로그인,회원가입, 안내...) 에 대한 네비 등록
    NavHost(navController = navController, startDestination = "start") {
        composable("start") { Start(navController = navController) }
        composable("createAccount") {
            CreateAccount(
                email = email,
                pw  = pw,
                rePw = rePw,
                cell = cell,
                navController = navController
            )
        }
        composable("login"){ Login(navController = navController) }
        composable("naming") { CreateAccountNaming(
            email = email.value,
            pw = pw.value,
            cell = cell.value,
            navController = navController
        )
        }
        composable("guide") { Guide(navController = navController) }
        composable("home") { HomeNav() }
    }
}

