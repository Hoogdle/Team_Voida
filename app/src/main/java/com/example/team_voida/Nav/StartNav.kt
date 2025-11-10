package com.example.team_voida.Nav

import AssistantSelector
import AssistantToServer
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.util.Log
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.team_voida.Basket.BasketListServer
import com.example.team_voida.CreateAccount.CreateAccount
import com.example.team_voida.CreateAccount.CreateAccountNaming
import com.example.team_voida.Home.Home
import com.example.team_voida.Login.ForgotPw
import com.example.team_voida.Login.ForgotPw2
import com.example.team_voida.Login.Login
import com.example.team_voida.Start.Guide
import com.example.team_voida.Start.Start
import com.example.team_voida.Tools.MainViewModel
import com.example.team_voida.session
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

// 시작화면에서 사용되는 Navigation
@Composable
fun StartNav(
    viewModel: MainViewModel
){

    val navController = rememberNavController()


    var input = remember { mutableStateOf("") }




    // 각각 회원가입 또는 로그인에서 사용되는 입력 정보 저장
    val email = remember { mutableStateOf("") }
    val pw = remember { mutableStateOf("") }
    val rePw = remember { mutableStateOf("") }

    val userId = remember { mutableStateOf(-1) }

    val pwVisibility = remember { mutableStateOf(false) }
    val rePwVisibility = remember { mutableStateOf(false) }

    val cell = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }

    // check git hub
    // StartNav와 관련된 페이지, (로그인,회원가입, 안내...) 에 대한 네비 등록
    NavHost(navController = navController, startDestination = "start") {
        composable("start") { Start(navController = navController) }
        composable("createAccount") {
            CreateAccount(
                email = email,
                pw  = pw,
                rePw = rePw,
                cell = cell,
                navController = navController,
                address = address,
                pwVisibility = pwVisibility,
                rePwVisibility = rePwVisibility
            )
        }
        composable("login"){ Login(navController = navController) }
        composable("forgotpw1"){ ForgotPw(userId = userId,navController = navController) }
        composable("pwReset"){ ForgotPw2(userId = userId, navController = navController) }

        composable("naming") { CreateAccountNaming(
            email = email.value,
            pw = pw.value,
            cell = cell.value,
            navController = navController,
            address = address
        )
        }
        composable("guide") { Guide(navController = navController, viewModel) }
        composable("home") { HomeNav(viewModel) }
    }
}

