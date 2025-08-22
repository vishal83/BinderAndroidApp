package com.visgupta.example.binderandroidapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.visgupta.example.binderandroidapp.ui.theme.BinderAndroidAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), BinderServiceClient.CallbackListener {
    
    private val TAG = "MainActivity"
    private lateinit var binderClient: BinderServiceClient
    
    // State variables for UI
    private var serviceConnected by mutableStateOf(false)
    private var serviceInfo by mutableStateOf("")
    private var testResults by mutableStateOf(listOf<String>())
    private var isLoading by mutableStateOf(false)
    private var asyncProgress by mutableStateOf(0)
    private var asyncResult by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binderClient = BinderServiceClient(this)
        binderClient.setCallbackListener(this)
        
        enableEdgeToEdge()
        setContent {
            BinderAndroidAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { 
                                Text(
                                    "Binder IPC Test App",
                                    fontWeight = FontWeight.Bold
                                ) 
                            }
                        )
                    }
                ) { innerPadding ->
                    BinderTestUI(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binderClient.unbindService()
    }
    
    // Callback implementations
    override fun onOperationComplete(result: String) {
        runOnUiThread {
            asyncResult = result
            asyncProgress = 100
        }
    }
    
    override fun onProgressUpdate(progress: Int) {
        runOnUiThread {
            asyncProgress = progress
        }
    }
    
    override fun onError(error: String) {
        runOnUiThread {
            asyncResult = "Error: $error"
        }
    }
    
    @Composable
    fun BinderTestUI(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // Service Connection Section
            ServiceConnectionCard()
            
            // Test Operations Section
            if (serviceConnected) {
                TestOperationsCard()
                
                // Async Progress Section
                if (asyncProgress > 0) {
                    AsyncProgressCard()
                }
                
                // Test Results Section
                if (testResults.isNotEmpty()) {
                    TestResultsCard()
                }
            }
            
            // Information Section
            InfoCard()
        }
    }
    
    @Composable
    fun ServiceConnectionCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Service Connection",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Status: ${if (serviceConnected) "Connected" else "Disconnected"}",
                        color = if (serviceConnected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                    
                    Button(
                        onClick = {
                            if (serviceConnected) {
                                binderClient.unbindService()
                                serviceConnected = false
                                serviceInfo = ""
                            } else {
                                lifecycleScope.launch {
                                    isLoading = true
                                    val connected = binderClient.bindService()
                                    serviceConnected = connected
                                    if (connected) {
                                        serviceInfo = binderClient.getServiceInfo()
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (serviceConnected) "Disconnect" else "Connect")
                        }
                    }
                }
                
                if (serviceInfo.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            serviceInfo,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    fun TestOperationsCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "IPC Test Operations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Basic Tests
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                val result = binderClient.testAddNumbers(42, 58)
                                addTestResult("Add Numbers (42 + 58): $result")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Numbers")
                    }
                    
                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                val result = binderClient.testProcessMessage("Hello Binder!")
                                addTestResult("Process Message: $result")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Process Message")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                val result = binderClient.testHeavyComputation(1000000)
                                addTestResult("Heavy Computation: $result")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Heavy Computation")
                    }
                    
                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                val priority = binderClient.getServiceThreadPriority()
                                addTestResult("Service Thread Priority: $priority")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Get Priority")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                binderClient.testAsyncOperation("Background Task")
                                addTestResult("Async operation started...")
                                asyncProgress = 0
                                asyncResult = ""
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Async Operation")
                    }
                    
                    Button(
                        onClick = {
                            lifecycleScope.launch {
                                val results = binderClient.testConcurrentCalls()
                                results.forEach { result ->
                                    addTestResult("Concurrent: $result")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Concurrent Calls")
                    }
                }
            }
        }
    }
    
    @Composable
    fun AsyncProgressCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Async Operation Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                LinearProgressIndicator(
                    progress = { asyncProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Progress: $asyncProgress%")
                
                if (asyncResult.isNotEmpty()) {
                    Text(
                        "Result: $asyncResult",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    @Composable
    fun TestResultsCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Test Results",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(
                        onClick = { testResults = listOf() }
                    ) {
                        Text("Clear")
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(testResults.reversed()) { result ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                result,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun InfoCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "About Binder IPC",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "This app demonstrates Android's Binder IPC mechanism:\n" +
                    "• Inter-process communication\n" +
                    "• AIDL interface usage\n" +
                    "• Service binding and callbacks\n" +
                    "• Priority inheritance\n" +
                    "• Concurrent IPC calls\n" +
                    "• Async operations with progress",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    
    private fun addTestResult(result: String) {
        val timestamp = System.currentTimeMillis()
        val formattedResult = "[${timestamp}] $result"
        testResults = testResults + formattedResult
        Log.d(TAG, "Test result: $formattedResult")
    }
}