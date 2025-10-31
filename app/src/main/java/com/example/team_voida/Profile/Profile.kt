package com.example.team_voida.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.ui.theme.SearchBarColor
import com.example.team_voida.ui.theme.TextLittleDark

@Composable
fun Profile(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>

){

    val scrollState = rememberScrollState()

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
            basketFlag.value = false
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value = 4
        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)

    ){
        Notification("계정 설정 화면입니다. 아래의 목록에서 변경하고 싶은 계정 정보를 선택해주세요.")

        Spacer(Modifier.height(10.dp))
        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 23.dp
                ),
            textAlign = TextAlign.Center,
            text = "Settings",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 25.sp,
                fontFamily = FontFamily(Font(R.font.roboto_bold)),
            )
        )

        Spacer(Modifier.height(20.dp))
        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 23.dp
                ),
            textAlign = TextAlign.Center,
            text = "Personal",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.roboto_extrabold)),
            )
        )

        ProfileButton("프로필",{navController.navigate("account")})
        HorizontalDivider(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    end = 10.dp
                ),
            color = SearchBarColor
        )
        ProfileButton("배송지",{})
        HorizontalDivider(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    end = 10.dp
                ),
            color = SearchBarColor
        )
        ProfileButton("결제내역",{navController.navigate("paymentHistory")})
        HorizontalDivider(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    end = 10.dp
                ),
            color = SearchBarColor
        )
        
        ProfileButton("결제수단",{navController.navigate("paymentSetting")})
        HorizontalDivider(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    end = 10.dp
                ),
            color = SearchBarColor
        )
    }
}

@Composable
fun ProfileButton(
    contents: String,
    onClickAction: () -> Unit
){
    Button(
        shape = RectangleShape,
        colors = ButtonColors(
            contentColor = Color.Transparent,
            containerColor = Color.Transparent,
            disabledContentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        onClick = {
            onClickAction()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp,
                    end = 10.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                modifier = Modifier
                    .padding(
                        top = 15.dp,
                        bottom = 15.dp
                    ),
                text = contents,
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                )
            )

            Image(
                painter = painterResource(R.drawable.profile_arrow),
                contentDescription = contents + "버튼",
                modifier = Modifier
                    .size(15.dp)
                    .offset(
                        y = 17.dp
                    )
            )
        }

    }
}