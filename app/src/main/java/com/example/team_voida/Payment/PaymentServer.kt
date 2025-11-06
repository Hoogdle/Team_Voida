package com.example.team_voida.Payment

import android.util.Log
import com.example.team_voida.Basket.BasketInfo
import com.example.team_voida.ProfileServer.CardInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL



@Serializable
data class PaymentUserInfo(
    var address: String,
    var email: String,
    var cell: String,
)
@Serializable
data class PaymentInfo(
    val address: String,
    val phone: String,
    val email: String,
    val item: List<BasketInfo>,
    val cards: List<CardInfo>
)

@Serializable
data class OrderResponse(
    val order_num: String,
    val address: String,
    val email: String,
    val cell: String,
    val item: List<BasketInfo>
)

suspend fun PaymentServerOne(
    action: String="",
    session_id: String,
    product_id: Int
): PaymentInfo?{

    val jsonObject = JSONObject()
    jsonObject.put("session_id", session_id)
    jsonObject.put("product_id", product_id)

    val jsonObjectString = jsonObject.toString()


    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/OneItemPayment") // edit1
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
            val json = Json.decodeFromString<PaymentInfo>(inputStream) // edit3
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

suspend fun PaymentServerMultiple(
    session_id: String
): PaymentInfo?{
    val jsonObject = JSONObject()
    jsonObject.put("session_id", session_id)

    val jsonObjectString = jsonObject.toString()


    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/BasketPayment") // edit1
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
            val json = Json.decodeFromString<PaymentInfo>(inputStream) // edit3
            return json
            Log.e("BasketPay",json.toString())
        } else {
            Log.e("xxx","else")
            Log.e("BasketPay","?")
            return  null
        }
    } catch (e: Exception) {
        Log.e("xxx","catch")
        Log.e("BasketPay","!")

        e.printStackTrace()
        return  null
    }
}

suspend fun AddOneOrder(
    session_id: String,
    address: String,
    email: String,
    cell: String,
    productId: Int
): OrderResponse?{
    val jsonObject = JSONObject()
    jsonObject.put("session_id", session_id)
    jsonObject.put("address", address)
    jsonObject.put("email", email)
    jsonObject.put("cell", cell)
    jsonObject.put("product_id", productId)

    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/CreateOrder") // edit1
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
            val json = Json.decodeFromString<OrderResponse>(inputStream) // edit3
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


suspend fun AddBaksetOrder(
    session_id: String,
    address: String,
    email: String,
    cell: String,
    price: Float,
    itemList: List<BasketInfo>,
    card_id: Int
): OrderResponse?{


    val jsonObject = JSONObject()
    jsonObject.put("session_id", session_id)
    jsonObject.put("address", address)
    jsonObject.put("phone", cell)
    jsonObject.put("email", email)
    jsonObject.put("total_price", price)
    jsonObject.put("card_id", card_id)

    val jsonArray = JSONArray()
    itemList.forEach { item ->
        val itemObj = JSONObject()
        itemObj.put("product_id", item.product_id)
        itemObj.put("img", item.img)
        itemObj.put("name", item.name)
        itemObj.put("price", item.price)
        itemObj.put("number", item.number)

        jsonArray.put(itemObj)
    }

    jsonObject.put("items", jsonArray)

    Log.e("Order",jsonArray .toString())


    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/CreateOrder") // edit1
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
            val json = Json.decodeFromString<OrderResponse>(inputStream) // edit3
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