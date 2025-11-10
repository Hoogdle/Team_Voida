package com.example.team_voida

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.team_voida.CreateAccount.CreateAccount
import com.example.team_voida.Home.Home
import com.example.team_voida.Nav.HomeNav
import com.example.team_voida.Nav.StartNav
import com.example.team_voida.Profile.defaultColor
import com.example.team_voida.Profile.themeGreenYellow
import com.example.team_voida.Profile.themeNavyPink
import com.example.team_voida.Profile.themeRedSkyBlue
import com.example.team_voida.SearchResult.SearchResult
import com.example.team_voida.Start.Start
import com.example.team_voida.Tools.DataStoreManager
import com.example.team_voida.Tools.MainViewModel
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.Team_VoidaTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

//// it will be replaced with DataStore code
// session 관리
object session{
    var sessionId = mutableStateOf("")
}

//// it will be replaced with DataStore code

// session 관리
object themeInStart{
    val themeId = mutableStateOf(0)
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val dataStoreManager = DataStoreManager(applicationContext)
        val viewModel = MainViewModel(dataStoreManager)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Log.e("Main","Recycle")


            val navController = rememberNavController()

            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val sessionInDb by viewModel.userToken.collectAsState()
            val themeCode by viewModel.themeMode.collectAsState()

            Log.e("Main",isLoggedIn.toString())
            Log.e("Main",sessionInDb.toString())
            Log.e("Main",themeCode.toString())

            themeInStart.themeId.value = themeCode!!

            if(themeCode != -1) {
                when(themeInStart.themeId.value){
                    0 -> defaultColor()
                    1 -> themeNavyPink()
                    2 -> themeGreenYellow()
                    3 -> themeRedSkyBlue()
                }
                if (isLoggedIn) {
                    session.sessionId.value = sessionInDb.toString()
                    HomeNav(
                        viewModel
                    )
                } else {
                    StartNav(
                        viewModel
                    )
                }
            }
        }
    }
}

