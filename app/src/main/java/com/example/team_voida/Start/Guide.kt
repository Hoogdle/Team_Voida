package com.example.team_voida.Start

import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.navigation.NavController
import com.example.team_voida.Notification.Notification
import com.example.team_voida.R
import com.example.team_voida.ui.theme.ButtonBlue
import com.example.team_voida.ui.theme.TextLittleDark
import com.example.team_voida.ui.theme.TextWhite

// 사용자 안내 메인 컴포저블
@Composable
fun Guide(
    navController: NavController
){
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("1","2","3","4")
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){
        Notification("앱 사용 안내 페이지입니다. 중앙부 화면에서 어플리케이션 사용방법을 안내 받을 수 있습니다. 오른쪽 스와이프 제스쳐를 통해 다음 안내 메시지를 제공 받을 수 있습니다.")
        Spacer(Modifier.height(15.dp))
        GuidePager(navController)
    }
}

@Composable
fun GuidePager(
    navController: NavController
){
    Column (
        modifier = Modifier.fillMaxSize()
    ){
        val pageCount = 4
        val pagerState = rememberPagerState(
            pageCount = {pageCount}
        )

        // 스와이프 제스쳐를 위한 컴포저블 활용
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                when(it){
                    0 -> Guide1()
                    1 -> Guide2()
                    2 -> Guide3()
                    3 -> Guide4(navController = navController)
                }
            }
        }
        Row (
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            repeat(pageCount){ iteration ->
                val color = if (pagerState.currentPage == iteration) Color(0,76,255) else Color(199,214,251)
                Box(
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            end = 8.dp
                        )
                        .background(color, CircleShape)
                        .size(10.dp)
                )
            }
        }
    }
}

// Guide1, Guide2, Guide3, Guide4 모두 화면 안내 관려 페이지
@Composable
fun Guide1(
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Box(
            modifier = Modifier
                .semantics(mergeDescendants = true){
            }
        ){
            Image(

                painter = painterResource(R.drawable.guide1),
                contentDescription = ""
            )
            Column(

                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(
                        x = 45.dp,
                        y = 420.dp
                    )
                    .width(300.dp)
            ){
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "안녕하세요 Voida 쇼핑입니다.",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "본 어플리케이션은 시각 장애인을 위한 쇼핑앱으로, 스크린 리더기를 보다 편리하게 사용할 수 있게 디자인 되었습니다. 화면을 우측으로 스와이프 하여 보다 다양한 기능을 만나보세요.",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
                Spacer(Modifier.height(35.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "두 손가락으로 우측으로 스와이프 →",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
            }
        }
    }
}

@Composable
fun Guide2(
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Box(
            modifier = Modifier
                .semantics(mergeDescendants = true){
                }
        ){
            Image(

                painter = painterResource(R.drawable.guide2),
                contentDescription = ""
            )
            Column(

                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(
                        x = 45.dp,
                        y = 400.dp
                    )
                    .width(300.dp)
            ){
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "AI Assistant",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "오직 음성만으로 쇼핑을 즐길 수 있습니다! 화면 최하단 중앙부에 AI Assistant 버튼을 누른 뒤, 원하는 기능을 말해보세요!",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
                Spacer(Modifier.height(45.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "두 손가락으로 우측으로 스와이프 →",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
            }
        }
    }
}

@Composable
fun Guide3(
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Box(
            modifier = Modifier
                .semantics(mergeDescendants = true){
                }
        ){
            Image(

                painter = painterResource(R.drawable.guide3),
                contentDescription = ""
            )
            Column(

                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(
                        x = 45.dp,
                        y = 400.dp
                    )
                    .width(300.dp)
            ){
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "제스처로 화면 확대",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
                Spacer(Modifier.height(35.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "두 손가락으로 화면을 줌 인 하거나, 화면을 빠르게 두 번 터치하여 화면을 확대할 수 있습니다!",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
                Spacer(Modifier.height(35.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "두 손가락으로 우측으로 스와이프 →",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
            }
        }
    }
}

@Composable
fun Guide4(
    navController: NavController
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Box(
            modifier = Modifier
                .semantics(mergeDescendants = true){
                }
        ){
            Image(

                painter = painterResource(R.drawable.guide4),
                contentDescription = ""
            )
            Column(

                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(
                        x = 45.dp,
                        y = 410.dp
                    )
                    .width(300.dp)
            ){
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "준비 되셨나요?",
                    color = TextLittleDark,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                    )
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = "Voida를 통해 보다 편리한 쇼핑을 즐기세요! 아래의 시작하기 버튼을 눌러 홈 화면으로 이동해주세요!",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 25.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    )
                )
                Spacer(Modifier.height(20.dp))
                GuideButton(navController)
            }
        }
    }
}

@Composable
fun GuideButton(
    navController: NavController
){
    Button(
        shape = RectangleShape,
        modifier = Modifier
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .width(185.dp)
            .height(65.dp)
            .clip(shape = RoundedCornerShape(15.dp))
        ,
        onClick = {
            navController.navigate("home")
        },
        colors = ButtonColors(
            containerColor = ButtonBlue,
            contentColor = TextWhite,
            disabledContentColor = TextWhite,
            disabledContainerColor = ButtonBlue
        )
    ) {
        Text(
            text = "시작하기",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = TextWhite,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )
        )
    }
}








