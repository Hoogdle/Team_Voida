package com.example.team_voida.Login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.team_voida.CreateAccount.CheckEmail
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.Tools.MainViewModel
import com.example.team_voida.session
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.LoginTextFiled
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


// 로그인 메인 컴포저블
@Composable
fun Login(
    navController: NavController,
){
    val email = remember{ mutableStateOf("") }
    val pw = remember{ mutableStateOf("") }
    val passwordVisibility = remember { mutableStateOf(false) }
    
    val rememberPage = remember { mutableStateOf(false) }

    val view = LocalView.current

    if(rememberPage.value == false){
        view.announceForAccessibility("로그인 화면입니다. 화면 최상단에서 안내메세지를 제공받으세요.")
        rememberPage.value = true
    }


    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){
        Notification("로그인 페이지입니다. 이메일 또는 전화번호를 입력한 후, 비밀번호를 입력하여 로그인 해주세요.")
        Spacer(Modifier.height(25.dp))
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Login",
            style = TextStyle(
                fontSize = 55.sp,
                fontFamily = FontFamily(Font(R.font.roboto_bold)),
            )
        )
        Spacer(Modifier.height(165.dp))
        LoginTextField(email,"이메일 또는 전화번호")
        Spacer(Modifier.height(15.dp))
        LoginPassWordField(pw,"비밀번호", passwordVisibility)
        Spacer(Modifier.height(10.dp))
        LoginForgotPW(navController)
        Spacer(Modifier.height(165.dp))
        LogIntButton(email, pw, navController)
        Spacer(Modifier.height(15.dp))
        Text(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .clickable {navController.navigate("start")},
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

// 로그인 입력란 컴포저블
// BasicTextField를 커스텀하여 제작
// Todo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
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
            color = TextColor,
            fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            fontSize = 14.sp
        ),

        decorationBox = @Composable{ innerTextField ->
            TextFieldDefaults.DecorationBox(
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.LightGray,
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_bold)),
                            fontSize = 14.sp
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
                    unfocusedContainerColor = LoginTextFiled,
                    focusedContainerColor = LoginTextFiled,
                    errorContainerColor = LoginTextFiled,
                    disabledContainerColor = LoginTextFiled
                ),
            )
        }
    )
}

// 위와 동일
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPassWordField(
    input: MutableState<String>,
    placeholder: String,
    passwordVisibility: MutableState<Boolean>
){
    val interactionSource = remember{ MutableInteractionSource() }

    Box{

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
                color = TextColor,
                fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                fontSize = 14.sp
            ),

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation(),
            decorationBox = @Composable{ innerTextField ->
                TextFieldDefaults.DecorationBox(
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = Color.LightGray,
                            style = TextStyle(
                                color = TextColor,
                                fontFamily = FontFamily(Font(R.font.roboto_bold)),
                                fontSize = 14.sp
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
                        unfocusedContainerColor = LoginTextFiled,
                        focusedContainerColor = LoginTextFiled,
                        errorContainerColor = LoginTextFiled,
                        disabledContainerColor = LoginTextFiled
                    ),
                )
            }
        )
        Icon(
            modifier = Modifier
                .clickable {
                    passwordVisibility.value != passwordVisibility.value
                }
                .size(20.dp)
                .offset(
                    x = 330.dp,
                    y = 20.dp
                ),
            painter = painterResource(R.drawable.eye_password),
            contentDescription = "비밀번호 가리기 버튼"
        )
    }
}

@Composable
fun LoginForgotPW(
    navController: NavController
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .clickable {navController.navigate("forgotpw1")},
            text = "비밀번호를 잊어버렸나요?",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextLittleDark,
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}

// 로그인 버튼 컴포저블
@Composable
fun LogIntButton(
    email: MutableState<String>,
    pw: MutableState<String>,
    navController: NavController,
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
            var result: String? = null

            runBlocking {
                val job = GlobalScope.launch {
                    result = LoginServer(
                        email = email.value,
                        pw = pw.value
                    )
                }
            }

            Thread.sleep(2000L)

            if(result != null){
                session.sessionId.value = result as String
                Log.e("www",session.sessionId.value)
                navController.navigate("guide")
            } else{
                Toast.makeText(context, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
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
            text = "로그인",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextWhite,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}


// 로그인 버튼 컴포저블
@Composable
fun FindPwButton(
    email: MutableState<String>,
    cell: MutableState<String>,
    userId: MutableState<Int>,
    navController: NavController
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
            
            lateinit var result: ResetPW1Response

            runBlocking {
                val job = GlobalScope.launch {
                    result = ResetPW1(
                        email = email.value,
                        cell = cell.value
                    )
                }
            }
            Thread.sleep(2000L)
            if(result.is_user){
                userId.value = result.user_id
                navController.navigate("pwReset")
            } else{
                Toast.makeText(context, "이메일과 전화번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
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
            text = "비밀번호 찾기",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextWhite,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}


// 로그인 버튼 컴포저블
@Composable
fun ResetPwButton(
    userId: MutableState<Int>,
    pw: MutableState<String>,
    rePw: MutableState<String>,
    navController: NavController
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
            var result: Boolean = false

            runBlocking {
                val job = GlobalScope.launch {
                    result = ResetPW2(
                        userId = userId.value,
                        pw = pw.value
                    )
                }
            }
            Thread.sleep(2000L)
            if(result == true){
                Toast.makeText(context, "로그인 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
                navController.navigate("login")
            } else{
                Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
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
            text = "비밀번호 설정",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextWhite,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}