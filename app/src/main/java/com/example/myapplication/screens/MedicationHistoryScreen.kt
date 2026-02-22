package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.MedicationRecord
import com.example.myapplication.data.MedicineInfo
import com.example.myapplication.viewmodel.MedicineViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun MedicationHistoryScreen(
    viewModel: MedicineViewModel
) {
    val medicineInfo by viewModel.medicineInfo.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showDayDetails by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 日历视图
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 月份导航
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { currentMonth = currentMonth.minusMonths(1) }
                    ) {
                        Text("上月")
                    }
                    
                    Text(
                        text = "${currentMonth.year}年${currentMonth.monthValue}月",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = { currentMonth = currentMonth.plusMonths(1) }
                    ) {
                        Text("下月")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 星期标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // 日历网格
                val firstDay = currentMonth.atDay(1)
                val startDayOfWeek = firstDay.dayOfWeek.value
                val daysInMonth = currentMonth.lengthOfMonth()
                val totalCells = if ((startDayOfWeek + daysInMonth - 1) % 7 == 0) {
                    startDayOfWeek + daysInMonth - 1
                } else {
                    ((startDayOfWeek + daysInMonth - 1) / 7 + 1) * 7
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(totalCells) { index ->
                        val day = index - startDayOfWeek + 2
                        if (day in 1..daysInMonth) {
                            val date = currentMonth.atDay(day)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.secondary
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        viewModel.selectDate(date)
                                        showDayDetails = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onSecondary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 选中日期的详细信息
        if (showDayDetails) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "${selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))} 服药记录",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val records by viewModel.selectedDateRecordsWithDefaults.collectAsState()
                    
                    records.forEach { record ->
                        MedicationTimeRow(
                            time = record.time,
                            isTaken = record.taken,
                            dosage = medicineInfo.dosage,
                            onStatusChange = { newStatus ->
                                viewModel.markMedicationTaken(selectedDate, record.time, newStatus)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationTimeRow(
    time: LocalTime,
    isTaken: Boolean,
    dosage: String,
    onStatusChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "剂量: $dosage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isTaken,
                onCheckedChange = onStatusChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
} 