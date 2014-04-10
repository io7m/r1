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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.Log;

public final class SBErrorBox
{
  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        final Log log = new Log(new Properties(), "x", "y");

        try {
          final StringBuilder m = new StringBuilder();
          for (int index = 0; index < 32; ++index) {
            m.append("This is too much text, and this is line "
              + index
              + "\n");
          }

          Integer.parseInt(m.toString());
        } catch (final NumberFormatException x) {
          final JDialog d = SBErrorBox.showError(log, x);
          d.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final WindowEvent e)
            {
              System.exit(0);
            }
          });
        }
      }
    });
  }

  private static @Nonnull JDialog showActualErrorBox(
    final @Nonnull String title,
    final @Nonnull String message,
    final @CheckForNull JTextArea backtrace)
  {
    final JDialog d = new JDialog();
    d.setTitle(title);
    d.setMinimumSize(new Dimension(320, 0));

    final JButton ok = new JButton("OK");
    ok.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final ActionEvent _)
      {
        SBWindowUtilities.closeDialog(d);
      }
    });

    JLabel icon = null;
    try {
      icon = SBIcons.makeErrorIcon();
    } catch (final IOException _) {
      // Who cares?
    }

    final int max_length = 120;
    final String truncated;
    if (message.length() > max_length) {
      truncated = message.substring(0, max_length - 1) + "...";
    } else {
      truncated = message;
    }

    final JPanel main = new JPanel();
    final DesignGridLayout dg = new DesignGridLayout(main);
    dg.row().grid(icon).add(new JLabel(title));
    dg.emptyRow();
    dg.row().grid().add(new JLabel(truncated));

    if (backtrace != null) {
      final JScrollPane pane = new JScrollPane(backtrace);
      pane.setPreferredSize(new Dimension(600, 320));
      backtrace.setCaretPosition(0);

      final JLabel backtrace_note =
        new JLabel("The full error backtrace is as follows:");
      dg.emptyRow();
      dg.row().grid().add(backtrace_note);
      dg.emptyRow();
      dg.row().grid().add(pane);
    }

    dg.emptyRow();
    dg.row().grid().add(ok);

    d.setContentPane(main);
    d.pack();
    d.setVisible(true);
    return d;
  }

  private static @Nonnull JDialog showActualErrorWithException(
    final @Nonnull String title,
    final @Nonnull String message,
    final @Nonnull Throwable e)
  {
    final StringWriter writer = new StringWriter();
    writer.append(e.getMessage());
    writer.append("\n");
    writer.append("\n");

    e.printStackTrace(new PrintWriter(writer));
    e.printStackTrace();

    final JTextArea text = new JTextArea();
    text.setEditable(false);
    text.setText(writer.toString());

    return SBErrorBox.showActualErrorBox(title, message, text);
  }

  public static @Nonnull JDialog showError(
    final @Nonnull Log log,
    final @Nonnull Throwable e)
  {
    final String title = e.getClass().getCanonicalName();
    log.error(title + ": " + e.getMessage());
    return SBErrorBox.showActualErrorWithException(title, e.getMessage(), e);
  }

  public static @Nonnull JDialog showErrorWithTitle(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull Throwable e)
  {
    log.error(title + ": " + e.getMessage());
    return SBErrorBox.showActualErrorWithException(title, e.getMessage(), e);
  }

  public static void showErrorLater(
    final @Nonnull Log log,
    final @Nonnull Throwable e)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        SBErrorBox.showError(log, e);
      }
    });
  }

  public static void showErrorWithTitleLater(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull Throwable e)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        SBErrorBox.showErrorWithTitle(log, title, e);
      }
    });
  }

  public static @Nonnull JDialog showErrorWithoutException(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull String message)
  {
    log.error(title + ": " + message);
    return SBErrorBox.showActualErrorBox(title, message, null);
  }

  public static void showErrorWithoutExceptionLater(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull String message)
  {
    log.error(title + ": " + message);

    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("synthetic-access") @Override public void run()
      {
        final JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setText(message);
        SBErrorBox.showActualErrorBox(title, message, text);
      }
    });
  }
}