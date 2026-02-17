package com.example.logistics_driver_app.data.websocket

/**
 * Interface for WebSocket event callbacks
 */
interface WebSocketListener {
    
    /**
     * Called when WebSocket connection is successfully opened
     */
    fun onConnected()
    
    /**
     * Called when connection is closed
     * @param code Close code
     * @param reason Close reason
     */
    fun onDisconnected(code: Int, reason: String)
    
    /**
     * Called when ACK message is received
     * @param message ACK message
     */
    fun onAckReceived(message: String)
    
    /**
     * Called when NEW_ORDER message is received
     * @param order New order details
     */
    fun onNewOrderReceived(order: NewOrderPayload)
    
    /**
     * Called when ERROR message is received
     * @param errorMessage Error message
     */
    fun onError(errorMessage: String)
    
    /**
     * Called when a connection error occurs
     * @param throwable Exception details
     */
    fun onConnectionError(throwable: Throwable)
}
