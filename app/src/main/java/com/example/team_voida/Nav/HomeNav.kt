package com.example.team_voida.Nav

import AssistantSelector
import AssistantToServer
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.team_voida.Basket.Basket
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Basket.BasketPaymentButton
import com.example.team_voida.Categories.Categories
import com.example.team_voida.Categories.CategoryList
import com.example.team_voida.CreateAccount.CreateAccount
import com.example.team_voida.CreateAccount.CreateAccountNaming
import com.example.team_voida.Home.Home
import com.example.team_voida.Home.HomePartList
import com.example.team_voida.Home.HomePopularCall
import com.example.team_voida.Home.Popular
import com.example.team_voida.Login.Login
import com.example.team_voida.Payment.PayRegister
import com.example.team_voida.Payment.Payment
import com.example.team_voida.Payment.PaymentUserInfo
import com.example.team_voida.ProductInfo.ProductInfo
import com.example.team_voida.ProductInfo.ProductInfoBottomBar
import com.example.team_voida.ProductInfo.ProductInfoInfo
import com.example.team_voida.ProductInfo.sampleProductInfoData
import com.example.team_voida.Profile.Account
import com.example.team_voida.Profile.PaymentHistory
import com.example.team_voida.Profile.PaymentHistoryList
import com.example.team_voida.Profile.PaymentSetting
import com.example.team_voida.Profile.Profile
import com.example.team_voida.R
import com.example.team_voida.SearchResult.SearchResult
import com.example.team_voida.Start.Guide
import com.example.team_voida.Start.Start
import com.example.team_voida.Tools.LoaderSet
import com.example.team_voida.session
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// HomeNav 파일에 거의 모든 컴포저블에서 사용하는 정보가 저장됨.
// 즉 거의 대부분의 컴포저블은 HomeNav로 부터 시작되며
// HomeNav에서 선언한 변수를 인자로 받아 사용함

