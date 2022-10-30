package com.termux.app.startup;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

import android.content.Context;

import org.java_websocket.WebSocket;

import static com.termux.shared.termux.TermuxConstants.TERMUX_HOME_DIR_PATH;

import com.termux.app.startup.Utils;
import com.termux.app.startup.DLoadRVB;

public class Actions {
  private void Actions() {}
  
  private static boolean installRvb(Context c, WebSocket ws) {
    new DLoadRVB().execute(ws);

    File zip = new File(Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder.zip").toString());
    File unzipDest = new File(TERMUX_HOME_DIR_PATH);

    if (zip.exists())
      Utils.send(ws, "success", "Downloaded!");
    else {
      Utils.send(ws, "error", "Failed to download ZIP!");
      return false;
    }

    Utils.send(ws, "info", "Unzipping revanced-builder.zip");
    if(!Utils.unzip(ws, zip, unzipDest))
      return false;

    File rvbMain = new File(Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder-main").toString());
    if (!rvbMain.renameTo(new File(Utils.RVB_LOCATION))) {
      Utils.send(ws, "error", "Error while renaming revanced-builder-main to revanced-buiilder!");
      return false;
    }

    Utils.send(ws, "info", "Installing packages");
    HashMap npmExecResult = Utils.exec(c, "npm", new String[] {"ci", "--omit=dev"});
    if(Boolean.parseBoolean(npmExecResult.get("isError").toString())) {
      Utils.send(ws, "error", "Error while installing packages!\n\nStderr:\n" + npmExecResult.get("stderr"));
      return false;
    } else {
      Utils.send(ws, "success", "Packages installed!");
    }

    return true;
  }

  public static boolean preflight(Context c, WebSocket ws) {
    Utils.send(ws, "info", "Checking and requesting storage permissions");
    if(Utils.requestStorage(c))
      Utils.send(ws, "success", "Internal storage accessible!");
    else {
      Utils.send(ws, "error", "Internal storage permission denied!");
      return false;
    }

    Utils.send(ws, "info", "Checking if ReVanced Builder is installed.");
    if (Utils.isRvbInstalled()) {
      Utils.send(ws, "success", "ReVanced Builder is installed!");
      return true;
    } else {
      Utils.send(ws, "info", "ReVanced Builder not installed. Installing.");
      if (installRvb(c, ws)) {
        Utils.send(ws, "success", "ReVanced Builder installed!");
      } else {
        Utils.send(ws, "error", "Failed to install ReVanced Builder!");
        return false;
      }
    }
    Utils.send(ws, "success", "All checks done");
    Utils.send(ws, "stateChange", "run");
    return true;
  }

  public static boolean run(Context c, WebSocket ws) {
    HashMap runResult = Utils.exec(c, "node", new String[] {"~/revanced-builder"});
    if (Boolean.parseBoolean(runResult.get("isError").toString())) {
      Utils.send(ws, "error", "An unexpected error occured:\n" + execResult.get("stderr"));
      return false;
    }
  }

  public static void update() {}
  public static void reinstall() {}
}
