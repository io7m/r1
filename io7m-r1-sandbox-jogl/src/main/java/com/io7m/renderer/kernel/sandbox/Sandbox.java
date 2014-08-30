/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel.sandbox;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogType;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JPropertyException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;

public final class Sandbox
{
  private static boolean hasConsole()
  {
    final String gui =
      System.getProperty("com.io7m.renderer.sandbox.no_console");
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
    if (Sandbox.hasConsole() == false) {
      try {
        @SuppressWarnings("resource") final PrintStream out =
          new PrintStream(new BufferedOutputStream(new FileOutputStream(
            "sandbox.log",
            true)));
        System.setErr(out);
        System.setOut(out);
      } catch (final FileNotFoundException e) {
        Sandbox.showFatalErrorAndExit(
          Sandbox.makeEmptyLog(),
          "Could not open log file",
          e);
      }
    }

    Sandbox.announceTime();

    if (args.length == 0) {
      Sandbox.showFatalErrorAndExitWithoutException(
        Sandbox.makeEmptyLog(),
        "No configuration file",
        "No configuration file specified on command line!");
    }

    try {
      final Properties p = new Properties();
      final FileInputStream s = new FileInputStream(new File(args[0]));
      p.load(s);
      s.close();
      Sandbox.run(p);
    } catch (final FileNotFoundException e) {
      Sandbox.showFatalErrorAndExit(
        Sandbox.makeEmptyLog(),
        "Could not find config file",
        e);
    } catch (final IOException e) {
      Sandbox.showFatalErrorAndExit(
        Sandbox.makeEmptyLog(),
        "Error reading config file",
        e);
    } catch (final JPropertyException e) {
      Sandbox.showFatalErrorAndExit(
        Sandbox.makeEmptyLog(),
        "Error reading config file",
        e);
    }
  }

  private static void announceTime()
  {
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS Z");
    System.err.println();
    System.err.println("== Started at " + d.format(c.getTime()));
    System.err.println();
  }

  private static LogType makeEmptyLog()
  {
    return Log
      .newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "sandbox");
  }

  public static void run(
    final Properties props)
    throws JPropertyException
  {
    final LogPolicyType policy =
      LogPolicyProperties.newPolicy(props, "com.io7m.renderer");
    final LogType log = Log.newLog(policy, "sandbox");
    Sandbox.runWithConfig(SandboxConfig.fromProperties(props), log);
  }

  public static void runWithConfig(
    final SandboxConfig config,
    final LogType log)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        log.debug("starting");
        try {
          new SBRepeatingReleasedEventsFixer().install();

          final SBGLRenderer renderer = new SBGLRenderer(config, log);
          final SBSceneController controller =
            new SBSceneController(renderer, log);
          renderer.setController(controller);

          final SBMainWindow window =
            new SBMainWindow(config, controller, renderer, log);
          window.setTitle("Sandbox");
          window.setPreferredSize(new Dimension(800, 600));
          window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          window.pack();
          window.setVisible(true);
        } catch (final FilesystemError e) {
          SBErrorBox.showErrorWithTitleLater(log, "Filesystem error", e);
          e.printStackTrace();
        } catch (final IOException e) {
          SBErrorBox.showErrorWithTitleLater(log, "I/O error", e);
          e.printStackTrace();
        } catch (final SBExceptionInputError e) {
          SBErrorBox.showErrorWithTitleLater(log, "Input error", e);
          e.printStackTrace();
        } catch (final Throwable e) {
          SBErrorBox.showErrorWithTitleLater(log, "Bug", e);
          e.printStackTrace();
          System.exit(1);
        }
      }
    });
  }

  public static void showFatalErrorAndExit(
    final LogType log,
    final String title,
    final Throwable e)
  {
    if (Sandbox.hasConsole()) {
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
            SBErrorBox.showErrorWithTitle(log, m.toString(), e);
          r.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final @Nullable WindowEvent _)
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
    final LogType log,
    final String title,
    final String message)
  {
    if (Sandbox.hasConsole()) {
      System.err.println("fatal: " + title + ": " + message);
      System.exit(1);
    }

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override public void run()
        {
          final JDialog r =
            SBErrorBox.showErrorWithoutException(log, title, message);

          r.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final @Nullable WindowEvent _)
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