package com.example.team_voida.ProfileServer

import android.util.Log
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.Payment.PaymentInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp
import java.util.Date

@Serializable
data class PayHistoryList(
    val card_id: Int,
    val card_code: String,
    val card_date: String,
    val card_company: String,
    val pay_list: List<PayHistory>,
    val card_num: Int
)

@Serializable
data class PayHistory(
    val is_refund: Boolean,
    val date: String,
    val order_num: String,
    val price: Int
)

@Serializable
data class PayDetailItem(
    val product_id: Int,
    val img: String,
    val name: String,
    val price: Float,
    val number: Int
)


@Serializable
data class PayDetailHistory(
    val order_num: String,
    val address: String,
    val cell: String,
    val item_list: List<BasketInfo>
)

@Serializable
data class PaymentDetailInfo(
    val order_num: String,
    val address: String,
    val cell: String,
    val email: String,
    val items: List<BasketInfo>
)


suspend fun PayHistoryListServer(
    session_id: String
): List<PayHistoryList>{

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
            val json = Json.decodeFromString<List<PayHistoryList>>(inputStream) // edit3
            return json
        } else {
            Log.e("xxx","else")
            return listOf(
                PayHistoryList(
                    card_id = -1,
                    card_code = "",
                    card_date = "",
                    card_company = "",
                    card_num = 0,
                    pay_list = listOf(
                        PayHistory(
                            is_refund = false,
                            date = "",
                            order_num = "",
                            price = 0
                        )
                    )
                )
            )
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")

        e.printStackTrace()
        return listOf(
            PayHistoryList(
                card_id = -1,
                card_code = "",
                card_date = "",
                card_company = "",
                card_num = 0,
                pay_list = listOf(
                    PayHistory(
                        is_refund = false,
                        date = "",
                        order_num = "",
                        price = 0
                    )
                )
            )
        )
    }
}


suspend fun PayDetailHistoryListServer(
    session_id: String,
    order_num: String
): PaymentDetailInfo?{

    val jsonObject = JSONObject()
    jsonObject.put("session_id", session_id)
    jsonObject.put("order_num", order_num)

    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/PayDetailHistoryList") // edit1
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
            val json = Json.decodeFromString<PaymentDetailInfo>(inputStream) // edit3
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


suspend fun CancelOrder(
    session_id: String,
    order_num: String
): Boolean{

    val jsonObject = JSONObject()
    jsonObject.put("session_id", session_id)
    jsonObject.put("order_num", order_num)

    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/CancelOrder") // edit1
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
            return  false
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")

        e.printStackTrace()
        return  false
    }
}