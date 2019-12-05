package io.sentry.android.core;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.UUID;

final class Installation {
  private static String sID = null;
  private static final String INSTALLATION = "INSTALLATION";
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  public static synchronized String id(Context context) {
    if (sID == null) {
      File installation = new File(context.getFilesDir(), INSTALLATION);
      try {
        if (!installation.exists()) writeInstallationFile(installation);
        sID = readInstallationFile(installation);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return sID;
  }

  private static String readInstallationFile(File installation) throws IOException {
    RandomAccessFile f = new RandomAccessFile(installation, "r");
    byte[] bytes = new byte[(int) f.length()];
    f.readFully(bytes);
    f.close();
    return new String(bytes, UTF_8);
  }

  private static void writeInstallationFile(File installation) throws IOException {
    FileOutputStream out = new FileOutputStream(installation);
    String id = UUID.randomUUID().toString();
    out.write(id.getBytes(UTF_8));
    out.close();
  }
}
