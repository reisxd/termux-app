package com.termux.app.startup;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

import org.java_websocket.WebSocket;

import static com.termux.shared.termux.TermuxConstants.TERMUX_HOME_DIR_PATH;

public final class DLoadRVB extends AsyncTask<WebSocket, Object, String> {
  protected String doInBackground(WebSocket... ws) {
    try{
      int count = 0;
      URL url = new URL("https://github.com/reisxd/revanced-builder/archive/refs/heads/main.zip");
      URLConnection conn = url.openConnection();
      Utils.send(ws[0], "info", "Downloading ReVanced Builder");
      conn.connect();
      int fileSize = conn.getContentLength();
      InputStream i = new BufferedInputStream(url.openStream(), 8192);
      OutputStream o = new FileOutputStream(Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder.zip").toString());
      byte data[] = new byte[1024];
      long total = 0;
      while((count = i.read(data)) != -1) {
        total += count;
        publishProgress(ws[0], "" + (int) ((total * 100) / fileSize));
        o.write(data, 0, count);
      }
      o.flush();
      i.close();
      o.close();
    } catch (Exception e) {
        Utils.send(ws[0], "error", "Error while downloading revanced-builder.zip!\n" + Utils.getStackString(e));
        return null;
    }
    return null;
  }

  protected void onProgressUpdate(Object... data) {
    Utils.send((WebSocket)(data[0]), "progress", String.valueOf(data[1]));
  }
}
