# MediBox -- 基于 MQTT 的智能药品管理 Android 应用

## 项目简介

MediBox 是一款基于 Android 平台的智能药品管理应用，通过 MQTT 协议与物联网传感器通信，实现药品存储环境的实时监控和服药提醒。该应用采用 Jetpack Compose 构建现代化 UI，适用于需要精确管控药品保存条件的场景（如家庭药箱、小型药房等），同时支持与送药小车等硬件设备联动。

## 功能概述

### 药品信息管理

- 录入和编辑药品名称、剂量
- 设置每日服药时间（支持多个时间点，如早/中/晚）
- 配置药品适宜保存温度范围和湿度范围

### 服药记录追踪

- 按月日历视图查看服药历史
- 选择日期查看当天各时间段的服药状态
- 手动标记已服药/未服药

### 环境实时监控

- 通过 MQTT 协议接收传感器上报的温湿度数据
- 将实时数据与药品适宜保存范围进行比对
- 以三级状态（正常/警告/危险）直观展示环境情况
- 显示 MQTT 连接状态和温湿度适宜范围

### MQTT 通信

- 订阅环境传感器数据和小车状态主题
- 发布下次服药时间、服药提醒和小车控制指令
- 支持自动重连机制

## 技术架构

| 层级 | 技术 |
|------|------|
| UI 框架 | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 状态管理 | ViewModel + StateFlow |
| 数据持久化 | DataStore Preferences |
| 网络通信 | HiveMQ MQTT Client |
| 序列化 | Gson（含 LocalTime/LocalDate 自定义适配器） |
| 语言 | Kotlin |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 14 (API 34) |

## 项目结构

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt               # 应用入口，NavHost 和底部导航栏
├── config/
│   └── MqttConfig.kt             # MQTT 服务器地址、Topic、QoS 等集中配置
├── data/
│   └── MedicineData.kt           # 数据模型（药品信息、服药记录、环境数据等）
├── mqtt/
│   └── MqttManager.kt            # MQTT 连接、订阅、发布、重连管理
├── navigation/
│   └── Screen.kt                 # 导航路由定义
├── repository/
│   └── MedicineRepository.kt     # 基于 DataStore 的数据读写仓库
├── screens/
│   ├── MedicineInfoScreen.kt     # 药品信息编辑页面
│   ├── MedicationHistoryScreen.kt# 服药记录日历页面
│   └── EnvironmentMonitorScreen.kt# 环境监控页面
├── ui/theme/
│   ├── Color.kt                  # 颜色定义
│   ├── Theme.kt                  # Material 3 主题配置
│   └── Type.kt                   # 排版样式
├── util/
│   └── GsonUtils.kt              # Gson 自定义序列化适配器
└── viewmodel/
    └── MedicineViewModel.kt      # 核心 ViewModel，桥接 UI 与数据层
```

## MQTT Topic 说明

| Topic | 方向 | 用途 |
|-------|------|------|
| `medicine/environment` | 订阅 | 接收传感器温湿度数据 |
| `medicine/car/status` | 订阅 | 接收送药小车状态 |
| `medicine/next_time` | 发布 | 发送下次服药时间 |
| `medicine/reminder` | 发布 | 发送服药提醒 |
| `medicine/car/control` | 发布 | 发送小车控制指令 |

## 配置说明

MQTT 服务器地址默认为 `192.168.1.53:1883`（局域网），如需修改请编辑 `MqttConfig.kt` 中的 `SERVER_HOST` 和 `SERVER_PORT`。

## 构建与运行

1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 根据实际环境修改 `MqttConfig.kt` 中的 MQTT 服务器地址
4. 确保 MQTT Broker 已启动并可访问
5. 连接 Android 设备或启动模拟器
6. 点击 Run 构建并安装应用

## 依赖版本

- Android Gradle Plugin: 8.4.1
- Kotlin: 1.9.0
- Compose BOM: 2023.08.00
- HiveMQ MQTT Client: 1.3.3
- Gson: 2.10.1
- Navigation Compose: 2.7.1
- DataStore: 1.0.0

## 许可证

MIT License
