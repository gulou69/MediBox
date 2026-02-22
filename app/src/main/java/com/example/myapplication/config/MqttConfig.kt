package com.example.myapplication.config

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * MQTT配置管理
 * 集中管理所有MQTT相关的配置参数
 */
object MqttConfig {
    
    // ================== 服务器配置 ==================
    
    /** MQTT服务器地址 */
    const val SERVER_HOST = "192.168.1.53"
    
    /** MQTT服务器端口 */
    const val SERVER_PORT = 1883
    
    /** WebSocket端口（如果需要） */
    const val WEBSOCKET_PORT = 8083
    
    /** 是否使用SSL/TLS */
    const val USE_SSL = false
    
    /** SSL端口（如果使用SSL） */
    const val SSL_PORT = 8883
    
    // ================== 客户端配置 ==================
    
    /** 客户端ID前缀 */
    const val CLIENT_ID_PREFIX = "medicine_app"
    
    /** 连接超时时间（秒） */
    const val CONNECTION_TIMEOUT = 30
    
    /** 保活间隔（秒） */
    const val KEEP_ALIVE_INTERVAL = 60
    
    /** 是否自动重连 */
    const val AUTO_RECONNECT = true
    
    /** 重连间隔（毫秒） */
    const val RECONNECT_DELAY = 5000L
    
    // ================== Topic配置 ==================
    
    /** 环境数据订阅主题 */
    const val ENVIRONMENT_SUBSCRIBE_TOPIC = "medicine/environment"
    
    /** 下次服药时间发布主题 */
    const val NEXT_MEDICATION_PUBLISH_TOPIC = "medicine/next_time"
    
    /** 服药提醒主题 */
    const val MEDICATION_REMINDER_TOPIC = "medicine/reminder"
    
    /** 系统状态主题 */
    const val SYSTEM_STATUS_TOPIC = "medicine/status"
    
    /** 小车控制主题 */
    const val CART_CONTROL_TOPIC = "medicine/cart/control"
    
    /** 小车状态反馈主题 */
    const val CART_STATUS_TOPIC = "medicine/cart/status"
    
    // ================== QoS配置 ==================
    
    /** 默认QoS等级 */
    const val DEFAULT_QOS = 1
    
    /** 环境数据QoS */
    const val ENVIRONMENT_QOS = 1
    
    /** 控制命令QoS */
    const val CONTROL_QOS = 2
    
    // ================== 数据格式配置 ==================
    
    /** 环境数据分隔符 */
    const val DATA_SEPARATOR = ","
    
    /** 键值对分隔符 */
    const val KEY_VALUE_SEPARATOR = ":"
    
    /** 时间格式 */
    const val TIME_FORMAT = "HH:mm"
    
    /** 日期时间格式 */
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    
    // ================== 消息模板 ==================
    
    /** 环境数据格式：temperature:25.5,humidity:60.2 */
    fun formatEnvironmentData(temperature: Float, humidity: Float): String {
        return "temperature$KEY_VALUE_SEPARATOR$temperature${DATA_SEPARATOR}humidity$KEY_VALUE_SEPARATOR$humidity"
    }
    
    /** 下次服药时间格式：next_medication_time:08:30 */
    fun formatNextMedicationTime(time: String): String {
        return "next_medication_time$KEY_VALUE_SEPARATOR$time"
    }
    
    /** 服药提醒格式：reminder:药名,剂量,时间 */
    fun formatMedicationReminder(medicineName: String, dosage: String, time: String): String {
        return "reminder$KEY_VALUE_SEPARATOR$medicineName${DATA_SEPARATOR}$dosage${DATA_SEPARATOR}$time"
    }
    
    /** 小车控制命令格式：command:move_to_location,location:bedroom */
    fun formatCartCommand(command: String, location: String): String {
        return "command$KEY_VALUE_SEPARATOR$command${DATA_SEPARATOR}location$KEY_VALUE_SEPARATOR$location"
    }
    
    // ================== 解析方法 ==================
    
    /** 解析环境数据 - 支持JSON格式和键值对格式 */
    fun parseEnvironmentData(message: String): Pair<Float, Float>? {
        return try {
            // 检测是否是JSON格式
            if (message.trim().startsWith("{") && message.trim().endsWith("}")) {
                // JSON格式解析
                val gson = Gson()
                val jsonObject = gson.fromJson(message, JsonObject::class.java)
                
                val temperature = jsonObject.get("temperature")?.asString?.toFloat()
                val humidity = jsonObject.get("humidity")?.asString?.toFloat()
                
                if (temperature != null && humidity != null) {
                    Pair(temperature, humidity)
                } else null
            } else {
                // 原有的键值对格式解析
                val parts = message.split(DATA_SEPARATOR)
                val tempPart = parts.find { it.startsWith("temperature$KEY_VALUE_SEPARATOR") }
                val humidityPart = parts.find { it.startsWith("humidity$KEY_VALUE_SEPARATOR") }
                
                val temperature = tempPart?.split(KEY_VALUE_SEPARATOR)?.get(1)?.toFloat()
                val humidity = humidityPart?.split(KEY_VALUE_SEPARATOR)?.get(1)?.toFloat()
                
                if (temperature != null && humidity != null) {
                    Pair(temperature, humidity)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /** 解析小车状态 */
    fun parseCartStatus(message: String): Map<String, String> {
        return try {
            message.split(DATA_SEPARATOR).associate { part ->
                val keyValue = part.split(KEY_VALUE_SEPARATOR)
                if (keyValue.size == 2) {
                    keyValue[0] to keyValue[1]
                } else {
                    part to ""
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    // ================== 完整服务器URI ==================
    
    /** 获取完整的MQTT服务器URI */
    fun getServerUri(): String {
        val protocol = if (USE_SSL) "ssl" else "tcp"
        val port = if (USE_SSL) SSL_PORT else SERVER_PORT
        return "$protocol://$SERVER_HOST:$port"
    }
    
    /** 获取WebSocket URI（如果需要） */
    fun getWebSocketUri(): String {
        val protocol = if (USE_SSL) "wss" else "ws"
        val port = if (USE_SSL) SSL_PORT else SERVER_PORT
        return "$protocol://$SERVER_HOST:$port/mqtt"
    }

} 