package com.example.myapplication.mqtt

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.myapplication.config.MqttConfig
import com.example.myapplication.data.EnvironmentData
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.time.LocalTime
import java.util.UUID

class MqttManager(private val context: Context) {
    private var mqttClient: Mqtt3AsyncClient? = null
    
    // 使用配置文件中的参数
    private val clientId = "${MqttConfig.CLIENT_ID_PREFIX}_${UUID.randomUUID()}"
    
    // MQTT连接状态
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    // 环境数据
    private val _environmentData = MutableStateFlow<EnvironmentData?>(null)
    val environmentData: StateFlow<EnvironmentData?> = _environmentData.asStateFlow()
    
    companion object {
        private const val TAG = "MqttManager"
    }
    
    init {
        // MQTT管理器初始化完成
        Log.d(TAG, "MQTT管理器初始化完成")
    }
    
    /**
     * 检查网络连接状态
     */
    fun checkNetworkConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * 连接到MQTT服务器
     */
    fun connect() {
        try {
            // 检查网络连接
            if (!checkNetworkConnection()) {
                Log.e(TAG, "❌ 网络连接不可用")
                _isConnected.value = false
                return
            }
            
            Log.i(TAG, "✅ 网络连接正常")
            Log.i(TAG, "开始连接MQTT服务器...")
            Log.i(TAG, "服务器地址: ${MqttConfig.SERVER_HOST}:${MqttConfig.SERVER_PORT}")
            Log.i(TAG, "客户端ID: $clientId")
            
            mqttClient = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(MqttConfig.SERVER_HOST)
                .serverPort(MqttConfig.SERVER_PORT)
                .identifier(clientId)
                .buildAsync()
            
            mqttClient?.connect()?.whenComplete { connAck: Mqtt3ConnAck?, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "❌ 连接失败: ${throwable.message}", throwable)
                    _isConnected.value = false
                } else {
                    Log.i(TAG, "✅ MQTT连接成功！")
                    Log.i(TAG, "连接确认: $connAck")
                    _isConnected.value = true
                    subscribeToTopics()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "连接异常: ${e.message}", e)
            _isConnected.value = false
        }
    }
    
    /**
     * 订阅相关主题
     */
    private fun subscribeToTopics() {
        Log.i(TAG, "开始订阅主题...")
        
        // 订阅环境数据主题
        val environmentTopic = MqttConfig.ENVIRONMENT_SUBSCRIBE_TOPIC
        Log.i(TAG, "订阅环境数据主题: $environmentTopic")
        
        mqttClient?.subscribeWith()
            ?.topicFilter(environmentTopic)
            ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(0)!!)
            ?.callback { publish ->
                Log.d(TAG, "收到环境主题消息回调")
                handleEnvironmentMessage(publish)
            }
            ?.send()
            ?.whenComplete { subAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "订阅环境数据主题失败: ${throwable.message}", throwable)
                } else {
                    Log.i(TAG, "✅ 成功订阅环境数据主题: $environmentTopic")
                    Log.i(TAG, "订阅确认: $subAck")
                }
            }
        
        // 可以添加其他主题的订阅
        subscribeToCartStatus()
    }
    
    /**
     * 订阅小车状态主题
     */
    private fun subscribeToCartStatus() {
        mqttClient?.subscribeWith()
            ?.topicFilter(MqttConfig.CART_STATUS_TOPIC)
            ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(MqttConfig.DEFAULT_QOS)!!)
            ?.callback { publish ->
                handleCartStatusMessage(publish)
            }
            ?.send()
            ?.whenComplete { subAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "订阅小车状态主题失败: ${throwable.message}", throwable)
                } else {
                    Log.i(TAG, "成功订阅小车状态主题: ${MqttConfig.CART_STATUS_TOPIC}")
                }
            }
    }
    
    /**
     * 处理环境数据消息
     */
    private fun handleEnvironmentMessage(publish: Mqtt3Publish) {
        try {
            val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
            Log.d(TAG, "收到环境数据: $message")
            Log.d(TAG, "消息长度: ${message.length}")
            Log.d(TAG, "消息格式检测: ${if (message.trim().startsWith("{")) "JSON格式" else "键值对格式"}")
            
            // 使用配置文件中的解析方法
            val data = MqttConfig.parseEnvironmentData(message)
            if (data != null) {
                val (temperature, humidity) = data
                _environmentData.value = EnvironmentData(
                    temperature = temperature,
                    humidity = humidity,
                    timestamp = System.currentTimeMillis()
                )
                Log.i(TAG, "环境数据更新成功: 温度=${temperature}°C, 湿度=${humidity}%")
            } else {
                Log.w(TAG, "无法解析环境数据: $message")
                Log.w(TAG, "请检查数据格式是否正确")
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理环境数据失败", e)
        }
    }
    
    /**
     * 处理小车状态消息
     */
    private fun handleCartStatusMessage(publish: Mqtt3Publish) {
        try {
            val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
            Log.d(TAG, "收到小车状态: $message")
            
            val status = MqttConfig.parseCartStatus(message)
            Log.i(TAG, "小车状态: $status")
            // 这里可以添加小车状态的处理逻辑
        } catch (e: Exception) {
            Log.e(TAG, "处理小车状态失败", e)
        }
    }
    
    /**
     * 发布下次服药时间
     */
    fun publishNextMedicationTime(time: LocalTime?) {
        if (time == null) {
            Log.d(TAG, "下次服药时间为空，不发送")
            return
        }
        
        try {
            val timeString = time.format(java.time.format.DateTimeFormatter.ofPattern(MqttConfig.TIME_FORMAT))
            val message = MqttConfig.formatNextMedicationTime(timeString)
            
            mqttClient?.publishWith()
                ?.topic(MqttConfig.NEXT_MEDICATION_PUBLISH_TOPIC)
                ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(MqttConfig.CONTROL_QOS)!!)
                ?.payload(message.toByteArray(StandardCharsets.UTF_8))
                ?.send()
                ?.whenComplete { publishResult, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "发布下次服药时间失败", throwable)
                    } else {
                        Log.i(TAG, "成功发布下次服药时间: $message")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "发布下次服药时间异常", e)
        }
    }
    
    /**
     * 发送服药提醒
     */
    fun publishMedicationReminder(medicineName: String, dosage: String, time: String) {
        try {
            val message = MqttConfig.formatMedicationReminder(medicineName, dosage, time)
            
            mqttClient?.publishWith()
                ?.topic(MqttConfig.MEDICATION_REMINDER_TOPIC)
                ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(MqttConfig.DEFAULT_QOS)!!)
                ?.payload(message.toByteArray(StandardCharsets.UTF_8))
                ?.send()
                ?.whenComplete { publishResult, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "发送服药提醒失败", throwable)
                    } else {
                        Log.i(TAG, "成功发送服药提醒: $message")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "发送服药提醒异常", e)
        }
    }
    
    /**
     * 发送小车控制命令
     */
    fun publishCartCommand(command: String, location: String) {
        try {
            val message = MqttConfig.formatCartCommand(command, location)
            
            mqttClient?.publishWith()
                ?.topic(MqttConfig.CART_CONTROL_TOPIC)
                ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.fromCode(MqttConfig.CONTROL_QOS)!!)
                ?.payload(message.toByteArray(StandardCharsets.UTF_8))
                ?.send()
                ?.whenComplete { publishResult, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "发送小车控制命令失败", throwable)
                    } else {
                        Log.i(TAG, "成功发送小车控制命令: $message")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "发送小车控制命令异常", e)
        }
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        mqttClient?.disconnect()?.whenComplete { _, throwable ->
            if (throwable != null) {
                Log.e(TAG, "断开连接失败", throwable)
            } else {
                Log.i(TAG, "MQTT断开连接成功")
            }
            _isConnected.value = false
            _environmentData.value = null // 断开时清空环境数据
        }
    }
    
    /**
     * 重连MQTT服务器
     */
    fun reconnect() {
        Log.i(TAG, "开始重连MQTT服务器...")
        // 先断开现有连接
        mqttClient?.disconnect()?.whenComplete { _, _ ->
            // 重置状态
            _isConnected.value = false
            _environmentData.value = null
            // 重新连接
            connect()
        } ?: run {
            // 如果没有现有连接，直接连接
            connect()
        }
    }
    
    /**
     * 设置自定义MQTT配置（如果需要动态配置）
     */
    fun setCustomConfig(serverHost: String, serverPort: Int, subscribeTopics: String, publishTopic: String) {
        Log.i(TAG, "注意：当前使用的是配置文件中的固定配置")
        Log.i(TAG, "如需动态配置，请修改 MqttConfig.kt 文件")
        Log.i(TAG, "当前配置 - 服务器: ${MqttConfig.getServerUri()}")
        Log.i(TAG, "环境数据主题: ${MqttConfig.ENVIRONMENT_SUBSCRIBE_TOPIC}")
        Log.i(TAG, "下次服药时间主题: ${MqttConfig.NEXT_MEDICATION_PUBLISH_TOPIC}")
    }
} 