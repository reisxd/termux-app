package com.termux.app.startup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

public final class Utils {
  public static final String RVB_LOCATION = Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder").toString();

  public static String getStackString(Exception ex) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  public static void send(WebSocket conn, String type, String msg) {
    HashMap<String, String> hm = new HashMap();
    hm.put("type", type);
    hm.put("msg", msg);
    try {
      JSONObject json = new JSONObject(hm);
      conn.send(json.toString());
    } catch (Exception e) {
      Logger.logError("RVBA_Startup:send", "Failed to send message to client! Exception:\n" + getStackString(e));
    }
  }

  public static boolean isRvbInstalled() {
    File d = new File(RVB_LOCATION);
    if (d.exists() && d.isDirectory()) {
      File[] dirList = d.listFiles();
      return !(dirList == null || dirList.length == 0);
    } else return false;
  }

  public static HashMap exec(Context c, String command, String[] args) {
    ExecutionCommand ec = new ExecutionCommand(-1, Paths.get(TERMUX_BIN_PREFIX_DIR_PATH, command).toString(), args, null, RVB_LOCATION, ExecutionCommand.Runner.APP_SHELL.getName(), false);
    AppShell as = AppShell.execute(c, ec, null, new TermuxShellEnvironment(), null, false);
    HashMap<String, String> res = new HashMap();
    res.put("isError", String.valueOf((as == null || !ec.isSuccessful() || ec.resultData.exitCode != 0) || ec.resultData.isStateFailed()));
    res.put("stdout", ec.resultData.stdout.toString());
    res.put("stderr", ec.resultData.stderr.toString());
    return res;
  }

  public static boolean unzip(WebSocket conn, File zipFile, File targetDirectory) {
    try {
      ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
      ZipEntry ze;
      int count;
      byte[] buffer = new byte[8192];
      while ((ze = zis.getNextEntry()) != null) {
        File file = new File(targetDirectory, ze.getName());
        File dir = ze.isDirectory() ? file : file.getParentFile();
        if (!dir.isDirectory() && !dir.mkdirs())
          throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
        if (ze.isDirectory())
          continue;
        FileOutputStream fout = new FileOutputStream(file);
        try {
          while ((count = zis.read(buffer)) != -1)
            fout.write(buffer, 0, count);
        } finally {
          fout.close();
        }
        long time = ze.getTime();
        if (time > 0)
          file.setLastModified(time);
      }
      zis.close();
    } catch (Exception e) {
      send(conn, "error", "Failed to unzip!\nException: " + getStackString(e));
      return false;
    }
    send(conn, "success", "Unzipped!");
    return true;
  }

  public static boolean requestStorage(Context c) {
    int code = PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION;
    return PermissionUtils.checkAndRequestLegacyOrManageExternalStoragePermission(c, code, true);
  }
}
