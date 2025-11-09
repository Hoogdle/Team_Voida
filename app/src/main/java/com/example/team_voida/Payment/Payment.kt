package com.example.team_voida.Payment

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.team_voida.Basket.BasketCartNum
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Basket.BasketItemArrange
import com.example.team_voida.Basket.BasketProduct
import com.example.team_voida.Basket.ComposableLifecycle
import com.example.team_voida.Basket.basketSample
import com.example.team_voida.Categories.cateSports
import com.example.team_voida.Notification.Notification
import com.example.team_voida.ProductInfo.CancelAI
import com.example.team_voida.ProductInfo.Loader
import com.example.team_voida.Profile.CardDeleteDialog
import com.example.team_voida.Profile.CustomAlertDialogState
import com.example.team_voida.Profile.PaymentAdd
import com.example.team_voida.Profile.PaymentLogoSelector
import com.example.team_voida.ProfileServer.CardInfo
import com.example.team_voida.ProfileServer.PayDetailHistory
import com.example.team_voida.ProfileServer.PaymentDetailInfo
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.IconBlue
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.TextColor
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.Unselected
import com.example.team_voida.ui.theme.WishButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


// 결제 메인 컴포저블
@Composable
fun Payment(
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>,
    isPayOne: MutableState<Boolean>,
    isPayPage: MutableState<Boolean>,
    paymentUserInfo: MutableState<PaymentUserInfo>,
    dynamicTotalPrice: MutableState<String>,
    cardID: MutableState<Int>
){
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {

        // Component를 벗어날 때 수행
        onDispose {
            isPayPage.value = false
        }
    }

    isPayPage.value = true
    // 결제 수단 리스트
    val tmpRegisteredPayMethod = remember { mutableListOf("신용카드", "모바일 페이", "계좌이체") }

    // 결제 화면 하단 네비 Flag bit 설정 
    val paymentInfo:MutableState<PaymentPageInfo?> = remember { mutableStateOf<PaymentPageInfo?>(null) }
    val selectedCardId = cardID
    val whichAddress:MutableState<Int> = remember { mutableStateOf(-1) }

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
            basketFlag.value = true
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value = 3
        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }

    // 서버로 부터 해당 계정의 결제정보를 요청
    runBlocking {
        val job = GlobalScope.launch {
            if(isPayOne.value){
                paymentInfo.value = PaymentServerOne(
                    action = when (isItemWhichPart.value) {
                        1 -> ""
                        2 -> ""
                        3 -> ""
                        4 -> ""
                        else -> ""
                    },
                    session_id = session.sessionId.value,
                    product_id = productID.value

                )
            } else {
                paymentInfo.value = PaymentServerMultiple(
                    session_id = session.sessionId.value
                )

                if(!paymentInfo.value!!.cards.isEmpty()){
                    selectedCardId.value = paymentInfo.value!!.cards[0].card_id
                }

                Log.e("BasketPay",paymentInfo.value.toString())
            }
        }
    }




    Log.e("payment",productID.value.toString())
    // 결제 정보를 받은 경우 결제 페이지 정보 제공
    if(paymentInfo.value != null){

        var price = 0F

        paymentInfo.value!!.item.forEach {
            price += it.price
        }
        dynamicTotalPrice.value = price.toString()


        paymentInfo.value!!.address.forEach {
            if(it.flag){
                paymentUserInfo.value.address = it.address_text
            }
        }
        paymentUserInfo.value.cell = paymentInfo.value!!.phone
        paymentUserInfo.value.email = paymentInfo.value!!.email

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)

        ){
            Notification("결제 화면입니다. 아래에 설정된 배송지, 연락처를 확인해주세요. 결제되는 상품들을 확인하시고 화면 우측 하단에 '결제하기' 버튼을 눌러주세요..")

            // Payment
            Text(
                modifier = Modifier
                    .padding(
                        start = 22.dp
                    )
                    .semantics(mergeDescendants = true){
                        text = AnnotatedString("아래에 배송주소와 연락처를 확인해주세요.")
                    }
                ,
                textAlign = TextAlign.Center,
                text = "Payment",
                color = TextLittleDark,
                style = TextStyle(
                    fontSize = 25.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                )
            )

            Spacer(Modifier.height(15.dp))

            PaymentAddressList(
                addressList = paymentInfo.value!!.address,
                whichAddress = whichAddress
            )

            Spacer(Modifier.height(7.dp))
            PaymentContact(
                cell = paymentInfo.value!!.phone,
                email = paymentInfo.value!!.email,
                editable = true
            )
            Spacer(Modifier.height(15.dp))

            var num = 0
            paymentInfo.value!!.item.forEach { item ->
                num += item.number
            }
            PaymentNum(num)
            Spacer(Modifier.height(7.dp))
            PaymentRow(paymentInfo.value!!.item)
            Spacer(Modifier.height(15.dp))
            PaymentMethod()
            Spacer(Modifier.height(5.dp))
            PaymentMethodList(tmpRegisteredPayMethod)
            Spacer(Modifier.height(10.dp))
            PaymentCardList(
                selectedCardId = selectedCardId,
                cardList = paymentInfo.value!!.cards
            )
            Spacer(Modifier.height(10.dp))
        }
    } else{
        LoaderSet(info = "결제 중입니다",semantics = "결제 중입니다")
    }

}

