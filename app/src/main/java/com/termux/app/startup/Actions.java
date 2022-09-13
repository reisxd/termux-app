package com.termux.app.startup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import org.json.JSONObject;

import android.content.Context;

import org.java_websocket.WebSocket;

import com.termux.shared.android.PermissionUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.shell.command.ExecutionCommand;
import com.termux.shared.shell.command.runner.app.AppShell;
import com.termux.shared.termux.shell.command.environment.TermuxShellEnvironment;
import static com.termux.shared.termux.TermuxConstants.TERMUX_HOME_DIR_PATH;
import static com.termux.shared.termux.TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH;

public class Actions {
  private void Actions() {}
  
  private static final String RVB_LOCATION = Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder");

  private static void send(WebSocket conn, String type, String msg) {
    Hashmap<String, String> hm = new HashMap();
    hm.put("type", type);
    hm.put("msg", msg);
    JSONObject json = new JSONObject(hm);
    conn.send(json.toString());
  }

  private static boolean isRvbInstalled() {
    File d = new File(RVB_LOCATION);
    if (d.exists() && d.isDirectory()) {
      boolean dirList = d.listFiles();
      return !(dirList == null || dirList.length == 0);
    } else return false;
  }

  private static HashMap exec(Context c, String command, String[] args) {
    ExecutionCommand ec = new ExecutionCommand(-1, Paths.get(TERMUX_BIN_PREFIX_DIR_PATH, command), args, null, RVB_LOCATION, ExecutionCommand.Runner.APP_SHELL.getName(), false);
    AppShell as = AppShell.execute(c, ec, new TermuxShellEnvironment(), null, false);
    HashMap<String, Object> res = new HashMap();
    res.put("isError", new Boolean((as == null || !ec.isSuccessful() || ec.resultData.exitCode != 0) || ec.resultData.isStateFailed()));
    res.put("stdout", ec.resultData.stdout);
    res.put("stderr", ec.resultData.stderr);
    return res;
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
        OutputStream o = new FileOutputStream(Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder.zip"));
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

  private static boolean installRvb(Context c, WebSocket ws) {
    new DLoadRVB().execute();

    send(ws, "info", "Unzipping revanced-builder.zip");
    File homeDirFile = new File(TERMUX_HOME_DIR_PATH);
    String zip = Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder.zip");
    if(!dir.isExists()) dir.mkdirs();
    FileInputStream fis;
    final byte[] buffer = new byte[8096];
    try {
      fis = new FileInputStream(zip);
      ZipInputStream zis = new ZipInputStream(fis);
      ZipEntry ze = zis.getNextEntry();
      while(ze != null) {
        String fn = ze.getName();
        File newFile = new File(Paths.get(homeDirFile, "fn"));
        new File(newFile.getParent()).mkdirs();
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        zis.closeEntry();
        ze = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
    fis.close();
    } catch (Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        send(ws, "error", "Error while unzipping revanced-builder.zip!\n" + sw.toString());
        return false;
    }

    if (!(new File(Paths.get(homeDirFile, "revanced-builder-main"))).renameTo(new File(RVB_LOCATION))) {
      send(ws, "error", "Error while renaming revanced-builder-main to revanced-buiilder!");
      return false;
    }

    send(ws, "info", "Installing packages");
    HashMap npmExecResult = exec(c, "npm", new String[] {"ci", "--omit=dev"});
    if(npmExecResult.get("isError")) {
      send(ws, "error", "Error while installing packages!\n\nStderr:\n" + npmExecResult.get("stderr"));
      return false;
    }

    return true;
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
  public static void run() {}
  public static void update() {}
  public static void reinstall() {}
}
