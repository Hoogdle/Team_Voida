package com.example.team_voida.CreateAccount

import android.provider.Settings.Global
import android.util.Log
import com.example.team_voida.Home.Popular
import com.example.team_voida.session
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class UserInfo(
    val email: String,
    val pw: String,
    val cell: String
)

@Serializable
data class UserInfoResult(
    val detail: String
)

@Serializable
data class UserName(
    val un: String
)

@Serializable
data class SessionId(
    val sessionId: String
)

@Serializable
data class SignUpFinish(
    val result: Boolean,
    val session_id: String
)
// 회원가입, 사용자 정보가 서버에 전달
@OptIn(DelicateCoroutinesApi::class)
suspend fun CheckEmail(
    email: String
): Boolean{

    val jsonObject = JSONObject()
    jsonObject.put("email", email)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/EmailCheck") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.doOutput = true // 서버로 보내기 위해 doOutPut 옵션 활성화
        connection.doInput = true
        connection.requestMethod = "POST" // edit2 // or POST

        // 서버와 통신을 위하 코드는 아래으 url 참조
        // https://johncodeos.com/post-get-put-delete-requests-with-httpurlconnection/
        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        ) // The format of the content we're sending to the server
        connection.setRequestProperty(
            "Accept",
            "application/json"
        ) // The format of response we want to get from the server

        val outputStreamWriter = OutputStreamWriter(connection.outputStream)
        outputStreamWriter.write(jsonObjectString)
        outputStreamWriter.flush()


        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
            val json = Json.decodeFromString<Boolean>(inputStream) // edit3
            return json
        } else {
            Log.e("xxx","wrong!")
            return  false
        }
    } catch (e: Exception) {
        Log.e("xxx","what!")

        e.printStackTrace()
        return  false
    }
}

suspend fun CreateAccountServer(
    email: String,
    pw: String,
    cell: String,
    un: String,
    address: String
):Boolean?{
    val jsonObject = JSONObject()
    jsonObject.put("email", email)
    jsonObject.put("pw", pw)
    jsonObject.put("cell", cell)
    jsonObject.put("un", un)
    jsonObject.put("address", address)


    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/SignUp") // edit1
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.doOutput = true // 서버로 보내기 위해 doOutPut 옵션 활성화
        connection.doInput = true
        connection.requestMethod = "POST" // edit2 // or POST

        // 서버와 통신을 위하 코드는 아래으 url 참조
        // https://johncodeos.com/post-get-put-delete-requests-with-httpurlconnection/
        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        ) // The format of the content we're sending to the server
        connection.setRequestProperty(
            "Accept",
            "application/json"
        ) // The format of response we want to get from the server

        val outputStreamWriter = OutputStreamWriter(connection.outputStream)
        outputStreamWriter.write(jsonObjectString)

        outputStreamWriter.flush()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {

            val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
            val json = Json.decodeFromString<SignUpFinish>(inputStream) // edit3
            session.sessionId.value = json.session_id as String

            return json.result
        } else {
            return  null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return  null
    }
}
