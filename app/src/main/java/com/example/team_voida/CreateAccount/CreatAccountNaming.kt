package com.example.team_voida.CreateAccount

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.session
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.LoginTextFiled
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun CreateAccountNaming(
    email: String,
    pw: String,
    cell: String,
    navController: NavController,
    address: MutableState<String>
){
    val un = remember{ mutableStateOf("") }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Notification(
            text = "유저 이름을 설정하는 페이지입니다. 화면 중앙부에 위치한 입력란에 유저 이름을 입력해주세요."
        )
        Spacer(Modifier.height(75.dp))
        Image(
            modifier = Modifier
                .size(150.dp),
            painter = painterResource(R.drawable.sample_profile),
            contentDescription = "화면 중앙부에 입력란에 유저 이름을 입력해주세요!"
        )
        Spacer(Modifier.height(35.dp))
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "사용자 이름 입력",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 25.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
        Spacer(Modifier.height(15.dp))
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "계정 생성을 위해 아래에 사용자 이름을 입력 해주세요.",
            color = Color.Black,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            )
        )
        Spacer(Modifier.height(35.dp))
        UserNameTextField(un,"사용자 이름 입력")
        Spacer(Modifier.height(135.dp))
        UserNameButton(
            email,
            pw,
            cell,
            un,
            navController,
            address
        )
        Spacer(Modifier.height(10.dp))
        Text(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .clickable {navController.navigate("createAccount")},
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNameTextField(
    input: MutableState<String>,
    placeholder: String
){
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
            .clip(RoundedCornerShape(15.dp))
            .background(
                color = LoginTextFiled
            )
            .height(60.dp)

        ,
        singleLine = true,
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            color = TextColor,
            fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            fontSize = 14.sp
        ),

        decorationBox = @Composable{ innerTextField ->
            TextFieldDefaults.DecorationBox(
                placeholder = {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center,
                            text = placeholder,
                            color = Color.LightGray,
                            style = TextStyle(
                                color = TextColor,
                                fontFamily = FontFamily(Font(R.font.roboto_bold)),
                                fontSize = 14.sp
                            ),
                        )
                    }
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
                    unfocusedContainerColor = LoginTextFiled,
                    focusedContainerColor = LoginTextFiled,
                    errorContainerColor = LoginTextFiled,
                    disabledContainerColor = LoginTextFiled
                ),
            )
        }
    )
}

@Composable
fun UserNameButton(
    email: String,
    pw: String,
    cell: String,
    un: MutableState<String>,
    navController: NavController,
    address: MutableState<String>
){
    val context = LocalContext.current

    Button(
        shape = RectangleShape,
        modifier = Modifier
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .fillMaxWidth()
            .height(65.dp)
            .clip(shape = RoundedCornerShape(15.dp))
        ,
        onClick = {

            var check: Boolean? = false
            Log.e("zzz",email)
            Log.e("zzz",pw)
            Log.e("zzz",cell)
            Log.e("zzz",un.value)

            runBlocking {
                val job = GlobalScope.launch {
                    check = CreateAccountServer(
                        email = email,
                        pw = pw,
                        cell = cell,
                        un = un.value,
                        address = address.value
                    )
                }
            }
            check?.let { Log.e("xxx", it.toString()) }

            Thread.sleep(3500L)

            if(check == true){
                navController.navigate("guide")
            } else{
                Toast.makeText(context, "이미 존재하는 닉네임 입니다.", Toast.LENGTH_SHORT).show()
            }
        },
        colors = ButtonColors(
            containerColor = ButtonBlue,
            contentColor = TextWhite,
            disabledContentColor = TextWhite,
            disabledContainerColor = ButtonBlue
        )
    ) {
        Text(
            text = "완료",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextWhite,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}