// 배송지 주소 컴포저블
@Composable
fun PaymentAddress(
    addressId: Int,
    address: String,
    editable: Boolean,
    whichAddress: MutableState<Int>
){
    Column(
        modifier = Modifier
            .semantics(mergeDescendants = true){
                text = AnnotatedString("배송지 주소는 서울특별시 서대문구 독립문로 129-1 가나다 아파트세상 203동 1104호 입니다. 배송지를 수정하시려면 다음에 나오는 배송지 수정 버튼을 눌러주세요.")
            }
            .width(270.dp)
            .height(100.dp)
            .padding(
                end = 3.dp
            )
            .clip(RoundedCornerShape(7.dp))
            .border(
                width = if(whichAddress.value == addressId){
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
            .clickable {
                whichAddress.value = addressId
            }

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

// 연락처 정보 컴포저블
@Composable
fun PaymentContact(
    cell: String,
    email: String,
    editable: Boolean
){
    Column(
        modifier = Modifier
            .semantics(mergeDescendants = true){
                text = AnnotatedString("주문자의 전화번호는 010-1234-5678이며, 메일 주소는 123456@gmail.com 입니다. 연락처 정보를 수정하시려면 다음에 나오는 연락처 수정 버튼을 눌러주세요.")
            }
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .clip(RoundedCornerShape(7.dp))
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
            text = "연락처",
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
                text = cell+"\n"+email,
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
                        contentDescription = "연락처 수정 버튼",
                        modifier = Modifier

                    )
                }
            }
        }
    }
}


