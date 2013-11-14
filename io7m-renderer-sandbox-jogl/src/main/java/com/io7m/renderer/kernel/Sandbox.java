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

package com.io7m.renderer.kernel;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jaux.PropertyUtils.ValueIncorrectType;
import com.io7m.jaux.PropertyUtils.ValueNotFound;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FilesystemError;

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
    if (args.length == 0) {
      Sandbox.showFatalErrorAndExitWithoutException(
        Sandbox.makeEmptyLog(),
        "No configuration file",
        "No configuration file specified on command line!");
    }

    try {
      Sandbox.run(PropertyUtils.loadFromFile(args[0]));
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
    } catch (final ConstraintError e) {
      Sandbox.showFatalErrorAndExit(
        Sandbox.makeEmptyLog(),
        "Error reading config file",
        e);
    } catch (final ValueNotFound e) {
      Sandbox.showFatalErrorAndExit(
        Sandbox.makeEmptyLog(),
        "Error reading config file",
        e);
    } catch (final ValueIncorrectType e) {
      Sandbox.showFatalErrorAndExit(
        Sandbox.makeEmptyLog(),
        "Error reading config file",
        e);
    }
  }

  private static Log makeEmptyLog()
  {
    return new Log(new Properties(), "com.io7m.renderer", "sandbox");
  }

  public static void run(
    final @Nonnull Properties props)
    throws ConstraintError,
      ValueNotFound,
      ValueIncorrectType
  {
    final Log log = new Log(props, "com.io7m.renderer", "sandbox");
    Sandbox.runWithConfig(SandboxConfig.fromProperties(props), log);
  }

  public static void runWithConfig(
    final @Nonnull SandboxConfig config,
    final @Nonnull Log log)
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
            new SBMainWindow(controller, renderer, log);
          window.setTitle("Sandbox");
          window.setPreferredSize(new Dimension(800, 600));
          window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          window.pack();
          window.setVisible(true);
        } catch (final FilesystemError e) {
          SBErrorBox.showErrorWithTitleLater(log, "Filesystem error", e);
          e.printStackTrace();
        } catch (final ConstraintError e) {
          SBErrorBox.showErrorWithTitleLater(
            log,
            "Internal constraint error",
            e);
          e.printStackTrace();
          System.exit(1);
        } catch (final IOException e) {
          SBErrorBox.showErrorWithTitleLater(log, "I/O error", e);
          e.printStackTrace();
        }
      }
    });
  }

  public static void showFatalErrorAndExit(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull Throwable e)
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
