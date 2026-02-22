package com.example.myapplication.data

import java.time.LocalDate
import java.time.LocalTime

// 药品信息数据模型
data class MedicineInfo(
    val name: String = "默认药品",
    val dosage: String = "1粒",
    val medicationTimes: List<LocalTime> = listOf(
        LocalTime.of(8, 0),
        LocalTime.of(12, 0),
        LocalTime.of(20, 0)
    ),
    val temperatureRange: TemperatureRange = TemperatureRange(30f, 50f),
    val humidityRange: HumidityRange = HumidityRange(0f, 20f)
)

// 温度范围
data class TemperatureRange(
    val min: Float,
    val max: Float
)

// 湿度范围
data class HumidityRange(
    val min: Float,
    val max: Float
)

// 服药记录
data class MedicationRecord(
    val date: LocalDate,
    val time: LocalTime,
    val taken: Boolean = false
)

// 环境数据
data class EnvironmentData(
    val temperature: Float,
    val humidity: Float,
    val timestamp: Long = System.currentTimeMillis()
)

// 环境状态
enum class EnvironmentStatus {
    OPTIMAL,
    WARNING,
    CRITICAL
} 