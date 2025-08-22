package com.visgupta.binderserver

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.visgupta.binder.IBinderCallback
import com.visgupta.binder.IBinderTestService
import java.util.concurrent.Executors

/**
 * Inter-App Binder Service - runs in server application
 * This service is exported and can be accessed by other applications
 */
class BinderTestService : Service() {
    
    private val TAG = "InterAppBinderService"
    private val callbacks = RemoteCallbackList<IBinderCallback>()
    private val executor = Executors.newCachedThreadPool()
    
    private val binder = object : IBinderTestService.Stub() {
        
        override fun getServiceInfo(): String {
            val callingPid = android.os.Binder.getCallingPid()
            val callingUid = android.os.Binder.getCallingUid()
            
            Log.d(TAG, "getServiceInfo() called from PID: $callingPid, UID: $callingUid")
            MainActivity.addLog(MainActivity.LogType.OPERATION, callingPid, "GET_SERVICE_INFO", "Client requested service information")
            
            return "Inter-App Binder Test Service\n" +
                   "Server App: com.visgupta.binderserver\n" +
                   "Service PID: ${Process.myPid()}\n" +
                   "Client PID: $callingPid\n" +
                   "Service UID: ${Process.myUid()}\n" +
                   "Client UID: $callingUid\n" +
                   "Thread: ${Thread.currentThread().name}\n" +
                   "Inter-App Communication: ACTIVE"
        }
        
        override fun addNumbers(a: Int, b: Int): Int {
            val callingPid = android.os.Binder.getCallingPid()
            Log.d(TAG, "Inter-app addNumbers($a, $b) called from PID: $callingPid")
            MainActivity.addLog(MainActivity.LogType.OPERATION, callingPid, "ADD_NUMBERS", "Adding $a + $b")
            
            Thread.sleep(100) // Simulate processing
            val result = a + b
            MainActivity.addLog(MainActivity.LogType.OPERATION, callingPid, "ADD_NUMBERS_RESULT", "Result: $result")
            return result
        }
        
        override fun processMessage(message: String): String {
            val callingPid = android.os.Binder.getCallingPid()
            Log.d(TAG, "Inter-app processMessage('$message') called from PID: $callingPid")
            MainActivity.addLog(MainActivity.LogType.OPERATION, callingPid, "PROCESS_MESSAGE", "Processing: '$message'")
            
            val processedMessage = "Server processed: '$message' (Server PID: ${Process.myPid()}, Client PID: $callingPid)"
            return processedMessage
        }
        
        override fun registerCallback(callback: IBinderCallback?) {
            val callingPid = android.os.Binder.getCallingPid()
            Log.d(TAG, "Inter-app registerCallback() called from PID: $callingPid")
            MainActivity.addLog(MainActivity.LogType.CONNECTION, callingPid, "REGISTER_CALLBACK", "Client registered for callbacks")
            
            callback?.let {
                callbacks.register(it)
                Log.d(TAG, "Inter-app callback registered. Total callbacks: ${callbacks.registeredCallbackCount}")
                MainActivity.addLog(MainActivity.LogType.CONNECTION, callingPid, "CALLBACK_REGISTERED", "Total callbacks: ${callbacks.registeredCallbackCount}")
            }
        }
        
        override fun unregisterCallback(callback: IBinderCallback?) {
            val callingPid = android.os.Binder.getCallingPid()
            Log.d(TAG, "Inter-app unregisterCallback() called from PID: $callingPid")
            MainActivity.addLog(MainActivity.LogType.CONNECTION, callingPid, "UNREGISTER_CALLBACK", "Client unregistered callbacks")
            
            callback?.let {
                callbacks.unregister(it)
                Log.d(TAG, "Inter-app callback unregistered. Total callbacks: ${callbacks.registeredCallbackCount}")
                MainActivity.addLog(MainActivity.LogType.CONNECTION, callingPid, "CALLBACK_UNREGISTERED", "Total callbacks: ${callbacks.registeredCallbackCount}")
            }
        }
        
        override fun performHeavyComputation(iterations: Int): Long {
            val callingPid = android.os.Binder.getCallingPid()
            Log.d(TAG, "Inter-app performHeavyComputation($iterations) called from PID: $callingPid")
            MainActivity.addLog(MainActivity.LogType.OPERATION, callingPid, "HEAVY_COMPUTATION", "Starting computation with $iterations iterations")
            
            val startTime = System.currentTimeMillis()
            val initialPriority = Thread.currentThread().priority
            
            Log.d(TAG, "Server thread priority before computation: $initialPriority")
            
            // Simulate heavy computation
            var result = 0L
            for (i in 0 until iterations) {
                result += (i * i).toLong()
                
                // Check for inter-app priority inheritance
                if (i % 10000 == 0) {
                    val currentPriority = Thread.currentThread().priority
                    if (currentPriority != initialPriority) {
                        Log.d(TAG, "Inter-app priority inheritance detected! Changed from $initialPriority to $currentPriority")
                    }
                }
            }
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.d(TAG, "Inter-app heavy computation completed in ${duration}ms")
            
            return result
        }
        
        override fun getCurrentThreadPriority(): Int {
            val priority = Thread.currentThread().priority
            val callingPid = android.os.Binder.getCallingPid()
            Log.d(TAG, "Inter-app getCurrentThreadPriority() = $priority, called from PID: $callingPid")
            return priority
        }
        
        override fun performAsyncOperation(operation: String) {
            val callingPid = android.os.Binder.getCallingPid()
            Log.d(TAG, "Inter-app performAsyncOperation('$operation') called from PID: $callingPid")
            
            executor.execute {
                try {
                    val steps = 10
                    for (i in 1..steps) {
                        Thread.sleep(300) // Simulate work
                        
                        val progress = (i * 100) / steps
                        notifyProgress(progress)
                    }
                    
                    val result = "Inter-app operation '$operation' completed successfully at ${System.currentTimeMillis()}"
                    notifyCompletion(result)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in inter-app async operation", e)
                    notifyError("Inter-app error: ${e.message}")
                }
            }
        }
        
        override fun getServerAppInfo(): String {
            return "Server App Package: com.visgupta.binderserver\n" +
                   "Server PID: ${Process.myPid()}\n" +
                   "Server UID: ${Process.myUid()}\n" +
                   "Service Thread: ${Thread.currentThread().name}\n" +
                   "Active Callbacks: ${callbacks.registeredCallbackCount}"
        }
        
        private fun notifyProgress(progress: Int) {
            val n = callbacks.beginBroadcast()
            Log.d(TAG, "Notifying $n inter-app clients of progress: $progress%")
            MainActivity.addLog(MainActivity.LogType.CALLBACK, 0, "PROGRESS_UPDATE", "Notifying $n clients: $progress%")
            for (i in 0 until n) {
                try {
                    callbacks.getBroadcastItem(i).onProgressUpdate(progress)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Error notifying inter-app progress", e)
                    MainActivity.addLog(MainActivity.LogType.ERROR, 0, "CALLBACK_ERROR", "Failed to notify progress: ${e.message}")
                }
            }
            callbacks.finishBroadcast()
        }
        
        private fun notifyCompletion(result: String) {
            val n = callbacks.beginBroadcast()
            Log.d(TAG, "Notifying $n inter-app clients of completion")
            MainActivity.addLog(MainActivity.LogType.CALLBACK, 0, "OPERATION_COMPLETE", "Notifying $n clients: Operation completed")
            for (i in 0 until n) {
                try {
                    callbacks.getBroadcastItem(i).onOperationComplete(result)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Error notifying inter-app completion", e)
                    MainActivity.addLog(MainActivity.LogType.ERROR, 0, "CALLBACK_ERROR", "Failed to notify completion: ${e.message}")
                }
            }
            callbacks.finishBroadcast()
        }
        
        private fun notifyError(error: String) {
            val n = callbacks.beginBroadcast()
            Log.d(TAG, "Notifying $n inter-app clients of error")
            for (i in 0 until n) {
                try {
                    callbacks.getBroadcastItem(i).onError(error)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Error notifying inter-app error", e)
                }
            }
            callbacks.finishBroadcast()
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        val callingPid = android.os.Binder.getCallingPid()
        Log.d(TAG, "Inter-app service bound by PID: $callingPid")
        Log.d(TAG, "Intent action: ${intent?.action}")
        Log.d(TAG, "Intent package: ${intent?.`package`}")
        
        MainActivity.addLog(MainActivity.LogType.CONNECTION, callingPid, "SERVICE_BIND", "Client bound to service (Action: ${intent?.action})")
        
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Inter-app service created in PID: ${Process.myPid()}")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Inter-app service started")
        return START_STICKY // Keep service running
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Inter-app service destroyed")
        callbacks.kill()
        executor.shutdown()
    }
}