// 결제 상품 개수 컴포저블
@Composable
fun PaymentNum(
    cartNum: Int
){

    Row (
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                text = AnnotatedString("현재 결제 목록에는 ${cartNum} 개의 상품이 존재합니다.")
            }
            .fillMaxWidth()
            .padding(20.dp),

        ) {
        Text(
            modifier = Modifier,

            textAlign = TextAlign.Center,
            text = "Items",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 25.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
        Spacer(Modifier.width(7.dp))
        Text(
            modifier = Modifier
                .clip(shape = CircleShape)
                .background(
                    color = Selected
                )
                .width(30.dp)
                .height(30.dp)
                .offset(
                    y = 5.dp
                )
            ,
            textAlign = TextAlign.Center,
            text = cartNum.toString(),
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
    }
}

// 결제 정보 목록 컴포저블
// 시간이 없어 모듈화는 생략함
@Composable
fun PaymentRow(
    paymentInfo: List<BasketInfo>
){



    //
    paymentInfo.forEachIndexed { index, item ->
        Column {
            Row(
                modifier = Modifier
                    .semantics(mergeDescendants = true){
                        text = AnnotatedString("${item.name} 상품이 총 ${item.number} 개 담겨 있습니다. 상품 가격은 ${item.price.toInt()}원 입니다.")
                    }
                    .padding(
                        start = 10.dp,
                        end = 10.dp
                    )
                ,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Box(){
                    AsyncImage(
                        model = if(item.img[0]=='\"'){item.img.substring(1,item.img.length-1)} else{item.img},
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
                        Text(
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
                                .offset(
                                    y = 5.dp
                                )
                            ,
                            textAlign = TextAlign.Center,
                            text = item.number.toString(),
                            color = TextLittleDark,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                            )
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .padding(
                            start = 5.dp
                        )
                        .padding(13.dp)
                        .weight(7f)
                        .padding(top = 10.dp)
                    ,
                    text = item.name,
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )

                val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(item.price)

                Text(
                    modifier = Modifier
                        .padding(
                            start = 5.dp
                        )
                        .padding(13.dp)
                        .weight(4f)
                        .padding(top = 17.dp)

                    ,
                    text = textPrice + "원",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}

// 결제 정보 목록 컴포저블
// 시간이 없어 모듈화는 생략함
@Composable
fun PayDetailHistoryRow(
    paymentInfo: MutableState<PaymentDetailInfo?>
){



    //
    paymentInfo.value?.items?.forEachIndexed { index, item ->
        Column {
            Row(
                modifier = Modifier
                    .semantics(mergeDescendants = true){
                        text = AnnotatedString("${item.name} 상품이 총 ${item.number} 개 담겨 있습니다. 상품 가격은 ${item.price.toInt()}원 입니다.")
                    }
                    .padding(
                        start = 10.dp,
                        end = 10.dp
                    )
                ,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Box(){
                    AsyncImage(
                        model = if(item.img[0]=='\"'){item.img.substring(1,item.img.length-1)} else{item.img},
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
                        Text(
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
                                .offset(
                                    y = 5.dp
                                )
                            ,
                            textAlign = TextAlign.Center,
                            text = item.number.toString(),
                            color = TextLittleDark,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                            )
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .padding(
                            start = 5.dp
                        )
                        .padding(13.dp)
                        .weight(7f)
                        .padding(top = 10.dp)
                    ,
                    text = item.name,
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )

                val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(item.price)

                Text(
                    modifier = Modifier
                        .padding(
                            start = 5.dp
                        )
                        .padding(13.dp)
                        .weight(4f)
                        .padding(top = 17.dp)

                    ,
                    text = textPrice + "원",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}

// 결제 수단 방법 선택 컴포저블
@Composable
fun PaymentMethod(){
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .semantics(mergeDescendants = true){
                text = AnnotatedString("아래에 원하시는 결제 방법을 선택해주세요. 결제 방법을 수정하시려며 다음에 나오는 결제 방법 변경 버튼을 눌러주세요.")
            }
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp
            ),

        ) {
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Center,
            text = "Payment Method",
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 25.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
    }
}

@Composable
fun PaymentMethodList(
    tmpRegisteredPayMethod: MutableList<String>,
    whichMethod: MutableState<Int> = mutableStateOf(0)
){
    val scrollState = rememberScrollState()
    val selected = remember{ mutableStateOf(0) }
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(
                start = 20.dp
            )
    ){
        tmpRegisteredPayMethod.forEachIndexed { index, item ->
            Button(
                modifier = Modifier
                    .semantics(mergeDescendants = true){
                        text = AnnotatedString(item)
                    },
                onClick = {
                    selected.value = index
                    whichMethod.value = index
                },
                colors = if(selected.value == index){
                    ButtonColors(
                        contentColor = Selected,
                        containerColor = Selected,
                        disabledContentColor = Selected,
                        disabledContainerColor = Selected
                    )
                } else {
                    ButtonColors(
                        containerColor = Unselected,
                        contentColor = Unselected,
                        disabledContainerColor = Unselected,
                        disabledContentColor = Unselected
                    )
                }
            ){
                Text(
                    modifier = Modifier,

                    textAlign = TextAlign.Center,
                    text = item,
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
            }
            Spacer(Modifier.width(7.dp))
        }
    }
}


@Composable
fun PaymentCardList(
    selectedCardId: MutableState<Int>,
    cardList: List<CardInfo>
){
    val scrollState = rememberScrollState()


    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(
                start = 20.dp
            )
    ){
        cardList.forEachIndexed { index, item ->
            PaymentSmallCard(
                cardID = item.card_id,
                company = item.company,
                paymentNumber = item.card_code,
                name = "",
                expiredMonth = item.date.substring(0,2),
                expiredDate = item.date.substring(2,4),
                selectedCardId = selectedCardId
            )
            Spacer(Modifier.width(2.dp))
        }
    }
}
@Composable
fun PaymentSmallCard(
    cardID: Int,
    company: String,
    paymentNumber: String,
    name: String,
    expiredMonth: String,
    expiredDate: String,
    selectedCardId: MutableState<Int>
){
    val logo = PaymentLogoSelector(company)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(
                horizontal = 3.dp
            )
            .border(
                width = if(selectedCardId.value == cardID){
                    2.dp
                } else {
                    0.dp
                },
                color = IconBlue,
                shape = RoundedCornerShape(15.dp)
            )
            .clip(
                shape = RoundedCornerShape(15.dp)
            )
            .background(color= com.example.team_voida.ui.theme.PaymentCard)
            .clickable {
                selectedCardId.value = cardID
            }

    ){
        Column (
            modifier = Modifier.fillMaxWidth()
        ){

            // Logo and Setting
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Image(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        )
                    ,
                    painter = painterResource(logo),
                    contentDescription = ""
                )
                Button(
                    onClick = {
                    },
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

                }
            }

            Spacer(Modifier.height(30.dp))

            // Card Number
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Spacer(Modifier.width(35.dp))
                val lastNumber = paymentNumber.substring(12,16)

                for(i in 1..3){
                    Text(
                        text = "****  ",
                        style = TextStyle(
                            color = TextColor,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
                Text(
                    text = lastNumber,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.width(10.dp))
            }

            Spacer(Modifier.height(15.dp))

            // Name and Expired
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ){
                Spacer(Modifier.width(130.dp))

                Spacer(Modifier.weight(1f))

                Text(
                    text = expiredMonth + "/" + expiredDate,
                    style = TextStyle(
                        color = TextColor,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PaymentAddressList(
    addressList: List<Address>,
    whichAddress: MutableState<Int> = mutableStateOf(0)
){
    val scrollState = rememberScrollState()
    val selected = remember{ mutableStateOf(0) }
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(
                start = 10.dp
            )
    ){
        addressList.forEachIndexed { index, item ->
            PaymentAddress(
                addressId = item.address_id,
                address = item.address_text,
                editable = true,
                whichAddress = whichAddress
            )
            Spacer(Modifier.width(3.dp))
        }
    }
}