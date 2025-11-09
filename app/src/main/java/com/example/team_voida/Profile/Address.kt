package com.example.team_voida.Profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Notification.Notification
import com.example.team_voida.Payment.PaymentAddress
import com.example.team_voida.ProfileServer.AddAddress
import com.example.team_voida.ProfileServer.Address
import com.example.team_voida.ProfileServer.AddressList
import com.example.team_voida.ProfileServer.CardAdd
import com.example.team_voida.ProfileServer.CardInfo
import com.example.team_voida.ProfileServer.PayHistoryList
import com.example.team_voida.ProfileServer.PayHistoryListServer
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.IconBlue
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import com.example.team_voida.ui.theme.WishButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun Address(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
){
    val context = LocalContext.current

    Log.e("Address", "Recycle")

    val scrollState = rememberScrollState()

    val addressList: MutableState<List<Address>?> = remember { mutableStateOf<List<Address>?>(null) }
    val whichAddress: MutableState<Int> = remember { mutableStateOf(-1) }
    val addAddressText: MutableState<String> = remember { mutableStateOf("") }

    val customTextFieldDialogState: MutableState<AddressDialogState> = remember {
        mutableStateOf(
            AddressDialogState(
                text = "",
                isShowDialog = false,
            )
        )
    }


    // 유저 정보 페이지에 해당하는 하단 네비 Flag Bit 활성화
    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
        } else if (event == Lifecycle.Event.ON_STOP) {
        } else if (event == Lifecycle.Event.ON_DESTROY) {
        } else if (event == Lifecycle.Event.ON_CREATE) {
        } else if (event == Lifecycle.Event.ON_START) {
            basketFlag.value = false
            homeNavFlag.value = true
            productFlag.value = false

        } else if (event == Lifecycle.Event.ON_RESUME) {
        }
    }

    // 서버에 장바구니 정보 요청
    if(addressList.value == null){
        runBlocking {
            val job = GlobalScope.launch{
                addressList.value = AddressList(session.sessionId.value)
            }
        }
    }

    if(addressList.value != null) {

        if (customTextFieldDialogState.value.isShowDialog) {
            Log.e("Address", "Wow!")

            AddressDialog(
                introduction = "배송지 추가 팝업 입니다. 아래에 새로운 배송지를 입력해주세요.",
                address = addAddressText,
                onClickCancel = {customTextFieldDialogState.value = customTextFieldDialogState.value.copy(isShowDialog = false)},
                onClickConfirm = {

                    runBlocking {
                        val job = GlobalScope.launch{
                            addressList.value = AddAddress(
                                session_id = session.sessionId.value,
                                address = addAddressText.value
                            )
                        }
                    }

                    Thread.sleep(2000L)

                    if(addressList.value!![0].address_id != -1){
                        Toast.makeText(context, "새로운 주소가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                        customTextFieldDialogState.value = customTextFieldDialogState.value.copy(isShowDialog = false)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)

        ) {
            Notification("배송지 설정 화면입니다. 아래에 배송지를 수정하거나 대표 배송지를 설정할 수 있습니다. 배송지 추가를 원하시는 경우 '배송지 추가' 버튼을 눌러주세요.")

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

            Spacer(Modifier.height(15.dp))

            addressList.value!!.forEach {
                SettingAddress(
                    addressId = it.address_id,
                    address = it.address_text,
                    editable = true,
                    whichAddress = whichAddress,
                    flag = it.flag
                )

                Spacer(Modifier.height(10.dp))
            }

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
                    customTextFieldDialogState.value = customTextFieldDialogState.value.copy(isShowDialog = true)
                }
            ) {
                Text(
                    modifier = Modifier
                        .padding(
                        ),
                    textAlign = TextAlign.Center,
                    text = "배송지 추가",
                    color = TextWhite,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
            }
        }
    } else {
        LoaderSet(info = "배송지 로딩중", semantics = "배송지 로딩중")
    }
}


// 배송지 주소 컴포저블
@Composable
fun SettingAddress(
    addressId: Int,
    address: String,
    editable: Boolean,
    flag: Boolean,
    whichAddress: MutableState<Int>
){
    Column(
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                text = AnnotatedString("")
            }
            .fillMaxWidth()
            .height(120.dp)
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .clip(RoundedCornerShape(7.dp))
            .border(
                width = if (flag) {
                    2.dp
                } else {
                    0.dp
                },
                color = IconBlue,
                shape = RoundedCornerShape(7.dp)
            )
            .background(
                color = WishButton
            )
    ){
        Text(
            modifier = Modifier
                .padding(
                    start = 5.dp
                )
                .padding(
                    start = 13.dp,
                    top = 13.dp,
                    end = 13.dp
                )
            ,
            textAlign = TextAlign.Center,
            text = "배송 주소",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                modifier = Modifier
                    .padding(
                        start = 5.dp
                    )
                    .padding(13.dp)
                    .fillMaxWidth()
                    .weight(8f)
                ,
                text = address,
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                )
            )
            if(editable){
                Button(
                    onClick = {},
                    modifier = Modifier
                        .size(30.dp)
                        .width(1.dp)
                        .offset(
                            x = -10.dp,
                            y = 20.dp
                        )
                    ,
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.payment_edit),
                        contentDescription = "배송지 수정 버튼",
                        modifier = Modifier

                    )
                }
            }
        }
    }
}


data class AddressDialogState(
    var text: String? = null,
    var isShowDialog: Boolean = false,
    val onClickConfirm: (text: String) -> Unit = {},
    val onClickCancel: () -> Unit = {},
)

@Composable
fun AddressDialog(
    introduction: String,
    address: MutableState<String>,
    onClickCancel: () -> Unit,
    onClickConfirm: () -> Unit
) {


    Dialog(
        onDismissRequest = { onClickCancel() },
    ) {
        Card(
            shape = RoundedCornerShape(8.dp), // Card의 모든 꼭지점에 8.dp의 둥근 모서리 적용
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .wrapContentHeight()
                    .background(
                        color = Color.White,
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = introduction,
                    textAlign = TextAlign.Center,
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )

                Spacer(modifier = Modifier.height(25.dp))

                // TextField
                BasicTextField(
                    value = address.value,
                    onValueChange = { address.value = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Selected,
                                    shape = RoundedCornerShape(size = 16.dp)
                                )
                                .padding(all = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = "",
                                tint = Color.DarkGray,
                            )
                            Spacer(modifier = Modifier.width(width = 8.dp))
                            innerTextField()
                        }
                    },
                )

                Spacer(modifier = Modifier.height(25.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            onClickCancel()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonColors(
                            disabledContentColor = IconBlue,
                            disabledContainerColor = IconBlue,
                            contentColor = IconBlue,
                            containerColor = IconBlue
                        )
                    ) {
                        Text(
                            text = "취소",
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Button(
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                        onClickConfirm()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonColors(
                            disabledContentColor = IconBlue,
                            disabledContainerColor = IconBlue,
                            contentColor = IconBlue,
                            containerColor = IconBlue
                        )
                    ) {
                        Text(
                            text = "확인",
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                            )
                        )
                    }
                }
            }
        }
    }
}
