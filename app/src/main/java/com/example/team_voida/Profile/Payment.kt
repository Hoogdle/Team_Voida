package com.example.team_voida.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.Payment.PaymentMethodList
import com.example.team_voida.R
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.SkyBlue
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import kotlin.math.log

@Composable
fun PaymentSetting(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>
){

    val scrollState = rememberScrollState()
    val isAdding = remember { mutableStateOf(false) }

    // 유저 정보 페이지에 해당하는 하단 네비 Flag Bit 활성화
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

    ) {
        Notification("결제수단 설정 화면입니다. 현재 결제수단을 확인하시고, 다른 결제수단으로 변경하시거나 새로운 결제수단을 등록하세요.")

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

        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 10.dp
                ),
            textAlign = TextAlign.Center,
            text = "결제수단 변경",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
            )
        )

        Spacer(Modifier.height(15.dp))

        PaymentAddButton(
            isAdding = isAdding
        )

        if(isAdding.value){
            PaymentAdd(
                isAdding = isAdding
            )
        }

        Spacer(Modifier.height(15.dp))

        PaymentCard(
            company = "ibk",
            paymentNumber = "1111222233334444",
            name = "Travis",
            expiredMonth = "12",
            expiredDate = "10"
        )

    }
}

@Composable
fun PaymentAddButton(
    isAdding: MutableState<Boolean>
){
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
        onClick = {
            isAdding.value = !isAdding.value
        }
    ) {
        Text(
            modifier = Modifier
                .padding(

                ),
            textAlign = TextAlign.Center,
            text = "결제수단 추가",
            color = TextWhite,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            )
        )
    }
}


@Composable
fun PaymentRegisterPreButton(
    cardNum: String,
    expiredMonth: String,
    expiredDate: String,
    cvv: String,
    isCorrect: MutableState<Boolean>
){
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
            .padding(
            )
        ,
        onClick = {
            isCorrect.value = true
        }
    ) {
        Text(
            modifier = Modifier
                .padding(

                ),
            textAlign = TextAlign.Center,
            text = "입력완료",
            color = TextWhite,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            )
        )
    }
}

@Composable
fun PaymentRegisterLastButton(
    pw: String,
    lastCheck: MutableState<Boolean>,
    isAdding: MutableState<Boolean>
){
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
        onClick = {
            lastCheck.value = true
            isAdding.value = false
        }
    ) {
        Text(
            modifier = Modifier
                .padding(

                ),
            textAlign = TextAlign.Center,
            text = "카드등록",
            color = TextWhite,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular)),
            )
        )
    }
}

@Composable
fun PaymentAdd(
    isAdding: MutableState<Boolean>
){

    val cardNum: MutableState<String> = remember{mutableStateOf("")}
    val expiredMonth: MutableState<String> = remember{mutableStateOf("")}
    val expiredDate: MutableState<String> = remember{mutableStateOf("")}
    val cvv: MutableState<String> = remember{mutableStateOf("")}
    val pw: MutableState<String> = remember{mutableStateOf("")}

    val isCorrect = remember { mutableStateOf(false) }
    val lastCheck = remember { mutableStateOf(false) }


    val whichMethod = remember { mutableStateOf(-1) }
    val whichBank = remember { mutableStateOf(-1) }
    val tmpRegisteredPayMethod = remember { mutableListOf("신용카드", "모바일 페이") }
    val bankList = remember { mutableListOf("우리은행","신한은행","기업은행","국민은행","농협은행","새마을금고","하나은행","SC 제일은행","신협은행","부산은행","광주은행","대구은행",) }

    Notification("아래에 정보를 입력하여 결제수단을 등록해주세요.", top = 5.dp, bottom = 5.dp)

    Notification("'카드', '모바일페이' 중 원하는 결제 방법을 선택해주세요.", top = 5.dp, bottom = 5.dp)

    PaymentMethodList(
        tmpRegisteredPayMethod = tmpRegisteredPayMethod,
        whichMethod = whichMethod
    )

    if(whichMethod.value==0){
        Notification("'신용카드 등록'을 선택하셨습니다. 아래에 원하시는 '카드사'를 선택해주세요.", top = 5.dp, bottom = 5.dp)

        PaymentMethodList(
            tmpRegisteredPayMethod = bankList,
            whichMethod = whichBank
        )

        if(whichBank.value != -1){
            Notification("'${bankList[whichBank.value]}'을 선택하셨습니다. 아래에 '카드번호', '만기일', 'CVV'를 입력해주세요.", top = 5.dp, bottom = 5.dp)

            Box(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
            ){
                Column {

                    Spacer(Modifier.height(5.dp))

                    Text(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        text = "카드번호",
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontSize = 15.sp
                        )
                    )

                    Spacer(Modifier.height(10.dp))
                    AccountTextField("카드번호를 입력해주세요.", height = 50.dp,cardNum)
                    Spacer(Modifier.height(10.dp))

                    Text(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        text = "유효기간",
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontSize = 15.sp
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ){

                        PaymentSettingTextField("년", height = 50.dp,expiredMonth)
                        PaymentSettingTextField("월", height = 50.dp,expiredDate)

                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        text = "CVV",
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontSize = 15.sp
                        )
                    )

                    Spacer(Modifier.height(10.dp))
                    AccountTextField("CVV를 입력해주세요.", height = 50.dp,cvv)

                    Spacer(Modifier.height(10.dp))
                    PaymentRegisterPreButton(
                        cardNum = cardNum.value,
                        expiredMonth = expiredMonth.value,
                        expiredDate = expiredDate.value,
                        cvv = cvv.value,
                        isCorrect = isCorrect
                    )
                    if(isCorrect.value){
                        Notification("마지막 단계입니다. 카드 비밀번호 4자리를 입력해주세요.", top = 5.dp, bottom = 5.dp)
                        Spacer(Modifier.height(10.dp))
                        AccountTextField("비밀번호", height = 50.dp,pw)
                        Spacer(Modifier.height(10.dp))
                        PaymentRegisterLastButton(pw.value, lastCheck = lastCheck, isAdding)
                    }
                }
            }
        }

    }else if(whichMethod.value==1){
        Notification("'모바일페이 등록'을 선택하셨습니다. 아래에 '카카오페이', '토스' 중 원하는 모바일 페이를 선택해주세요.", top = 5.dp, bottom = 5.dp)
    }

}

