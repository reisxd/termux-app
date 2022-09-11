package com.termux.app.startup;

import java.io.File;
import java.nio.file.Paths;

import android.content.Context;

import com.termux.shared.android.PermissionUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.file.FileUtils;
import static com.termux.shared.termux.TermuxConstants.TERMUX_HOME_DIR_PATH;

public final Actions {
  private void Actions() {}
  
  private static final String TAG = "RVBA_Startup:Actions";

  private static final String RVB_LOCATION = FileUtils.normalizePath(Paths.get(TERMUX_HOME_DIR_PATH, "revanced-builder"));

  private static boolean isRvbInstalled() {
    Logger.logInfo(TAG, "Checking if ReVanced Builder is installed");
    boolean exists = FileUtils.directoryFileExists(RVB_LOCATION, false);
    if (exists) {
      boolean dirList = (new File(RVB_LOCATION)).listFiles();
      return !(dirList == null || dirList.length == 0);
    } else return false;
  }

  public static boolean preflight(Context c) {
    // FIXME: Return something better.
    Logger.logInfo(TAG, "Checking and granting storage permissions");
    PermissionUtils.checkAndRequestLegacyOrManageExternalStoragePermission(c, PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION, true);
    if (isRvbInstalled()) {
      Logger.logInfo(TAG, "ReVanced Builder is installed.\nAll checks done.");
      return true;
    } else {
      Logger.logInfo(TAG, "ReVanced Builder not installed. Installing.");
      if (installRvb()) {
        Logger.logInfo(TAG, "Done.");
        return true;
      } else {
        return false;
      }
    }
  }
  public static idkwhattoreturn run() {}
  public static idkwhattoreturn update() {}
  public static idkwhattoreturn reinstall() {}
}
