package com.example.team_voida.Login

import android.util.Log
import com.example.team_voida.CreateAccount.SessionId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


@Serializable
data class LoginRequest(
    val email: String,
    val pw: String
)

@Serializable
data class LoginSession(
    val session_id: String
)

@Serializable
data class ResetPW1Response(
    val is_user: Boolean,
    val user_id: Int
)

suspend fun LoginServer(
    email: String,
    pw: String
): String?{

    val jsonObject = JSONObject()
    jsonObject.put("email", email)
    jsonObject.put("pw", pw)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/SignIn") // edit1
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
            val json = Json.decodeFromString<LoginSession>(inputStream) // edit3
            return json.session_id
        } else {
            Log.e("xxx","else")
            return null
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")

        e.printStackTrace()
        return null
    }
}


suspend fun ResetPW2(
    userId: Int,
    pw: String
): Boolean{

    val jsonObject = JSONObject()
    jsonObject.put("user_id",userId)
    jsonObject.put("pw", pw)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/ResetPW2") // edit1
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
            return true
        } else {
            Log.e("xxx","else")
            return false
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")

        e.printStackTrace()
        return false
    }
}


suspend fun ResetPW1(
    email: String,
    cell: String
): ResetPW1Response{

    val jsonObject = JSONObject()
    jsonObject.put("email", email)
    jsonObject.put("cell", cell)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/ResetPW1") // edit1
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
            val json = Json.decodeFromString<ResetPW1Response>(inputStream) // edit3
            return json
        } else {
            Log.e("xxx","else")
            return ResetPW1Response(
                is_user = false,
                user_id = -1
            )
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")

        e.printStackTrace()
        return ResetPW1Response(
            is_user = false,
            user_id = -1
        )
    }
}