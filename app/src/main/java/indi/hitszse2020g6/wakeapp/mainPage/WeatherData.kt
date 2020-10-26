package indi.hitszse2020g6.wakeapp.mainPage

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object WeatherData {
    var temperature: Double = 0.0
    var weatherID: Int = -1
    var weatherDesc: String = ""
    var weatherIcon: String = ""

    fun updateWeather(){
        val result = (URL("https://api.openweathermap.org/data/2.5/weather?q=shenzhen&lang=zh_cn&units=metric&appid=671f71decbd92d6dfa83b477e77af41d&mode=xml").openConnection() as? HttpsURLConnection)?.run {
            readTimeout = 10000
            connectTimeout = 15000
            requestMethod = "GET"
            doInput = true
            // Starts the query
            connect()
            inputStream
        }
        if(result != null) {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(result, null)
            var depth = 0
            do {
                val n = parser.next()
                if(n == XmlPullParser.START_TAG) {
                    depth++
                } else if(n == XmlPullParser.END_TAG) {
                    depth--
                    continue
                }
                if(parser.name == null) {
                    continue
                }
                when (parser.name) {
                    "weather" -> {
                        parser.getAttributeValue(null, "number")?.let { weatherID = it.toInt() }
                        parser.getAttributeValue(null, "icon")?.let { weatherIcon = it }
                        parser.getAttributeValue(null, "value")?.let { weatherDesc = it }
                    }
                    "temperature" -> {
                        parser.getAttributeValue(null, "value")?.let { temperature = it.toDouble() }
                    }
                }
            } while (depth != 0)
        }
    }
}