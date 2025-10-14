package com.example.team_voida.ProductInfo

import android.util.Log
import com.example.team_voida.Home.Popular
import com.example.team_voida.Payment.PaymentInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


@Serializable
data class ProductInfoInfo(
    val product_id: Int,
    val name: String,
    val image_url: String,
    val price: Float,
    val ai_info: String,
    val ai_review: String
)

@Serializable
data class ReviewInfo(
    val ai_review: String
)

suspend fun ProductInfoServer(
    url: String,
    product_id: Int
): ProductInfoInfo?{

    val jsonObject = JSONObject()
    jsonObject.put("product_id", product_id)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(url) // edit1
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
            val json = Json.decodeFromString<ProductInfoInfo>(inputStream) // edit3
            return json
        } else {
            Log.e("xxx","wrong!")
            return  null
        }
    } catch (e: Exception) {
        Log.e("xxx","what!")

        e.printStackTrace()
        return  null
    }
}

suspend fun ProductDetailedInfoServer(
    url: String,
    product_id: Int
): ProductInfoInfo?{

    val jsonObject = JSONObject()
    jsonObject.put("product_id", product_id)

    var result: String? = ""
    val jsonObjectString = jsonObject.toString()

    try {
        val url = URL(url) // edit1
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
            val json = Json.decodeFromString<ProductInfoInfo>(inputStream) // edit3
            return json
        } else {
            Log.e("xxx","wrong!")
            return  null
        }
    } catch (e: Exception) {
        Log.e("xxx","what!")

        e.printStackTrace()
        return  null
    }
}

suspend fun ReviewInfoServer(
    product_id: Int,
    isWhichItem: Int
): ReviewInfo?{
    val jsonObject = JSONObject()
    jsonObject.put("product_id", product_id)

    val jsonObjectString = jsonObject.toString()

    var url = ""

    when(isWhichItem){
        1 -> url = "/Popular"
        2 -> url = "/BigSale"
        3 -> url = "/TodaySale"
        4 -> url = "/New"
        else -> url = ""
    }

    try {
        val url = URL(" https://fluent-marmoset-immensely.ngrok-free.app/Review${url}") // edit1
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
            val json = Json.decodeFromString<ReviewInfo?>(inputStream) // edit3
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