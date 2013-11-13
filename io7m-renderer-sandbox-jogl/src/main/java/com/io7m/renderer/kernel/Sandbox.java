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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FilesystemError;

public final class Sandbox
{
  private static final @Nonnull Options options;

  static {
    options = Sandbox.makeOptions();
  }

  private static @Nonnull Options makeOptions()
  {
    final Options o = new Options();

    {
      OptionBuilder.withLongOpt("shaders");
      OptionBuilder.withDescription("Archive file containing shaders");
      OptionBuilder.withArgName("shaders.zip");
      OptionBuilder.hasArg();
      OptionBuilder.isRequired();
      o.addOption(OptionBuilder.create());
    }

    return o;
  }

  private static void showHelp()
  {
    final HelpFormatter formatter = new HelpFormatter();
    final PrintWriter pw = new PrintWriter(System.err);
    pw.println("sandbox: [options]");
    pw.println();
    pw.println("  Options include:");
    formatter.printOptions(pw, 80, Sandbox.options, 2, 4);
    pw.println();
    pw.flush();
  }

  public static void main(
    final String args[])
    throws ParseException
  {
    if (args.length == 0) {
      Sandbox.showHelp();
      System.exit(1);
    }

    final PosixParser parser = new PosixParser();
    final CommandLine line = parser.parse(Sandbox.options, args);
    final File shaders = new File(line.getOptionValue("shaders"));

    final Properties props = new Properties();
    props.setProperty("com.io7m.renderer.logs.sandbox", "true");
    props.setProperty("com.io7m.renderer.logs.sandbox.filesystem", "false");
    props.setProperty(
      "com.io7m.renderer.logs.sandbox.control.filesystem",
      "false");
    props.setProperty(
      "com.io7m.renderer.logs.sandbox.gl.object-parser",
      "false");
    props.setProperty("com.io7m.renderer.sandbox.level", "LOG_DEBUG");
    final Log log = new Log(props, "com.io7m.renderer", "sandbox");

    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        log.debug("starting");
        try {
          new SBRepeatingReleasedEventsFixer().install();

          final SBGLRenderer renderer = new SBGLRenderer(shaders, log);
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
          SBErrorBox.showError(log, "Filesystem error", e);
          e.printStackTrace();
          System.exit(1);
        } catch (final ConstraintError e) {
          SBErrorBox.showError(log, "Internal constraint error", e);
          e.printStackTrace();
          System.exit(1);
        } catch (final IOException e) {
          SBErrorBox.showError(log, "I/O error", e);
          e.printStackTrace();
          System.exit(1);
        }
      }
    });
  }

}
