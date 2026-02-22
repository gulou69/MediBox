package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import com.example.myapplication.repository.MedicineRepository
import com.example.myapplication.mqtt.MqttManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MedicineRepository(application)
    private val mqttManager = MqttManager(application)
    
    // 药品信息
    val medicineInfo = repository.medicineInfo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MedicineInfo()
    )
    
    // 服药记录
    val medicationRecords = repository.medicationRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 环境数据
    val environmentData = mqttManager.environmentData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    // MQTT连接状态
    val isConnected = mqttManager.isConnected.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    // 编辑模式状态
    private val _isEditingMedicine = MutableStateFlow(false)
    val isEditingMedicine = _isEditingMedicine.asStateFlow()
    
    // 重连状态
    private val _isReconnecting = MutableStateFlow(false)
    val isReconnecting = _isReconnecting.asStateFlow()
    
    // 选择的日期
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    
    // 当前选择日期的服药记录
    val selectedDateRecords = combine(medicationRecords, selectedDate) { records, date ->
        records.filter { it.date == date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 当前选择日期的完整服药记录（包括默认时间）
    val selectedDateRecordsWithDefaults = combine(
        selectedDate, 
        medicationRecords, 
        medicineInfo
    ) { date, records, info ->
        val existingRecords = records.filter { it.date == date }
        info.medicationTimes.map { time ->
            existingRecords.find { it.time == time } 
                ?: MedicationRecord(date, time, false)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 环境状态
    val environmentStatus = combine(environmentData, medicineInfo) { envData, info ->
        if (envData == null) return@combine EnvironmentStatus.OPTIMAL
        
        val tempOk = envData.temperature >= info.temperatureRange.min && 
                     envData.temperature <= info.temperatureRange.max
        val humidityOk = envData.humidity >= info.humidityRange.min && 
                         envData.humidity <= info.humidityRange.max
        
        when {
            tempOk && humidityOk -> EnvironmentStatus.OPTIMAL
            !tempOk || !humidityOk -> EnvironmentStatus.WARNING
            else -> EnvironmentStatus.CRITICAL
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EnvironmentStatus.OPTIMAL
    )
    
    init {
        // 启动时连接MQTT
        connectMqtt()
        
        // 监听药品信息变化，更新下次服药时间
        viewModelScope.launch {
            medicineInfo.collect { info ->
                updateNextMedicationTime()
            }
        }
    }
    
    // 保存药品信息
    fun saveMedicineInfo(medicineInfo: MedicineInfo) {
        viewModelScope.launch {
            repository.saveMedicineInfo(medicineInfo)
            _isEditingMedicine.value = false
            updateNextMedicationTime()
        }
    }
    
    // 切换编辑模式
    fun toggleEditMode() {
        _isEditingMedicine.value = !_isEditingMedicine.value
    }
    
    // 标记服药状态
    fun markMedicationTaken(date: LocalDate, time: LocalTime, isTaken: Boolean) {
        viewModelScope.launch {
            repository.markMedicationTaken(date, time, isTaken)
            updateNextMedicationTime()
        }
    }
    
    // 选择日期
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    // 更新下次服药时间
    private suspend fun updateNextMedicationTime() {
        val nextTime = repository.getNextMedicationTime()
        mqttManager.publishNextMedicationTime(nextTime)
    }
    
    // 连接MQTT
    fun connectMqtt() {
        mqttManager.connect()
    }
    
    // 断开MQTT连接
    fun disconnectMqtt() {
        mqttManager.disconnect()
    }
    
    // 重连MQTT
    fun reconnectMqtt() {
        _isReconnecting.value = true
        mqttManager.reconnect()
        // 3秒后重置重连状态
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _isReconnecting.value = false
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mqttManager.disconnect()
    }
} 