package com.example.team_voida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.team_voida.CreateAccount.CreateAccount
import com.example.team_voida.Home.Home
import com.example.team_voida.Nav.HomeNav
import com.example.team_voida.Nav.StartNav
import com.example.team_voida.SearchResult.SearchResult
import com.example.team_voida.Start.Start
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.Team_VoidaTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// session 관리
object session{
    var sessionId = mutableStateOf("")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            StartNav()
           // HomeNav()
        }
    }
}

