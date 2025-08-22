package com.visgupta.example.binderandroidapp;

import com.visgupta.example.binderandroidapp.IBinderCallback;

/**
 * AIDL interface for testing Binder IPC functionality
 * This demonstrates the communication between processes using Android's Binder mechanism
 */
interface IBinderTestService {
    /**
     * Basic method to test simple data transfer
     */
    String getServiceInfo();
    
    /**
     * Method to test parameter passing and return values
     */
    int addNumbers(int a, int b);
    
    /**
     * Method to test string manipulation across process boundaries
     */
    String processMessage(String message);
    
    /**
     * Method to test callback functionality (demonstrates bidirectional communication)
     */
    void registerCallback(IBinderCallback callback);
    
    /**
     * Method to unregister callback
     */
    void unregisterCallback(IBinderCallback callback);
    
    /**
     * Method to test heavy computation (for priority inheritance testing)
     */
    long performHeavyComputation(int iterations);
    
    /**
     * Method to get current thread priority (for priority inheritance testing)
     */
    int getCurrentThreadPriority();
    
    /**
     * Method to simulate different types of IPC operations
     */
    void performAsyncOperation(String operation);
}
