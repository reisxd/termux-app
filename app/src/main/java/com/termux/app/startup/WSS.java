package com.termux.app.startup;

import java.net.InetSocketAddress;

import org.json.JSONObject;

import android.content.Context;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.termux.shared.logger.Logger;
import com.termux.app.startup.Actions;

public class WSS extends WebSocketServer {
  private static final String TAG = "RVBA_Startup:WSS";
  private Context context;

  public WSS(InetSocketAddress address, Context context) {
    super(address);
    this.context = context;
  }
  
  @Override
  public void onOpen(WebSocket c, ClientHandshake h){
    Logger.logInfo(TAG, "New connection: " + h.getResourceDescriptor());
  }

  @Override
  public void onClose(WebSocket c, int code, String reason, boolean remote) {
    Logger.logInfo(TAG, "Connection " + c.getRemoteSocketAddress() + " closed with exit code " + code + ". Additional info:\n" + reason);
  }

  @Override
  public void onMessage(WebSocket c, String msg) {
    // We assume its a JSON string.
    Log.logDebug(TAG, "Received message from " + c.getRemoteSocketAddress() + ":\n" + msg);
    JSONObject msgJson = new JSONObject(msg);
    String action = msgJson.get("action");
    switch(action) {
      case "preflight":
        Actions.preflight(context, c);
        break;
      case "run":
        Actions.run();
        break;
      case "update":
        Actions.update();
        break;
      case "reinstall":
        Actions.reinstall();
        break;
      default:
        // send some error here Ig
        break;
    }
  }

  @Override
  public void onError(WebSocket c, Exception ex) {
    Logger.logError(TAG, "Error occured at " + c.getRemoteSocketAddress() + ":\n" + Log.getStackTraceString(ex));
  }

  @Override
  public void onStart() {
    Logger.logInfo(TAG, "Server started successfully!");
  }
}
