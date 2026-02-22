package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.MedicineInfo
import com.example.myapplication.data.TemperatureRange
import com.example.myapplication.data.HumidityRange
import com.example.myapplication.viewmodel.MedicineViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineInfoScreen(
    viewModel: MedicineViewModel
) {
    val medicineInfo by viewModel.medicineInfo.collectAsState()
    val isEditing by viewModel.isEditingMedicine.collectAsState()
    
    var name by remember(medicineInfo) { mutableStateOf(medicineInfo.name) }
    var dosage by remember(medicineInfo) { mutableStateOf(medicineInfo.dosage) }
    var tempMin by remember(medicineInfo) { mutableStateOf(medicineInfo.temperatureRange.min.toString()) }
    var tempMax by remember(medicineInfo) { mutableStateOf(medicineInfo.temperatureRange.max.toString()) }
    var humidityMin by remember(medicineInfo) { mutableStateOf(medicineInfo.humidityRange.min.toString()) }
    var humidityMax by remember(medicineInfo) { mutableStateOf(medicineInfo.humidityRange.max.toString()) }
    
    var time1 by remember(medicineInfo) { 
        mutableStateOf(medicineInfo.medicationTimes.getOrNull(0)?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "08:00") 
    }
    var time2 by remember(medicineInfo) { 
        mutableStateOf(medicineInfo.medicationTimes.getOrNull(1)?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "12:00") 
    }
    var time3 by remember(medicineInfo) { 
        mutableStateOf(medicineInfo.medicationTimes.getOrNull(2)?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "20:00") 
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "药品信息",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Button(
                        onClick = {
                            if (isEditing) {
                                // 保存信息
                                try {
                                    val medicationTimes = listOf(
                                        LocalTime.parse(time1),
                                        LocalTime.parse(time2),
                                        LocalTime.parse(time3)
                                    )
                                    val newInfo = MedicineInfo(
                                        name = name,
                                        dosage = dosage,
                                        medicationTimes = medicationTimes,
                                        temperatureRange = TemperatureRange(
                                            tempMin.toFloat(),
                                            tempMax.toFloat()
                                        ),
                                        humidityRange = HumidityRange(
                                            humidityMin.toFloat(),
                                            humidityMax.toFloat()
                                        )
                                    )
                                    viewModel.saveMedicineInfo(newInfo)
                                } catch (e: Exception) {
                                    // 可以添加错误处理
                                }
                            } else {
                                viewModel.toggleEditMode()
                            }
                        }
                    ) {
                        Text(
                            text = if (isEditing) "保存" else "编辑"
                        )
                    }
                }

                if (isEditing) {
                    // 编辑模式
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("药品名称") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("剂量") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "服药时间",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = time1,
                            onValueChange = { time1 = it },
                            label = { Text("早上") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("08:00") }
                        )
                        OutlinedTextField(
                            value = time2,
                            onValueChange = { time2 = it },
                            label = { Text("中午") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("12:00") }
                        )
                        OutlinedTextField(
                            value = time3,
                            onValueChange = { time3 = it },
                            label = { Text("晚上") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("20:00") }
                        )
                    }

                    Text(
                        text = "温度范围",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempMin,
                            onValueChange = { tempMin = it },
                            label = { Text("最低温度(°C)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = tempMax,
                            onValueChange = { tempMax = it },
                            label = { Text("最高温度(°C)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Text(
                        text = "湿度范围",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = humidityMin,
                            onValueChange = { humidityMin = it },
                            label = { Text("最低湿度(%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = humidityMax,
                            onValueChange = { humidityMax = it },
                            label = { Text("最高湿度(%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                } else {
                    // 显示模式
                    InfoDisplayItem("药品名称", medicineInfo.name)
                    InfoDisplayItem("剂量", medicineInfo.dosage)
                    
                    Text(
                        text = "服药时间",
                        style = MaterialTheme.typography.titleMedium
                    )
                    medicineInfo.medicationTimes.forEachIndexed { index, time ->
                        val timeLabel = when (index) {
                            0 -> "早上"
                            1 -> "中午"
                            2 -> "晚上"
                            else -> "时间${index + 1}"
                        }
                        InfoDisplayItem(timeLabel, time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    }
                    
                    InfoDisplayItem(
                        "温度范围", 
                        "${medicineInfo.temperatureRange.min}°C - ${medicineInfo.temperatureRange.max}°C"
                    )
                    InfoDisplayItem(
                        "湿度范围", 
                        "${medicineInfo.humidityRange.min}% - ${medicineInfo.humidityRange.max}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoDisplayItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 