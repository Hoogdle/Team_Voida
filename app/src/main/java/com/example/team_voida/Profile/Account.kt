package com.example.team_voida.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.ui.theme.ButtonBlackColor
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.DisabledText
import com.example.team_voida.ui.theme.LoginTextFiled
import com.example.team_voida.ui.theme.SearchBarColor
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.SkyBlue
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite

@Composable
fun Account(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>
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
        Notification("사용자 정보 변경 화면입니다. 프로필 이미지, 유저 이름, 이메일 주소를 확인할 수 있습니다. 비밀번호 변경을 원하시면 아래의 입력란에 변경된 비밀번호를 입력해주세요.")

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

        Spacer(Modifier.height(5.dp))

        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 10.dp
                ),
            textAlign = TextAlign.Center,
            text = "프로필 정보 변경",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
            )
        )


        Spacer(Modifier.height(15.dp))

        AccountProfileImg()

        Spacer(Modifier.height(25.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SearchBarColor)
                .padding(
                    start = 25.dp,
                    top = 25.dp,
                    bottom = 25.dp
                )
            ,
            text = "후그들형님",
            color = DisabledText,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
            )
        )

        Text(
            modifier = Modifier
                .offset(
                    y = -6.dp
                )
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SearchBarColor)
                .padding(
                    start = 25.dp,
                    top = 25.dp,
                    bottom = 25.dp
                )
            ,
            text = "12345@gmail.com",
            color = DisabledText,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
            )
        )
        AccountTextField("123456")

        Spacer(Modifier.height(80.dp))
        Button(

            shape = RoundedCornerShape(10.dp),
            colors = ButtonColors(
                contentColor = ButtonBlue,
                containerColor = ButtonBlue,
                disabledContentColor = ButtonBlue,
                disabledContainerColor = ButtonBlue
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(10.dp)
            ,
            onClick = {}
        ) {
            Text(
                modifier = Modifier
                    .padding(
                        
                    ),
                textAlign = TextAlign.Center,
                text = "변경사항 저장",
                color = TextWhite,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                )
            )
        }
    }
}

@Composable
fun AccountProfileImg(){
    Box(
        modifier = Modifier
            .offset(
                x = 10.dp
            )
    ){
        Image(
            painter = painterResource(R.drawable.account_avatar),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(86.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)

                .border(
                    width = 5.dp,
                    color = Color.White,
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .offset(
                    x = 60.dp,
                    y = 11.dp
                )
        ){

            Image(
                painter = painterResource(R.drawable.payment_edit),
                contentDescription = "프로필 이미지 수정 버튼",
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .background(
                        color = Selected
                    )
                    .width(30.dp)
                    .height(30.dp)
                    .border(
                        width = 3.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable {  }

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTextField(
    placeholder: String
){
    val input = remember{ mutableStateOf("") }
    val interactionSource = remember{ MutableInteractionSource() }

    BasicTextField(
        // 필터 입력 후 Action에 대해 정의
        keyboardActions = KeyboardActions(
            // 예시)
            // 백엔드로 필터링 된 데이터 요청 함수
            // callFilter2Backend(input)

            // 선택된 페이지에 관한 데이터는 이 함수의 input 변수를 활용
            onDone = {}
        ),
        value = input.value,
        onValueChange = {
            input.value = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = SkyBlue
            )
            .padding(
                start = 10.dp
            )
            .height(70.dp)

        ,
        singleLine = true,
        textStyle = TextStyle(
            color = TextColor,
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontSize = 15.sp
        ),

        decorationBox = @Composable{ innerTextField ->
            TextFieldDefaults.DecorationBox(
                placeholder = {
                    Text(
                        text = placeholder,
                        color = TextColor,
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontSize = 15.sp
                        ),
                    )
                },
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                enabled = true,
                innerTextField = innerTextField,
                value = input.value.toString(),
                interactionSource = interactionSource,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextColor,
                    unfocusedTextColor = TextColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = TextColor,
                    unfocusedContainerColor = SkyBlue,
                    focusedContainerColor = SkyBlue,
                    errorContainerColor = SkyBlue,
                    disabledContainerColor = SkyBlue
                ),
            )
        }
    )
}