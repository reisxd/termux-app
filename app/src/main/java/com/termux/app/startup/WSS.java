package com.termux.app.startup;

import java.net.InetSocketAddress;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WSS extends WebSocketServer {
  private static final String TAG = "Startup:WSS";

  public WSS(InetSocketAddress address) {
    super(address);
  }
  
  @Override
  public void onOpen(WebSocket c, ClientHandshake h){
    Log.i(TAG, "New connection: " + h.getResourceDescriptor());
  }

  @Override
  public void onClose(WebSocket c, int code, String reason, boolean remote) {
    Log.i(TAG, "Connection " + c.getRemoteSocketAddress() + " closed with exit code " + code + ". Additional info:\n" + reason);
  }

  @Override
  public void onMessage(WebSocket c, String msg) {
    Log.d(TAG, "Received message from " + c.getRemoteSocketAddress() + ":\n" + msg);
  }

  @Override
  public void onError(WebSocket c, Exception ex) {
    Log.e(TAG, "Error occured at " + c.getRemoteSocketAddress() + ":\n" + Log.getStackTraceString(ex));
  }

  @Override
  public void onStart() {
    Log.i(TAG, "Server started successfully!");
  }
}
