package com.example.team_voida.Basket

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import com.example.team_voida.ui.theme.BasketPaymentColor
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.Selected
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun Basket(
    dynamicTotalPrice: MutableState<String>,
    navController: NavController,
    basketFlag: MutableState<Boolean>,
    homeNavFlag: MutableState<Boolean>,
    productFlag: MutableState<Boolean>,
    selectedIndex: MutableState<Int>,
    productID: MutableState<Int>,
    isItemWhichPart: MutableState<Int>
){
    val scrollState = rememberScrollState()
    val cartNum = remember { mutableStateOf(0)}


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
            basketFlag.value = true
            homeNavFlag.value = true
            productFlag.value = false

            selectedIndex.value =  3
            Log.e("123","on_start")
        } else if(event == Lifecycle.Event.ON_RESUME){
            Log.e("123","on_resume")
        }
    }

    val basketInfo: MutableState<List<BasketInfo>?> = remember { mutableStateOf<List<BasketInfo>?>(null) }

    if(basketInfo.value == null){
        runBlocking {
            val job = GlobalScope.launch{
                basketInfo.value = BasketListServer(session.sessionId.value)
            }
        }
    }

    if(basketInfo.value != null){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)

        ){
            var tmp = 0;
            basketInfo.value!!.forEach { item ->
                tmp += item.number
            }
            cartNum.value = tmp
            Notification("장바구니 화면입니다. 아래에 장바구니에 담긴 상품을 확인하고, 오른쪽 하단의 결제하기 버튼으로 상품을 구매하세요.")
            BasketCartNum(cartNum)
            Spacer(Modifier.height(15.dp))
            BasketItemArrange(
                basketInfo.value,
                dynamicTotalPrice,
                basketInfo
            )
        }
    } else {
        LoaderSet()
    }

}

@Composable
fun BasketCartNum(
    cartNum: MutableState<Int>
){

    Row (
        modifier = Modifier
            .semantics(mergeDescendants = true){
                text = AnnotatedString("현재 장바구니에는 ${cartNum.value} 개의 상품이 담겨 있습니다.")
            }
            .fillMaxWidth()
            .padding(20.dp),

    ) {
        Text(
            modifier = Modifier,

            textAlign = TextAlign.Center,
            text = "Cart",
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
            text = cartNum.value.toString(),
            color = TextLittleDark,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
            )
        )
    }
}

@Composable
fun BasketItem(
    id: Int,
    img: String,
    name: String,
    option: String,
    num: Int,
    price: String,
    basketInfo: MutableState<List<BasketInfo>?>
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp,
                bottom = 10.dp
            )
    ){
        AsyncImage(
            model = if(img[0]=='\"'){img.substring(1,img.length-1)} else{img},
            contentDescription = "",
            modifier = Modifier
                .width(125.dp)
                .height(150.dp)
                .border(
                    width = 4.dp,
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                )
                .shadow(elevation = 15.dp, shape = RoundedCornerShape(15.dp))
        )

        Column (
            modifier = Modifier
                .padding(
                    start = 10.dp
                )
                .fillMaxHeight()
        ){
            Row (
                modifier = Modifier
            ){

                // product name
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    text = name,
                    color = TextLittleDark,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        fontSize = 16.sp
                    ),
                )

                // del button
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.5f)
                        .height(32.dp)
                    ,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonColors(
                        contentColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    onClick = {
                        runBlocking {
                            val job = GlobalScope.launch {
                                basketInfo.value = BasketModify(
                                    action = "BasketDel",
                                    product_id = id,
                                    session_id = session.sessionId.value
                                )
                            }
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(R.drawable.basket_del),
                        contentDescription = "",
                        modifier = Modifier
//                            .width(30.dp)
//                            .height(30.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }

            Spacer(Modifier.height(20.dp))

            // price row
            Row (
                modifier = Modifier
            ){
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    text =
                        if(option == "null"){
                            "옵션 없음"
                        } else {
                            option
                        }
                    ,
                    color = TextLittleDark,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        fontSize = 16.sp
                    ),
                )
            }
            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(
                        end = 10.dp
                    )
            ){
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(9f)
                        .offset(
                            y = 5.dp
                        ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    text = price + "원",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                        fontSize = 18.sp
                    ),
                )
                Row (
                    modifier = Modifier
                ){
                    Button(
                        modifier = Modifier
                            .size(30.dp),
                        onClick = {
                            runBlocking {
                                val job = GlobalScope.launch {
                                    basketInfo.value = BasketModify(
                                        action = "BasketSub",
                                        product_id = id,
                                        session_id = session.sessionId.value
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent,
                            disabledContentColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    ) {
                        Image(
                            painter = painterResource( R.drawable.basket_sub),
                            contentDescription = ""
                        )
                    }
                    Spacer(Modifier.width(5.dp))
                    Text(
                        modifier = Modifier
                            .clip(
                                shape = RoundedCornerShape(5.dp)
                            )
                            .background(
                                color = Selected
                            )
                            .width(30.dp)
                            .height(30.dp)
                            .offset(
                                y = 5.dp
                            ),
                        text = num.toString(),
                        color = TextLittleDark,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                            fontSize = 16.sp
                        ),
                    )
                    Spacer(Modifier.width(5.dp))
                    Button(
                        modifier = Modifier
                            .size(30.dp),
                        onClick = {
                            runBlocking {
                                val job = GlobalScope.launch {
                                    basketInfo.value = BasketModify(
                                        action = "BasketAdd",
                                        product_id = id,
                                        session_id = session.sessionId.value
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent,
                            disabledContentColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    ) {
                        Image(
                            painter = painterResource( R.drawable.basket_add),
                            contentDescription = ""
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun BasketItemArrange(
    basketItems: List<BasketInfo>?,
    dynamicTotalPrice: MutableState<String>,
    basketInfo: MutableState<List<BasketInfo>?>
){
    var totalPrice = 0

    Column(
        modifier = Modifier
            .fillMaxHeight()
    ){
        basketItems?.forEachIndexed { index, item ->
            val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(item.price)

            totalPrice += item.price.toInt() * item.number
            BasketItem(
                id = item.product_id,
                img = item.img,
                name = item.name,
                option = "옵션 없음",
                num = item.number,
                price = textPrice,
                basketInfo = basketInfo
            )
        }
        dynamicTotalPrice.value = "%,d".format(totalPrice)
    }
}

@Composable
fun BasketPaymentButton(
    price: String,
    isPayOne: MutableState<Boolean>,
    navController: NavController
){
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(
                color = BasketPaymentColor
            )
            .padding(
                top = 15.dp,
                bottom = 15.dp
            )
            .fillMaxWidth()

    ){
        Text(
            modifier = Modifier
                .padding(
                    start = 30.dp,
                    top = 10.5.dp
                )
                ,
            text = "총액" + " " + price + "원",
            color = TextLittleDark,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                fontSize = 16.sp
            ),
        )
        Button(
            modifier = Modifier
                .padding(
                    end =  20.dp
                )
                ,
            shape = RoundedCornerShape(15.dp),
            onClick = {
                isPayOne.value = false
                navController.navigate("payment")
            },
            colors = ButtonColors(
                contentColor = ButtonBlue,
                containerColor = ButtonBlue,
                disabledContentColor = ButtonBlue,
                disabledContainerColor = ButtonBlue
            ),

        ) {
            Text(
                text = "결제하기",
                color = TextWhite,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    fontSize = 16.sp
                ),
            )
        }
    }
}

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}