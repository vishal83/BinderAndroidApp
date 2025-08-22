package com.visgupta.example.binderandroidapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Client class for interacting with the Binder test service
 * Demonstrates various IPC communication patterns
 */
class BinderServiceClient(private val context: Context) {
    
    private val TAG = "BinderServiceClient"
    private var service: IBinderTestService? = null
    private var bound = false
    
    // Callback implementation for bidirectional communication
    private val callback = object : IBinderCallback.Stub() {
        override fun onOperationComplete(result: String?) {
            Log.d(TAG, "Async operation completed: $result")
            callbackListener?.onOperationComplete(result ?: "")
        }
        
        override fun onProgressUpdate(progress: Int) {
            Log.d(TAG, "Progress update: $progress%")
            callbackListener?.onProgressUpdate(progress)
        }
        
        override fun onError(error: String?) {
            Log.e(TAG, "Operation error: $error")
            callbackListener?.onError(error ?: "Unknown error")
        }
    }
    
    // Interface for UI to receive callback events
    interface CallbackListener {
        fun onOperationComplete(result: String)
        fun onProgressUpdate(progress: Int)
        fun onError(error: String)
    }
    
    private var callbackListener: CallbackListener? = null
    
    // Service connection for managing the binding
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "Service connected")
            this@BinderServiceClient.service = IBinderTestService.Stub.asInterface(service)
            bound = true
            
            // Register callback for bidirectional communication
            try {
                this@BinderServiceClient.service?.registerCallback(callback)
                Log.d(TAG, "Callback registered with service")
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to register callback", e)
            }
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "Service disconnected")
            service = null
            bound = false
        }
    }
    
    /**
     * Bind to the Binder test service
     */
    suspend fun bindService(): Boolean = suspendCoroutine { continuation ->
        val intent = Intent(context, BinderTestService::class.java)
        val result = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        
        if (result) {
            // Wait a moment for the connection to establish
            CoroutineScope(Dispatchers.Main).launch {
                delay(500) // Give time for onServiceConnected to be called
                continuation.resume(bound)
            }
        } else {
            continuation.resume(false)
        }
    }
    
    /**
     * Unbind from the service
     */
    fun unbindService() {
        if (bound) {
            try {
                service?.unregisterCallback(callback)
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to unregister callback", e)
            }
            context.unbindService(connection)
            bound = false
            service = null
        }
    }
    
    /**
     * Set callback listener for UI updates
     */
    fun setCallbackListener(listener: CallbackListener?) {
        this.callbackListener = listener
    }
    
    /**
     * Test basic service info retrieval
     */
    suspend fun getServiceInfo(): String = withContext(Dispatchers.IO) {
        try {
            service?.getServiceInfo() ?: "Service not connected"
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting service info", e)
            "Error: ${e.message}"
        }
    }
    
    /**
     * Test parameter passing and return values
     */
    suspend fun testAddNumbers(a: Int, b: Int): Int = withContext(Dispatchers.IO) {
        try {
            service?.addNumbers(a, b) ?: 0
        } catch (e: RemoteException) {
            Log.e(TAG, "Error adding numbers", e)
            -1
        }
    }
    
    /**
     * Test string processing across process boundaries
     */
    suspend fun testProcessMessage(message: String): String = withContext(Dispatchers.IO) {
        try {
            service?.processMessage(message) ?: "Service not connected"
        } catch (e: RemoteException) {
            Log.e(TAG, "Error processing message", e)
            "Error: ${e.message}"
        }
    }
    
    /**
     * Test heavy computation and priority inheritance
     */
    suspend fun testHeavyComputation(iterations: Int): Long = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "Starting heavy computation with $iterations iterations")
            
            // Set high priority for this thread to test priority inheritance
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            Log.d(TAG, "Client thread priority set to URGENT_DISPLAY")
            
            val result = service?.performHeavyComputation(iterations) ?: 0L
            val endTime = System.currentTimeMillis()
            
            Log.d(TAG, "Heavy computation completed in ${endTime - startTime}ms")
            result
        } catch (e: RemoteException) {
            Log.e(TAG, "Error in heavy computation", e)
            -1L
        }
    }
    
    /**
     * Get current thread priority from service
     */
    suspend fun getServiceThreadPriority(): Int = withContext(Dispatchers.IO) {
        try {
            service?.getCurrentThreadPriority() ?: -999
        } catch (e: RemoteException) {
            Log.e(TAG, "Error getting thread priority", e)
            -999
        }
    }
    
    /**
     * Test async operations with callbacks
     */
    suspend fun testAsyncOperation(operation: String): Unit = withContext(Dispatchers.IO) {
        try {
            service?.performAsyncOperation(operation)
            Log.d(TAG, "Async operation '$operation' started")
        } catch (e: RemoteException) {
            Log.e(TAG, "Error starting async operation", e)
        }
    }
    
    /**
     * Test multiple concurrent IPC calls
     */
    suspend fun testConcurrentCalls(): List<String> = withContext(Dispatchers.IO) {
        val results = mutableListOf<String>()
        
        try {
            // Launch multiple concurrent calls
            val jobs = listOf(
                async { "Add: ${testAddNumbers(5, 3)}" },
                async { "Process: ${testProcessMessage("Hello Binder!")}" },
                async { "Priority: ${getServiceThreadPriority()}" },
                async { "Info: ${getServiceInfo().substringBefore('\n')}" }
            )
            
            // Wait for all to complete
            jobs.forEach { job ->
                results.add(job.await())
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in concurrent calls", e)
            results.add("Error: ${e.message}")
        }
        
        results
    }
    
    /**
     * Check if service is currently bound
     */
    fun isServiceBound(): Boolean = bound && service != null
}
