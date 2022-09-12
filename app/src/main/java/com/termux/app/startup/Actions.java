package com.termux.app.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URLConnection;
import java.util.BufferedInputStream;
import java.util.InputStream;
import java.util.OutputStream;
import java.util.HashMap;
import org.json.JSONObject;

import android.content.Context;

import org.java_websocket.WebSocket;

import com.termux.shared.android.PermissionUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.file.FileUtils;
import static com.termux.shared.termux.TermuxConstants.TERMUX_HOME_DIR_PATH;

public final Actions {
  private void Actions() {}
  
  private static final String TAG = "RVBA_Startup:Actions";

  private static final String RVB_LOCATION = FileUtils.normalizePath(Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder"));

  private static void send(WebSocket conn, String type, String msg) {
    Hashmap<String, String> hm = new HashMap();
    hm.put("type", type);
    hm.put("msg", msg);
    JSONObject json = new JSONObject(hm);
    conn.send(json.toString());
  }

  private static boolean isRvbInstalled() {
    boolean exists = FileUtils.directoryFileExists(RVB_LOCATION, false);
    if (exists) {
      boolean dirList = (new File(RVB_LOCATION)).listFiles();
      return !(dirList == null || dirList.length == 0);
    } else return false;
  }

  class DLoadRVB extends AsyncTask<WebSocket, String, String> {
    @Override
    protected String doInBackground(WebSocket... ws) {
      try{
        int count = 0;
        URL url = new URL("https://github.com/reisxd/revanced-builder/archive/refs/heads/main.zip");
        URLConnection conn = url.openConnection();
        send(ws[0], "info", "Downloading ReVanced Builder");
        conn.connect();
        int fileSize = conn.getContentLength();
        InputStream i = new BufferedInputStream(url.openStream(), 8192);
        OutputStream o = new FileOutputStream(FileUtils.normalizePath(Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder.zip")));
        byte data[] = new byte[1024];
        long total = 0;
        while((count = i.read(data)) != -1) {
          total += count;
          publishProgress("" + (int) ((total * 100) / lengthOfFile));
          o.write(data, 0, count);
        }
        o.flush();
        i.close();
        o.close();
      } catch (Exception e) {
          StringWriter sw = new StringWriter();
          e.printStackTrace(new PrintWriter(sw));
          send(ws[0], "error", "Error while downloading revanced-builder.zip!\n" + sw.toString());
      }
      return null;
    }
    protected void onProgressUpdate(String... progress) {
      send(ws, "progress", progress[0]);
    }
  }

  private static boolean installRvb(WebSocket ws) {
    new DLoadRVB().execute();
  }

  public static boolean preflight(Context c, WebSocket ws) {
    send(ws, "info", "Checking and requesting storage permissions");
    if(PermissionUtils.checkAndRequestLegacyOrManageExternalStoragePermission(c, PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION, true))
      send(ws, "success", "Internal storage accessible!");
    else {
      send(ws, "error", "Internal storage permission denied!");
      return false;
    }
    send(ws, "info", "Checking if ReVanced Builder is installed.");
    if (isRvbInstalled()) {
      send(ws, "success", "ReVanced Builder is installed!");
      return true;
    } else {
      send(ws, "info", "ReVanced Builder not installed. Installing.");
      if (installRvb()) {
        send(ws, "success", "ReVanced Builder installed!");
        return true;
      } else {
        send(ws, "error", "Failed to install ReVanced Builder!");
        return false;
      }
    }
  }
  public static idkwhattoreturn run() {}
  public static idkwhattoreturn update() {}
  public static idkwhattoreturn reinstall() {}
}
