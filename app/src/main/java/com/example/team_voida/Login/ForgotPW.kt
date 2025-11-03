package com.example.team_voida.Login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.ui.theme.TextLittleDark

@Composable
fun ForgotPw(
    navController: NavController
){
    val email = remember{ mutableStateOf("") }
    val cell = remember{ mutableStateOf("") }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){
        Notification("비밀번호 재설정 페이지입니다. 아래에 이메일과 전화번호를 입력해주세요.")
        Spacer(Modifier.height(25.dp))
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Forgot Password",
            style = TextStyle(
                fontSize = 55.sp,
                fontFamily = FontFamily(Font(R.font.roboto_bold)),
            )
        )
        Spacer(Modifier.height(135.dp))
        LoginTextField(email,"이메일")
        Spacer(Modifier.height(15.dp))
        LoginTextField(cell,"전화번호")

        Spacer(Modifier.height(165.dp))
        FindPwButton(email, cell, navController)
        Spacer(Modifier.height(15.dp))

        Text(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .clickable {navController.navigate("login")},
            text = "취소",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextLittleDark,
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}