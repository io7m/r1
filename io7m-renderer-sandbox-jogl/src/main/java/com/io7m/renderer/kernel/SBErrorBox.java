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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.Log;

public final class SBErrorBox
{
  public static void showError(
    final @Nonnull Log log,
    final @Nonnull String title,
    final @Nonnull Throwable e)
  {
    log.error(title + ": " + e.getMessage());

    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("synthetic-access") @Override public void run()
      {
        SBErrorBox.showErrorWithException(title, e);
      }
    });
  }

  private static void showErrorBox(
    final @Nonnull String title,
    final @Nonnull String message,
    final @Nonnull JTextArea backtrace)
  {
    final JScrollPane pane = new JScrollPane(backtrace);
    pane.setPreferredSize(new Dimension(600, 320));
    backtrace.setCaretPosition(0);

    final JPanel header = new JPanel();
    final DesignGridLayout dg = new DesignGridLayout(header);
    dg.row().grid().add(new JLabel(title));
    dg.row().grid().add(new JLabel(message));

    final BorderLayout layout = new BorderLayout();
    final JPanel panel = new JPanel(layout);
    panel.add(header, BorderLayout.NORTH);
    panel.add(pane, BorderLayout.SOUTH);

    JOptionPane.showMessageDialog(
      null,
      panel,
      title,
      JOptionPane.ERROR_MESSAGE);
  }

  private static void showErrorWithException(
    final @Nonnull String title,
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

    SBErrorBox.showErrorBox(title, e.getMessage(), text);
  }

  public static void showErrorWithoutException(
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

        SBErrorBox.showErrorBox(title, message, text);
      }
    });
  }
}
