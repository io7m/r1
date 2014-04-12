/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.renderer.kernel.examples.viewer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jaux.PropertyUtils.ValueNotFound;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityAll;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;

final class Viewer
{
  private static void announceTime()
  {
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS Z");
    System.err.println();
    System.err.println("== Started at " + d.format(c.getTime()));
    System.err.println();
  }

  private static boolean hasConsole()
  {
    final String gui =
      System
        .getProperty("com.io7m.renderer.kernel.examples.viewer.no_console");
    if (gui == null) {
      return true;
    }
    if (gui.equals("true")) {
      return false;
    }
    return true;
  }

  public static void main(
    final String args[])
  {
    if (Viewer.hasConsole() == false) {
      try {
        @SuppressWarnings("resource") final PrintStream out =
          new PrintStream(new BufferedOutputStream(new FileOutputStream(
            "sandbox.log",
            true)));
        System.setErr(out);
        System.setOut(out);
      } catch (final FileNotFoundException e) {
        Viewer.showFatalErrorAndExit(
          Viewer.makeEmptyLog(),
          "Could not open log file",
          e);
      }
    }

    Viewer.announceTime();

    if (args.length == 0) {
      Viewer.showFatalErrorAndExitWithoutException(
        Viewer.makeEmptyLog(),
        "No configuration file",
        "No configuration file specified on command line!");
    }

    try {
      Viewer.run(PropertyUtils.loadFromFile(args[0]));
    } catch (final FileNotFoundException e) {
      Viewer.showFatalErrorAndExit(
        Viewer.makeEmptyLog(),
        "Could not find config file",
        e);
    } catch (final IOException e) {
      Viewer.showFatalErrorAndExit(
        Viewer.makeEmptyLog(),
        "Error reading config file",
        e);
    } catch (final ConstraintError e) {
      Viewer.showFatalErrorAndExit(
        Viewer.makeEmptyLog(),
        "Error reading config file",
        e);
    } catch (final ValueNotFound e) {
      Viewer.showFatalErrorAndExit(
        Viewer.makeEmptyLog(),
        "Error reading config file",
        e);
    }
  }

  private static Log makeEmptyLog()
  {
    return new Log(
      new Properties(),
      "com.io7m.renderer.kernel.examples",
      "viewer");
  }

  public static void run(
    final @Nonnull Properties props)
    throws ConstraintError,
      ValueNotFound
  {
    final Log log =
      new Log(props, "com.io7m.renderer.kernel.examples", "viewer");
    Viewer.runWithConfig(ViewerConfig.fromProperties(props), log);
  }

  public static void runWithConfig(
    final @Nonnull ViewerConfig config,
    final @Nonnull Log log)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        log.debug("starting");

        try {
          final FSCapabilityAll fs =
            Filesystem.makeWithoutArchiveDirectory(log);
          fs.mountArchiveFromAnywhere(
            config.getShaderArchiveDebugFile(),
            PathVirtual.ROOT);
          fs.mountArchiveFromAnywhere(
            config.getShaderArchiveDepthFile(),
            PathVirtual.ROOT);
          fs.mountArchiveFromAnywhere(
            config.getShaderArchiveForwardFile(),
            PathVirtual.ROOT);
          fs.mountArchiveFromAnywhere(
            config.getShaderArchivePostprocessingFile(),
            PathVirtual.ROOT);

          final ViewerMainWindow vmw = new ViewerMainWindow(config, log, fs);
          vmw.pack();
          vmw.setVisible(true);
        } catch (final ConstraintError e) {
          VErrorBox.showErrorWithTitleLater(
            log,
            "Internal constraint error",
            e);
          e.printStackTrace();
          System.exit(1);
        } catch (final FilesystemError e) {
          VErrorBox.showErrorWithTitleLater(log, "Filesystem error", e);
          e.printStackTrace();
          System.exit(1);
        }
      }
    });
  }

  public static void showFatalErrorAndExit(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull Throwable e)
  {
    if (Viewer.hasConsole()) {
      System.err.println("fatal: "
        + title
        + ": "
        + e.getClass().getCanonicalName()
        + ": "
        + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override public void run()
        {
          final StringBuilder m = new StringBuilder();
          m.append(title);
          m.append(" (");
          m.append(e.getClass().getName());
          m.append(")");

          final JDialog r =
            VErrorBox.showErrorWithTitle(log, m.toString(), e);
          r.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final WindowEvent _)
            {
              System.exit(1);
            }
          });
        }
      });
    } catch (final InvocationTargetException x) {
      x.printStackTrace();
    } catch (final InterruptedException x) {
      x.printStackTrace();
    }
  }

  public static void showFatalErrorAndExitWithoutException(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull String message)
  {
    if (Viewer.hasConsole()) {
      System.err.println("fatal: " + title + ": " + message);
      System.exit(1);
    }

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override public void run()
        {
          final JDialog r =
            VErrorBox.showErrorWithoutException(log, title, message);

          r.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final WindowEvent _)
            {
              System.exit(1);
            }
          });
        }
      });
    } catch (final InvocationTargetException x) {
      x.printStackTrace();
    } catch (final InterruptedException x) {
      x.printStackTrace();
    }
  }
}
