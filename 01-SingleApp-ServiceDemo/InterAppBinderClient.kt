package com.visgupta.example.binderclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Client for connecting to Binder service in ANOTHER APPLICATION
 * Demonstrates true inter-app IPC communication
 */
class InterAppBinderClient(private val context: Context) {
    
    private val TAG = "InterAppBinderClient"
    private var service: IBinderTestService? = null
    private var bound = false
    
    // Server app details
    private val SERVER_PACKAGE = "com.visgupta.example.binderserver"
    private val SERVER_SERVICE_ACTION = "com.visgupta.example.BINDER_TEST_SERVICE"
    
    // Callback implementation
    private val callback = object : IBinderCallback.Stub() {
        override fun onOperationComplete(result: String?) {
            Log.d(TAG, "Inter-app operation completed: $result")
            callbackListener?.onOperationComplete(result ?: "")
        }
        
        override fun onProgressUpdate(progress: Int) {
            Log.d(TAG, "Inter-app progress update: $progress%")
            callbackListener?.onProgressUpdate(progress)
        }
        
        override fun onError(error: String?) {
            Log.e(TAG, "Inter-app operation error: $error")
            callbackListener?.onError(error ?: "Unknown error")
        }
    }
    
    interface CallbackListener {
        fun onOperationComplete(result: String)
        fun onProgressUpdate(progress: Int)
        fun onError(error: String)
    }
    
    private var callbackListener: CallbackListener? = null
    
    // Service connection for inter-app binding
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "Connected to service in app: ${className.packageName}")
            this@InterAppBinderClient.service = IBinderTestService.Stub.asInterface(service)
            bound = true
            
            // Register callback
            try {
                this@InterAppBinderClient.service?.registerCallback(callback)
                Log.d(TAG, "Callback registered with inter-app service")
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to register callback with inter-app service", e)
            }
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "Disconnected from inter-app service")
            service = null
            bound = false
        }
    }
    
    /**
     * Check if server app is installed
     */
    fun isServerAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(SERVER_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Bind to service in another application
     */
    suspend fun bindToInterAppService(): Boolean = suspendCoroutine { continuation ->
        if (!isServerAppInstalled()) {
            Log.e(TAG, "Server app $SERVER_PACKAGE is not installed")
            continuation.resume(false)
            return@suspendCoroutine
        }
        
        // Create explicit intent for the service in another app
        val intent = Intent().apply {
            action = SERVER_SERVICE_ACTION
            setPackage(SERVER_PACKAGE) // Target specific package
        }
        
        // For Android 11+ compatibility, you might need to use component name
        // val intent = Intent().apply {
        //     component = ComponentName(SERVER_PACKAGE, "$SERVER_PACKAGE.BinderTestService")
        // }
        
        val result = try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception binding to inter-app service", e)
            false
        }
        
        if (result) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // Give more time for inter-app connection
                continuation.resume(bound)
            }
        } else {
            Log.e(TAG, "Failed to bind to inter-app service")
            continuation.resume(false)
        }
    }
    
    /**
     * Unbind from inter-app service
     */
    fun unbindFromInterAppService() {
        if (bound) {
            try {
                service?.unregisterCallback(callback)
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to unregister callback from inter-app service", e)
            }
            context.unbindService(connection)
            bound = false
            service = null
        }
    }
    
    fun setCallbackListener(listener: CallbackListener?) {
        this.callbackListener = listener
    }
    
    /**
     * Test inter-app service info retrieval
     */
    suspend fun getInterAppServiceInfo(): String = withContext(Dispatchers.IO) {
        try {
            val info = service?.getServiceInfo() ?: "Inter-app service not connected"
            Log.d(TAG, "Inter-app service info: $info")
            info
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting inter-app service info", e)
            "Error communicating with server app: ${e.message}"
        }
    }
    
    /**
     * Test inter-app computation
     */
    suspend fun testInterAppComputation(iterations: Int): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting inter-app heavy computation")
            
            // Set high priority to test cross-app priority inheritance
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            
            val result = service?.performHeavyComputation(iterations) ?: 0L
            Log.d(TAG, "Inter-app computation result: $result")
            result
        } catch (e: RemoteException) {
            Log.e(TAG, "Error in inter-app computation", e)
            -1L
        }
    }
    
    /**
     * Test all inter-app operations
     */
    suspend fun testAllInterAppOperations(): List<String> = withContext(Dispatchers.IO) {
        val results = mutableListOf<String>()
        
        try {
            // Test basic operations
            val info = getInterAppServiceInfo()
            results.add("Info: ${info.substringBefore('\n')}")
            
            val addResult = service?.addNumbers(25, 17) ?: 0
            results.add("Add (25+17): $addResult")
            
            val processResult = service?.processMessage("Inter-app Hello!") ?: "Failed"
            results.add("Process: $processResult")
            
            val priority = service?.getCurrentThreadPriority() ?: -999
            results.add("Server Priority: $priority")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in inter-app operations", e)
            results.add("Error: ${e.message}")
        }
        
        results
    }
    
    fun isConnectedToInterAppService(): Boolean = bound && service != null
}
