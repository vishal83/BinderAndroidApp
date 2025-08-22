package com.visgupta.binderclient

import android.os.Bundle
import android.os.Process
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.visgupta.binderclient.ui.theme.BinderClientTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), InterAppBinderClient.CallbackListener {
    
    private val TAG = "ClientMainActivity"
    private lateinit var interAppClient: InterAppBinderClient
    
    // State variables for UI
    private var serverAppInstalled by mutableStateOf(false)
    private var serviceConnected by mutableStateOf(false)
    private var serviceInfo by mutableStateOf("")
    private var testResults by mutableStateOf(listOf<String>())
    private var isLoading by mutableStateOf(false)
    private var asyncProgress by mutableStateOf(0)
    private var asyncResult by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        interAppClient = InterAppBinderClient(this)
        interAppClient.setCallbackListener(this)
        
        // Check if server app is installed
        serverAppInstalled = interAppClient.isServerAppInstalled()
        
        enableEdgeToEdge()
        setContent {
            BinderClientTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { 
                                Text(
                                    "Binder Client App",
                                    fontWeight = FontWeight.Bold
                                ) 
                            }
                        )
                    }
                ) { innerPadding ->
                    ClientUI(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        interAppClient.unbindFromInterAppService()
    }
    
    // Callback implementations for inter-app communication
    override fun onOperationComplete(result: String) {
        runOnUiThread {
            asyncResult = result
            asyncProgress = 100
            addTestResult("Async Complete: $result")
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
            addTestResult("Async Error: $error")
        }
    }
    
    @Composable
    fun ClientUI(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // Client Status Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "📱 Client Status",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        "Client App: com.visgupta.binderclient\n" +
                        "Client PID: ${Process.myPid()}\n" +
                        "Client UID: ${Process.myUid()}\n" +
                        "Server App Installed: ${if (serverAppInstalled) "✅ YES" else "❌ NO"}\n" +
                        "Inter-App Connection: ${if (serviceConnected) "🟢 CONNECTED" else "🔴 DISCONNECTED"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
            
            // Server Connection Section
            if (serverAppInstalled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🔗 Server Connection",
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
                                        interAppClient.unbindFromInterAppService()
                                        serviceConnected = false
                                        serviceInfo = ""
                                    } else {
                                        lifecycleScope.launch {
                                            isLoading = true
                                            val connected = interAppClient.bindToInterAppService()
                                            serviceConnected = connected
                                            if (connected) {
                                                serviceInfo = interAppClient.getInterAppServiceInfo()
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
                                    Text(if (serviceConnected) "Disconnect" else "Connect to Server")
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
            } else {
                // Server Not Installed Warning
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "⚠️ Server App Required",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Text(
                            "Please install the Binder Server App first:\n\n" +
                            "1. Build BinderServerApp project\n" +
                            "2. Install server APK on device\n" +
                            "3. Launch server app and start service\n" +
                            "4. Return to this client app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
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
                    "🧪 Inter-App IPC Tests",
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
                                val result = interAppClient.testInterAppAddNumbers(42, 58)
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
                                val result = interAppClient.testInterAppProcessMessage("Hello Inter-App!")
                                addTestResult("Process Message: ${result.substringBefore('(')}")
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
                                val result = interAppClient.testInterAppComputation(100000)
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
                                val priority = interAppClient.getInterAppThreadPriority()
                                addTestResult("Server Thread Priority: $priority")
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
                                interAppClient.testInterAppAsyncOperation("Inter-App Background Task")
                                addTestResult("Inter-app async operation started...")
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
                                val results = interAppClient.testAllInterAppOperations()
                                results.forEach { result ->
                                    addTestResult("Batch: $result")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test All")
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
                    "⏳ Inter-App Async Progress",
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
                        "📊 Test Results",
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
    
    private fun addTestResult(result: String) {
        val timestamp = System.currentTimeMillis()
        val formattedResult = "[${timestamp}] $result"
        testResults = testResults + formattedResult
        Log.d(TAG, "Inter-app test result: $formattedResult")
    }
}
