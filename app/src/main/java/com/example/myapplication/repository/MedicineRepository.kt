package com.example.myapplication.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.MedicationRecord
import com.example.myapplication.data.MedicineInfo
import com.example.myapplication.data.TemperatureRange
import com.example.myapplication.data.HumidityRange
import com.example.myapplication.util.createGson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medicine_settings")

class MedicineRepository(private val context: Context) {
    private val gson = createGson()
    
    companion object {
        private val MEDICINE_INFO_KEY = stringPreferencesKey("medicine_info")
        private val MEDICATION_RECORDS_KEY = stringPreferencesKey("medication_records")
    }
    
    // 获取药品信息
    val medicineInfo: Flow<MedicineInfo> = context.dataStore.data.map { preferences ->
        val json = preferences[MEDICINE_INFO_KEY]
        if (json != null) {
            gson.fromJson(json, MedicineInfo::class.java)
        } else {
            MedicineInfo() // 默认值
        }
    }
    
    // 保存药品信息
    suspend fun saveMedicineInfo(medicineInfo: MedicineInfo) {
        context.dataStore.edit { preferences ->
            preferences[MEDICINE_INFO_KEY] = gson.toJson(medicineInfo)
        }
    }
    
    // 获取服药记录
    val medicationRecords: Flow<List<MedicationRecord>> = context.dataStore.data.map { preferences ->
        val json = preferences[MEDICATION_RECORDS_KEY]
        if (json != null) {
            val type = object : TypeToken<List<MedicationRecord>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    
    // 保存服药记录
    suspend fun saveMedicationRecords(records: List<MedicationRecord>) {
        context.dataStore.edit { preferences ->
            preferences[MEDICATION_RECORDS_KEY] = gson.toJson(records)
        }
    }
    
    // 获取药品信息（suspend版本）
    suspend fun getMedicineInfo(): MedicineInfo {
        return medicineInfo.first()
    }
    
    // 获取服药记录（suspend版本）
    suspend fun getMedicationRecords(): List<MedicationRecord> {
        return medicationRecords.first()
    }
    
    // 标记服药状态
    suspend fun markMedicationTaken(date: LocalDate, time: LocalTime, isTaken: Boolean) {
        val currentRecords = getMedicationRecords().toMutableList()
        
        val existingRecordIndex = currentRecords.indexOfFirst { record ->
            record.date == date && record.time == time 
        }
        
        if (existingRecordIndex >= 0) {
            currentRecords[existingRecordIndex] = currentRecords[existingRecordIndex].copy(taken = isTaken)
        } else {
            currentRecords.add(MedicationRecord(date, time, isTaken))
        }
        
        saveMedicationRecords(currentRecords)
    }
    
    // 获取某天的服药记录
    suspend fun getRecordsForDate(date: LocalDate): List<MedicationRecord> {
        return getMedicationRecords().filter { it.date == date }
    }
    
    // 获取下次服药时间
    suspend fun getNextMedicationTime(): LocalTime? {
        val medicineInfo = getMedicineInfo()
        val today = LocalDate.now()
        val todayRecords = getMedicationRecords().filter { it.date == today }
        
        return medicineInfo.medicationTimes.firstOrNull { time ->
            todayRecords.none { record -> 
                record.time == time && record.taken 
            }
        }
    }
    
    // 获取今天的服药记录
    suspend fun getTodayMedicationRecords(): List<MedicationRecord> {
        val today = LocalDate.now()
        return getMedicationRecords().filter { record -> 
            record.date == today 
        }
    }
    
    // 获取指定日期的服药记录
    suspend fun getMedicationRecordsForDate(date: LocalDate): List<MedicationRecord> {
        return getMedicationRecords().filter { record -> 
            record.date == date 
        }
    }
} 