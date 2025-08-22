package com.visgupta.binderserver

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.visgupta.binderserver.ui.theme.BinderServerTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    
    private var serviceStarted by mutableStateOf(false)
    private var interactionLogs by mutableStateOf(listOf<InteractionLog>())
    private var connectedClients by mutableStateOf(0)
    private var totalOperations by mutableStateOf(0)
    
    // Data class for interaction logs
    data class InteractionLog(
        val timestamp: String,
        val type: LogType,
        val clientPid: Int,
        val operation: String,
        val details: String
    )
    
    enum class LogType {
        CONNECTION, OPERATION, CALLBACK, ERROR, INFO
    }
    
    companion object {
        private var instance: MainActivity? = null
        
        fun addLog(type: LogType, clientPid: Int, operation: String, details: String) {
            instance?.addInteractionLog(type, clientPid, operation, details)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        enableEdgeToEdge()
        
        // Add initial log
        addInteractionLog(LogType.INFO, Process.myPid(), "SERVER_START", "Binder Server App started")
        
        setContent {
            BinderServerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { 
                                Text(
                                    "Binder Server App",
                                    fontWeight = FontWeight.Bold
                                ) 
                            }
                        )
                    }
                ) { innerPadding ->
                    ServerUI(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
    
    private fun addInteractionLog(type: LogType, clientPid: Int, operation: String, details: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val log = InteractionLog(timestamp, type, clientPid, operation, details)
        
        interactionLogs = (interactionLogs + log).takeLast(100) // Keep last 100 logs
        
        if (type == LogType.OPERATION) {
            totalOperations++
        }
        
        Log.d("ServerUI", "[$timestamp] $operation: $details (Client PID: $clientPid)")
    }
    
    fun updateClientCount(count: Int) {
        connectedClients = count
    }
    
    @Composable
    fun ServerUI(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // Server Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🖥️ Server Status",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Package: com.visgupta.binderserver\n" +
                                "PID: ${Process.myPid()}\n" +
                                "UID: ${Process.myUid()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Service: ${if (serviceStarted) "🟢 RUNNING" else "🔴 STOPPED"}\n" +
                                "Clients: $connectedClients\n" +
                                "Operations: $totalOperations",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
            
            // Service Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🔧 Service Control",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (serviceStarted) {
                                    stopService(Intent(this@MainActivity, BinderTestService::class.java))
                                    serviceStarted = false
                                    addInteractionLog(LogType.INFO, Process.myPid(), "SERVICE_STOP", "Binder service stopped")
                                } else {
                                    startService(Intent(this@MainActivity, BinderTestService::class.java))
                                    serviceStarted = true
                                    addInteractionLog(LogType.INFO, Process.myPid(), "SERVICE_START", "Binder service started")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (serviceStarted) "Stop Service" else "Start Service",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Button(
                            onClick = {
                                interactionLogs = listOf()
                                totalOperations = 0
                                addInteractionLog(LogType.INFO, Process.myPid(), "LOGS_CLEARED", "Interaction logs cleared")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Logs")
                        }
                    }
                }
            }
            
            // Real-time Interaction Logs
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "📊 Live IPC Interactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (interactionLogs.isEmpty()) {
                        Text(
                            "No interactions yet. Start the service and connect from client app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            reverseLayout = true // Show newest logs at top
                        ) {
                            items(interactionLogs.reversed()) { log ->
                                LogItem(log = log)
                            }
                        }
                    }
                }
            }
            
            // Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "📱 Instructions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        "1. Start the Binder Service above\n" +
                        "2. Install and launch the Client App\n" +
                        "3. Watch real-time IPC interactions in logs\n" +
                        "4. Monitor client connections and operations",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    @Composable
    fun LogItem(log: InteractionLog) {
        val backgroundColor = when (log.type) {
            LogType.CONNECTION -> Color(0xFFE3F2FD)
            LogType.OPERATION -> Color(0xFFE8F5E8)
            LogType.CALLBACK -> Color(0xFFFFF3E0)
            LogType.ERROR -> Color(0xFFFFEBEE)
            LogType.INFO -> Color(0xFFF3E5F5)
        }
        
        val textColor = when (log.type) {
            LogType.CONNECTION -> Color(0xFF1976D2)
            LogType.OPERATION -> Color(0xFF388E3C)
            LogType.CALLBACK -> Color(0xFFF57C00)
            LogType.ERROR -> Color(0xFFD32F2F)
            LogType.INFO -> Color(0xFF7B1FA2)
        }
        
        val icon = when (log.type) {
            LogType.CONNECTION -> "🔗"
            LogType.OPERATION -> "⚙️"
            LogType.CALLBACK -> "📞"
            LogType.ERROR -> "❌"
            LogType.INFO -> "ℹ️"
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$icon ${log.operation}",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 14.sp
                    )
                    Text(
                        log.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                Text(
                    log.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f)
                )
                
                if (log.clientPid != Process.myPid()) {
                    Text(
                        "Client PID: ${log.clientPid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.6f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}
