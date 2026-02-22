package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.EnvironmentStatus
import com.example.myapplication.viewmodel.MedicineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentMonitorScreen(
    viewModel: MedicineViewModel
) {
    val environmentData by viewModel.environmentData.collectAsState()
    val environmentStatus by viewModel.environmentStatus.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isReconnecting by viewModel.isReconnecting.collectAsState()
    val medicineInfo by viewModel.medicineInfo.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "环境监控",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        // 连接状态卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isReconnecting -> Color(0xFFF3E5F5)
                    isConnected -> Color(0xFFE8F5E8)
                    else -> Color(0xFFFFF3E0)
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isReconnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF9C27B0)
                    )
                } else {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isReconnecting -> "正在重连MQTT..."
                        isConnected -> "MQTT已连接"
                        else -> "MQTT未连接"
                    },
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                // 刷新按钮
                IconButton(
                    onClick = { viewModel.reconnectMqtt() },
                    enabled = !isReconnecting
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新连接",
                        tint = when {
                            isReconnecting -> Color.Gray
                            isConnected -> Color(0xFF4CAF50)
                            else -> Color(0xFFFF9800)
                        }
                    )
                }
            }
        }
        
        // 环境数据展示逻辑
        when {
            !isConnected -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "未连接MQTT服务器",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "请点击上方刷新按钮尝试重新连接",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            isConnected && environmentData == null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("等待环境数据...", color = Color.Gray)
                    }
                }
            }
            isConnected && environmentData != null -> {
                // 环境数据卡片
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 温度卡片
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "温度",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "温度",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${environmentData!!.temperature}°C",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // 湿度卡片
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "湿度",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "湿度",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${environmentData!!.humidity}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // 环境状态卡片和适宜范围信息
        if (isConnected && environmentData != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (environmentStatus) {
                        EnvironmentStatus.OPTIMAL -> Color(0xFFE8F5E8)
                        EnvironmentStatus.WARNING -> Color(0xFFFFF3E0)
                        EnvironmentStatus.CRITICAL -> Color(0xFFFFEBEE)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (environmentStatus) {
                                EnvironmentStatus.OPTIMAL -> Icons.Default.CheckCircle
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = when (environmentStatus) {
                                EnvironmentStatus.OPTIMAL -> Color(0xFF4CAF50)
                                EnvironmentStatus.WARNING -> Color(0xFFFF9800)
                                EnvironmentStatus.CRITICAL -> Color(0xFFF44336)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (environmentStatus) {
                                EnvironmentStatus.OPTIMAL -> "环境状态良好"
                                EnvironmentStatus.WARNING -> "环境状态警告"
                                EnvironmentStatus.CRITICAL -> "环境状态危险"
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (environmentStatus != EnvironmentStatus.OPTIMAL) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (environmentStatus) {
                                EnvironmentStatus.WARNING -> "环境条件不在最佳范围内，请注意药品保存"
                                EnvironmentStatus.CRITICAL -> "环境条件严重偏离适宜范围，可能影响药品效果"
                                else -> ""
                            },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // 适宜范围信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "适宜保存条件",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "温度: ${medicineInfo.temperatureRange.min}~${medicineInfo.temperatureRange.max}°C",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "湿度: ${medicineInfo.humidityRange.min}~${medicineInfo.humidityRange.max}%",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

 