package com.visgupta.example.binderandroidapp;

/**
 * Callback interface for bidirectional communication testing
 * This demonstrates how services can call back to clients
 */
interface IBinderCallback {
    /**
     * Called when an async operation completes
     */
    void onOperationComplete(String result);
    
    /**
     * Called to notify progress updates
     */
    void onProgressUpdate(int progress);
    
    /**
     * Called when an error occurs
     */
    void onError(String error);
}
