package com.visgupta.example.binderandroidapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Service implementation that demonstrates Binder IPC functionality
 * This service runs in a separate process to properly test IPC communication
 */
class BinderTestService : Service() {
    
    private val TAG = "BinderTestService"
    private val callbacks = RemoteCallbackList<IBinderCallback>()
    private val executor = Executors.newCachedThreadPool()
    
    private val binder = object : IBinderTestService.Stub() {
        
        override fun getServiceInfo(): String {
            Log.d(TAG, "getServiceInfo() called from PID: ${android.os.Binder.getCallingPid()}")
            return "Binder Test Service\n" +
                   "Service PID: ${Process.myPid()}\n" +
                   "Caller PID: ${android.os.Binder.getCallingPid()}\n" +
                   "Service UID: ${Process.myUid()}\n" +
                   "Caller UID: ${android.os.Binder.getCallingUid()}\n" +
                   "Thread: ${Thread.currentThread().name}"
        }
        
        override fun addNumbers(a: Int, b: Int): Int {
            Log.d(TAG, "addNumbers($a, $b) called")
            // Simulate some processing time
            Thread.sleep(100)
            return a + b
        }
        
        override fun processMessage(message: String): String {
            Log.d(TAG, "processMessage('$message') called")
            val processedMessage = "Processed: $message (by PID ${Process.myPid()})"
            return processedMessage
        }
        
        override fun registerCallback(callback: IBinderCallback?) {
            Log.d(TAG, "registerCallback() called")
            callback?.let {
                callbacks.register(it)
                Log.d(TAG, "Callback registered. Total callbacks: ${callbacks.registeredCallbackCount}")
            }
        }
        
        override fun unregisterCallback(callback: IBinderCallback?) {
            Log.d(TAG, "unregisterCallback() called")
            callback?.let {
                callbacks.unregister(it)
                Log.d(TAG, "Callback unregistered. Total callbacks: ${callbacks.registeredCallbackCount}")
            }
        }
        
        override fun performHeavyComputation(iterations: Int): Long {
            Log.d(TAG, "performHeavyComputation($iterations) called")
            val startTime = System.currentTimeMillis()
            val initialPriority = Thread.currentThread().priority
            
            Log.d(TAG, "Thread priority before computation: $initialPriority")
            
            // Simulate heavy computation
            var result = 0L
            for (i in 0 until iterations) {
                result += (i * i).toLong()
                // Check if priority changed during computation (priority inheritance)
                if (i % 10000 == 0) {
                    val currentPriority = Thread.currentThread().priority
                    if (currentPriority != initialPriority) {
                        Log.d(TAG, "Priority inheritance detected! Changed from $initialPriority to $currentPriority")
                    }
                }
            }
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.d(TAG, "Heavy computation completed in ${duration}ms")
            
            return result
        }
        
        override fun getCurrentThreadPriority(): Int {
            val priority = Thread.currentThread().priority
            Log.d(TAG, "getCurrentThreadPriority() = $priority")
            return priority
        }
        
        override fun performAsyncOperation(operation: String) {
            Log.d(TAG, "performAsyncOperation('$operation') called")
            
            executor.execute {
                try {
                    // Simulate async work
                    val steps = 10
                    for (i in 1..steps) {
                        Thread.sleep(200) // Simulate work
                        
                        // Notify progress to all registered callbacks
                        val progress = (i * 100) / steps
                        notifyProgress(progress)
                    }
                    
                    // Notify completion
                    val result = "Operation '$operation' completed successfully at ${System.currentTimeMillis()}"
                    notifyCompletion(result)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in async operation", e)
                    notifyError("Error: ${e.message}")
                }
            }
        }
        
        private fun notifyProgress(progress: Int) {
            val n = callbacks.beginBroadcast()
            for (i in 0 until n) {
                try {
                    callbacks.getBroadcastItem(i).onProgressUpdate(progress)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Error notifying progress", e)
                }
            }
            callbacks.finishBroadcast()
        }
        
        private fun notifyCompletion(result: String) {
            val n = callbacks.beginBroadcast()
            for (i in 0 until n) {
                try {
                    callbacks.getBroadcastItem(i).onOperationComplete(result)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Error notifying completion", e)
                }
            }
            callbacks.finishBroadcast()
        }
        
        private fun notifyError(error: String) {
            val n = callbacks.beginBroadcast()
            for (i in 0 until n) {
                try {
                    callbacks.getBroadcastItem(i).onError(error)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Error notifying error", e)
                }
            }
            callbacks.finishBroadcast()
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound by PID: ${android.os.Binder.getCallingPid()}")
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created in PID: ${Process.myPid()}")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        callbacks.kill()
        executor.shutdown()
    }
}
