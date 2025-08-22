package com.visgupta.binder;

import com.visgupta.binder.IBinderCallback;

/**
 * AIDL interface for inter-app Binder IPC testing
 * This service will be exported and accessible by other applications
 */
interface IBinderTestService {
    /**
     * Basic method to test simple data transfer across apps
     */
    String getServiceInfo();
    
    /**
     * Method to test parameter passing and return values
     */
    int addNumbers(int a, int b);
    
    /**
     * Method to test string manipulation across app boundaries
     */
    String processMessage(String message);
    
    /**
     * Method to test callback functionality (bidirectional inter-app communication)
     */
    void registerCallback(IBinderCallback callback);
    
    /**
     * Method to unregister callback
     */
    void unregisterCallback(IBinderCallback callback);
    
    /**
     * Method to test heavy computation (for priority inheritance testing across apps)
     */
    long performHeavyComputation(int iterations);
    
    /**
     * Method to get current thread priority (for inter-app priority inheritance testing)
     */
    int getCurrentThreadPriority();
    
    /**
     * Method to simulate different types of inter-app IPC operations
     */
    void performAsyncOperation(String operation);
    
    /**
     * Method to get server app information
     */
    String getServerAppInfo();
}