// 하단 네비게이션 리스트 
val navItemList = listOf(
    BottomNav(
        unSelected = R.drawable.bottom_home,
        selected = R.drawable.bottom_sel_home,
        notify = "홈 화면 이동 버튼"
    ),
    BottomNav(
        unSelected = R.drawable.bottom_categories,
        selected = R.drawable.bottom_sel_categories,
        notify = "카테고리 화면 이동 버튼"
    ),
    BottomNav(
        unSelected = R.drawable.logo,
        selected = R.drawable.logo,
        notify = "AI Assitant 활성화"
    ),
    BottomNav(
        unSelected = R.drawable.bottom_cart,
        selected = R.drawable.bottom_sel_basket,
        notify = "장바구니 화면 이동 버튼"
    ),
    BottomNav(
        unSelected = R.drawable.bottom_profile,
        selected = R.drawable.bottom_sel_profile,
        notify = "계정 화면 이동 버튼"
    )
)
@SuppressLint("RememberReturnType", "UnrememberedMutableState")
@Composable
fun HomeNav(){

    // 각 화면에서 사용할 변수들 선언 ex) 네비게이션, 하단네비 Flag bit, 검색 입력란 ...
    val navController = rememberNavController() // home nav
    val basketController = rememberNavController()
    var selectedIndex = remember { mutableStateOf(0) }
    val basketFlag = remember { mutableStateOf(false) }
    val homeNavFlag = remember { mutableStateOf(true)}
    val productFlag = remember{ mutableStateOf(false) }
    val dynamicTotalPrice = remember { mutableStateOf("") }
    val isWhichPart = remember{ mutableStateOf(1) } // 실시간인기, 많이담는특가, ... 의 리스트를 구분하는 숫자
    val productID = remember { mutableStateOf(0)}
    val isItemWhichPart = remember{ mutableStateOf(0) }
    val price = remember { mutableStateOf(0f) }
    val categoryCode = remember{ mutableStateOf("") }
    val isPayOne = remember { mutableStateOf(true) } // 단일 결제, 다수 결제 구분

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }


    val context = LocalContext.current
    val view = LocalView.current

    val upPressed = remember { mutableStateOf(false) }
    val downPressed = remember { mutableStateOf(false) }


    // 화면 최소/최대 확대 비율 설정
    val minScale = 1f
    val maxScale = 4f

    // Remember the initial offset
    var initialOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    // Coefficient for slowing down movement
    val slowMovement = 0.5f

    val scrollState = rememberScrollState()

    // Lens(zoom) variable
    val interactionSource = remember{ MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val result: MutableState<List<Popular>?> = remember { mutableStateOf<List<Popular>?>(null) }

    val voiceInput = remember{ mutableStateOf("") }
    val isAssistantWorking = remember{ mutableStateOf(false) }

    val orderNumber = remember{ mutableStateOf("") }

    val isPayPage = remember { mutableStateOf(false) }
    val paymentUserInfo: MutableState<PaymentUserInfo> = remember { mutableStateOf(PaymentUserInfo("","","")) }

    val cardId: MutableState<Int> = remember { mutableStateOf(-1) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                voiceInput.value = spokenText  // Update prompt with recognized text
            } else {
                Log.e("here","here")
                isAssistantWorking.value = false
                Toast.makeText(context, "음성인식에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )


    // 홈 화면에 제공할 데이터를 서버에 요청
    runBlocking {
        val job = GlobalScope.launch {
            result.value = HomePopularCall()
        }
    }

    // 검색 입력 데이터 저장
    val input = remember{ mutableStateOf("") }


    fun aiAssitant(){
        isAssistantWorking.value = true

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voida Assistance가 음성을 인식합니다.")
            speechRecognizerLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    if(voiceInput.value != "" && voiceInput.value != "None"){
        var category: String? = null
        Log.e("haha","haha")

        if(voiceInput.value != ""){
            runBlocking {
                val job = GlobalScope.launch{
                    category = AssistantToServer(voiceInput.value)
                }
            }
        }

        while(category == null){
            Thread.sleep(1000L)
        }
        Log.e("haha","haha")

        Log.e("debug","category" + category.toString())

        AssistantSelector(
            isWhichPart = isWhichPart,
            isItemWhichPart = isItemWhichPart,
            llmCategory = category.toString(),
            navController = navController,
            voiceInput = voiceInput.value,
            input = input,
            isAssistantWorking = isAssistantWorking
        )
    }

    Scaffold(
        // 모든 하단 네비게이션 등록
        bottomBar = {
            Column {
                if(basketFlag.value){
                    BasketPaymentButton(
                        dynamicTotalPrice,
                        isPayOne,
                        navController,
                        isPayPage
                    )
                }
                if(productFlag.value){
                    val textPrice = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(price.value)
                    ProductInfoBottomBar(
                        textPrice,
                        productID,
                        isItemWhichPart,
                        navController,
                        isPayOne
                    )
                }
                if(homeNavFlag.value){
                    NavigationBar(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color.LightGray
                            ),
                        containerColor = Color.White
                    ){
                        navItemList.forEachIndexed { index, item ->
                            var tmpIndex = 0.dp
                            if(index == selectedIndex.value){
                                tmpIndex = 3.8.dp
                                if(index == 1){
                                    tmpIndex = 4.dp
                                }
                            }

                            NavigationBarItem(
                                colors = NavigationBarItemColors(
                                    selectedIndicatorColor = Color.Transparent,
                                    selectedTextColor = Color.Transparent,
                                    selectedIconColor = Color.Transparent,
                                    unselectedIconColor = Color.Transparent,
                                    unselectedTextColor = Color.Transparent,
                                    disabledIconColor = Color.Transparent,
                                    disabledTextColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .size(
                                        if(index != 2){
                                            30.dp
                                        } else {
                                            50.dp
                                        }
                                    )
                                    .offset(
                                        y = if(index != 2) tmpIndex
                                        else 0.dp
                                    )
                                ,
                                selected = selectedIndex.value == index,
                                onClick = {
                                    selectedIndex.value = index
                                    if(selectedIndex.value == 0) navController.navigate("home")
                                    else if(selectedIndex.value ==1) navController.navigate("categories")
                                    else if(selectedIndex.value ==2){
                                        // AI Assistant
                                        aiAssitant()

                                    }
                                    else if(selectedIndex.value == 3) navController.navigate("basket")
                                    else if(selectedIndex.value == 4) navController.navigate("profile")
                                },
                                icon = {
                                    if(index == selectedIndex.value && index != 2){
                                        Column {
                                            Image(
                                                modifier = Modifier
                                                    .size(27.dp)
                                                ,
                                                painter = painterResource(item.selected),
                                                contentDescription = item.notify
                                            )
                                        }
                                    } else{
                                        if (index == 2){
                                            Image(
                                                modifier = Modifier
                                                    .padding(0.dp)
                                                    .zIndex(1f)
                                                    .size(
                                                        50.dp
                                                    ),
                                                painter = painterResource(item.unSelected),
                                                contentDescription = item.notify
                                            )
                                        } else {
                                            Image(
                                                modifier = Modifier
                                                    .size(
                                                        20.dp
                                                    ),
                                                painter = painterResource(item.unSelected),
                                                contentDescription = item.notify
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ){ inner ->
        if(isAssistantWorking.value){
            LoaderSet(info = "AI Asssitant", semantics = "A I Assistant가 정보를 분석하는 중입니다. 잠시만 기다려주세요.")
        }else {
            NavHost(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    // 아래의 코드는 인터넷에 제공된 오픈소스 참고
                    // 화면을 확대 및 축소하는 기능
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, _, _ ->

                            val newScale = scale
                            scale = newScale.coerceIn(minScale, maxScale)

                            // Calculate new offsets based on zoom and pan
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val offsetXChange = (centerX - offsetX) * (newScale / scale - 1)
                            val offsetYChange = (centerY - offsetY) * (newScale / scale - 1)

                            // Calculate min and max offsets
                            val maxOffsetX = (size.width / 2) * (scale - 1)
                            val minOffsetX = -maxOffsetX
                            val maxOffsetY = (size.height / 2) * (scale - 1)
                            val minOffsetY = -maxOffsetY

                            // Update offsets while ensuring they stay within bounds
                            if (scale <= maxScale) {
                                offsetX = (offsetX + pan.x * scale * slowMovement + offsetXChange)
                                    .coerceIn(minOffsetX, maxOffsetX)
                                offsetY = (offsetY + pan.y * scale * slowMovement + offsetYChange)
                                    .coerceIn(minOffsetY, maxOffsetY)
                            }

                            // Store initial offset on pan
                            if (pan != Offset(0f, 0f) && initialOffset == Offset(0f, 0f)) {
                                initialOffset = Offset(offsetX, offsetY)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                // Reset scale and offset on double tap
                                if (scale != 1f) {
                                    scale = 1f
                                    offsetX = initialOffset.x
                                    offsetY = initialOffset.y
                                } else {
                                    scale = 2f
                                }
                            }
                        )
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    },
                navController = navController,
                startDestination = "home"
            ) {
                // HomeNav에서 갈 수 있는 모든 페이지의 네비게이션 등록
                composable("home") {
                    Home(
                        navController = navController,
                        input = input,
                        result = result.value,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        isWhichPart = isWhichPart,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart,
                        barPrice = price
                    )
                }
                composable("searchResult") {
                    SearchResult(
                        navController = navController,
                        input = input,
                        productName = input.value,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart,
                        barPrice = price
                    )
                }
                composable("basket") {
                    Basket(
                        dynamicTotalPrice,
                        navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart,
                        isPayPage = isPayPage
                    )
                }
                composable("productInfo") {
                    ProductInfo(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart,
                    )
                }
                composable("categories") {
                    Categories(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        categoryCode = categoryCode,
                        isItemWhichPart = isItemWhichPart
                    )
                }

                composable("payment") {
                    Payment(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart,
                        isPayOne = isPayOne,
                        isPayPage = isPayPage,
                        paymentUserInfo = paymentUserInfo,
                        dynamicTotalPrice = dynamicTotalPrice,
                        cardID = cardId
                    )
                }

                composable("profile") {
                    Profile(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex
                    )
                }

                composable("account") {
                    Account(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex
                    )
                }
                composable("partList") {
                    HomePartList(
                        navController = navController,
                        input = input,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        isWhichPart = isWhichPart,
                        barPrice = price,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart
                    )
                }

                composable("categoryList") {
                    CategoryList(
                        navController = navController,
                        input = input,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        isWhichPart = isWhichPart,
                        barPrice = price,
                        categoryCode = categoryCode.value,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart
                    )
                }

                composable("paymentSetting") {
                    PaymentSetting(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex
                    )
                }

                composable("paymentHistory") {
                    PaymentHistory(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        orderNumber = orderNumber
                    )
                }
                composable("paymentHistoryList") {
                    PaymentHistoryList(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        orderNumber = orderNumber
                    )
                }

                composable("payRegister"){
                    PayRegister(
                        navController = navController,
                        basketFlag = basketFlag,
                        homeNavFlag = homeNavFlag,
                        productFlag = productFlag,
                        selectedIndex = selectedIndex,
                        productID = productID,
                        isItemWhichPart = isItemWhichPart,
                        isPayOne = isPayOne,
                        isPayPage = isPayPage,
                        paymentUserInfo = paymentUserInfo,
                        cardId = cardId
                    )
                }
            }
        }
    }
}