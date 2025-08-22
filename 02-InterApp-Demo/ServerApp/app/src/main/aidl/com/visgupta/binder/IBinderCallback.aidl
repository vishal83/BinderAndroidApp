package com.visgupta.binder;

/**
 * Callback interface for inter-app bidirectional communication
 * Allows server app to call back to client app
 */
interface IBinderCallback {
    /**
     * Called when an async operation completes in server app
     */
    void onOperationComplete(String result);
    
    /**
     * Called to notify progress updates from server app
     */
    void onProgressUpdate(int progress);
    
    /**
     * Called when an error occurs in server app
     */
    void onError(String error);
}
