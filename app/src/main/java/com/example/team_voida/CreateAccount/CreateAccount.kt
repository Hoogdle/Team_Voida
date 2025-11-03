package com.example.team_voida.CreateAccount

import android.graphics.Paint.Align
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.ui.theme.LoginTextFiled
import com.example.team_voida.ui.theme.TextColor
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import com.example.team_voida.Home.HomePopularCall
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 계정 생성 메인 컴포저블
@Composable
fun CreateAccount(
    email: MutableState<String>,
    pw: MutableState<String>,
    rePw: MutableState<String>,
    cell: MutableState<String>,
    navController: NavController,
    address: MutableState<String>,
    pwVisibility: MutableState<Boolean>,
    rePwVisibility: MutableState<Boolean>,
    ){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){

        // 안내바
        Notification(
            text = "계정생성 페이지입니다. 아래에 차례로 이메일, 비밀번호, 비밀번호 재입력, 전화번호, 주소를 입력한 후, 하단의 '계정생성' 버튼을 눌러주세요."
        )
        Spacer(Modifier.height(25.dp))
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = "Create\nAccount",
            style = TextStyle(
                fontSize = 55.sp,
                fontFamily = FontFamily(Font(R.font.roboto_bold)),
            )
        )
        
        // 입력란 리스트
        Spacer(Modifier.height(45.dp))
        CreateAccountTextField(email,"이메일 주소")
        Spacer(Modifier.height(10.dp))
        CreateAccountPassWordField(pw,"비밀번호 입력",pwVisibility)
        Spacer(Modifier.height(10.dp))
        CreateAccountPassWordField(rePw,"비밀번호 재입력",rePwVisibility)
        Spacer(Modifier.height(10.dp))
        CreateAccountTextField(address,"주소 입력")
        Spacer(Modifier.height(10.dp))
        CreateAccountContactField(cell,"전화번호 입력")
        Spacer(Modifier.height(85.dp))
        CreateAccountButton(
            email = email,
            pw = pw,
            rePw = rePw,
             cell = cell,
            navController
        )
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

// 계정 생성에 사용되는 텍스트 필드 커스텀마이즈 컴포저블
// BasicTextFiled를 활용하여 커스텀마이즈
// Todo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountTextField(
    email: MutableState<String>,
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
        value = email.value,
        onValueChange = {
            email.value = it
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
                value = email.value.toString(),
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

// 위와 동일한 입력란
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountPassWordField(
    pw: MutableState<String>,
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
            value = pw.value,
            onValueChange = {
                pw.value = it
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
            visualTransformation = if (passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

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
                    value = pw.value.toString(),
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
                .size(20.dp)
                .offset(
                    x = 330.dp,
                    y = 20.dp
                )
                .clickable { passwordVisibility.value = !passwordVisibility.value }
            ,
            painter = painterResource(R.drawable.eye_password),
            contentDescription = "비밀번호 가리기 버튼"
        )
    }
}


// 위와 동일한 입력란
// Todo, match the inputfiled of this box
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountContactField(
    input : MutableState<String>,
    placeholder: String
){
//    val spacedInput = remember { mutableStateOf("          "+input) }
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

            decorationBox = @Composable{ innerTextField ->
                TextFieldDefaults.DecorationBox(
                    contentPadding = PaddingValues(
                        horizontal = 100.dp
                    ),
                    placeholder = {
                        Row {
                            Text(
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
        Image(
            modifier = Modifier
                .size(60.dp)
                .offset(
                    x = 30.dp,
                    y = 0.dp
                ),
            painter = painterResource(R.drawable.flag),
            contentDescription = "비밀번호 가리기 버튼"
        )
    }
}

// 계정생성 버튼 컴포저블
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun CreateAccountButton(
    email: MutableState<String>,
    pw: MutableState<String>,
    rePw: MutableState<String>,
    cell: MutableState<String>,
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
            Log.e("pw",pw.toString())
            if(pw.value != rePw.value){
                Toast.makeText(context, "두 비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show()
            } else {
                var tmp: Boolean? = false

                runBlocking {
                    val job = GlobalScope.launch {
                        tmp = CheckEmail(email.value)
                    }
                }
                Thread.sleep(1500L)

                if(tmp == true){
                    navController.navigate("naming")
                }
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
            text = "계정 생성",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextWhite,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}