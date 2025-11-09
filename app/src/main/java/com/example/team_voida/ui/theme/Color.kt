package com.example.team_voida.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.team_voida.theme

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)



val ButtonBlue = if(theme.themeId.value == 0 ){
    Color(0,76,255)
} else if(theme.themeId.value == 1) {
    Color(0,0,0)
} else {
    Color(0,76,255)
}

val NotifyBlock = Color(249,249,249)
val Selected = Color(229,235,252)
val TextColor = Color.Black
val IconBlue = Color(0,76,255)
val WishButton = Color(249,249,249)
val TextWhite = Color(243,243,243)
val TextLittleDark = Color(32,32,32)
val LoginTextFiled = Color(248,248,248)
val SearchBarColor = Color(248,248,248)
val BasketPaymentColor = Color(245,245,245)
val ButtonBlackColor = Color(32,32,32)
val SkyBlue = Color(229,235,252)
val LightPink = Color(255,235,235)
val Unselected = Color(249,249,249)
val DisabledText = Color(188,188,188)
val PaymentCard = Color(241,244,254)
val CancelColor = Color(248,17,64)