@Composable
fun PaymentCard(
    company: String,
    paymentNumber: String,
    name: String,
    expiredMonth: String,
    expiredDate: String
){
    val logo = PaymentLogoSelector(company)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(
                horizontal = 15.dp
            )
            .clip(
                shape = RoundedCornerShape(15.dp)
            )
            .background(color= com.example.team_voida.ui.theme.PaymentCard)

    ){
        Column {

            // Logo and Setting
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Image(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        )
                    ,
                    painter = painterResource(logo),
                    contentDescription = ""
                )
                Button(
                    onClick = {},
                    modifier = Modifier
                        .padding(
                            all = 10.dp
                        )
                        .size(50.dp)
                        .padding(
                            all = 5.dp
                        )
                    ,
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ){
                    Image(
                        painter = painterResource(R.drawable.cogwheel),
                        contentDescription = "프로필 이미지 수정 버튼",
                        modifier = Modifier
                            .clip(shape = CircleShape)
                            .background(
                                color = Selected
                            )
                            .padding(all = 10.dp)
                    )
                }

            }

            Spacer(Modifier.height(40.dp))

            // Card Number
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ){
                val lastNumber = paymentNumber.substring(12,16)

                for(i in 1..3){
                    Text(
                        text = "* * * *",
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontSize = 20.sp
                        )
                    )
                }

                Text(
                    text = lastNumber,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // Name and Expired
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    modifier = Modifier
                        .padding(horizontal = 23.dp),
                    text = name,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 15.sp
                    )
                )

                Text(
                    modifier = Modifier
                        .padding(horizontal = 23.dp),
                    text = expiredMonth + "/" + expiredDate,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

fun PaymentLogoSelector(
    company: String
):Int{
    var result = 0
    when(company){
        "bnk" -> result = R.drawable.bnk
        "city" -> result = R.drawable.city
        "dgb" -> result = R.drawable.dgb
        "gj" -> result = R.drawable.gj
        "hana" -> result = R.drawable.hana
        "ibk" -> result = R.drawable.ibk
        "kb" -> result = R.drawable.kb
        "mg" -> result = R.drawable.mg
        "nh" -> result = R.drawable.nh
        "sc" -> result = R.drawable.sc
        "sh" -> result = R.drawable.sh
        "shinhup" -> result = R.drawable.shinhup
        "sinhan" -> result = R.drawable.sinhan
        "woorie" -> result = R.drawable.woorie
    }
    return result
}


// 유저 정보 입력란 컴포저블
// 베이직 텍스트 필드 커스텀 마이즈
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSettingTextField(
    placeholder: String,
    height: Dp = 70.dp,
    input: MutableState<String> = mutableStateOf(""),
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
            .height(height)

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