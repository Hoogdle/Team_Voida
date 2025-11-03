package com.example.team_voida.ProfileServer

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class PayHistoryList(
    val card_id: Int,
    val card_code: String,
    val card_date: String,
    val card_company: String,
    val pay_list: List<PayHistory>
)

@Serializable
data class PayHistory(
    val is_refund: Boolean,
    val date: String,
    val order_num: String,
    val price: Int
)

suspend fun PayHistoryListServer(
    session_id: String
): PayHistory?{

    val jsonObject = JSONObject()
    jsonObject.put("session_id", session_id)

    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/PayHistoryList") // edit1
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
            val json = Json.decodeFromString<PayHistory>(inputStream) // edit3
            return json
        } else {
            Log.e("xxx","else")
            return  null
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")

        e.printStackTrace()
        return  null
    }
}