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

package com.io7m.r1.examples.viewer_new;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.examples.ExampleImageType;
import com.io7m.r1.examples.ExampleImages;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRendererDeferredDefault;
import com.jogamp.opengl.util.Animator;

/**
 * A window showing the expected and actual results of rendering a given
 * example.
 */

@SuppressWarnings("synthetic-access") public final class VExampleCaseDisplayImage extends
  JFrame
{
  private static final long         serialVersionUID;

  static {
    serialVersionUID = 857163604270367488L;
  }

  private final GLJPanel            canvas;
  private final JLabel              canvas_label;
  private final VExpectedImage      expected;
  private final JLabel              expected_label;
  private final ExampleImageType    example;
  private final VExampleRunnerImage runner;
  private final AtomicInteger       current_view_index;

  /**
   * Construct an example display.
   *
   * @param in_log
   *          A log interface
   * @param in_config
   *          The configuration
   * @param in_example_images
   *          An example image cache
   * @param in_renderer_cons
   *          The renderer
   * @param in_example
   *          The current example
   * @throws Exception
   *           On errors
   */

  public VExampleCaseDisplayImage(
    final LogUsableType in_log,
    final VExampleConfig in_config,
    final ExampleImages<BufferedImage> in_example_images,
    final ExampleImageType in_example,
    final ExampleRendererConstructorType in_renderer_cons)
    throws Exception
  {
    this.example = NullCheck.notNull(in_example, "Example");

    this.setTitle(this.example.exampleGetName());

    final GLProfile profile = VGLImplementation.getGLProfile(in_log);
    final GLCapabilities caps = new GLCapabilities(profile);

    this.runner =
      new VExampleRunnerImage(
        in_config,
        in_example,
        in_renderer_cons,
        null,
        in_log);

    this.current_view_index = new AtomicInteger(0);

    this.canvas = new GLJPanel(caps);
    this.canvas.addGLEventListener(this.runner);

    this.expected =
      new VExpectedImage(
        in_log,
        in_example_images,
        in_example,
        this.current_view_index);
    this.expected.setPreferredSize(new Dimension(
      ExampleImages.EXAMPLE_RESULT_IMAGE_WIDTH,
      ExampleImages.EXAMPLE_RESULT_IMAGE_HEIGHT));

    this.expected_label = new JLabel("Expected");
    this.canvas_label = new JLabel("OpenGL");

    this.expected.update(ExampleRendererDeferredDefault.getName());

    final JPanel top_panel = new JPanel();
    final DesignGridLayout top_layout = new DesignGridLayout(top_panel);
    top_layout.row().grid().add(this.expected_label).add(this.canvas_label);
    top_layout.row().grid().add(this.expected).add(this.canvas);

    this.canvas.setPreferredSize(new Dimension(
      ExampleImages.EXAMPLE_RESULT_IMAGE_WIDTH,
      ExampleImages.EXAMPLE_RESULT_IMAGE_HEIGHT));

    final JButton save = new JButton("Save view");
    save.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        in_log.debug("Save requested");
        VExampleCaseDisplayImage.this.runner.requestImageSave();
      }
    });

    final JPanel controls = new JPanel();
    final DesignGridLayout controls_layout = new DesignGridLayout(controls);
    controls_layout.row().left().add(save);

    final Container pane = this.getContentPane();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.add(top_panel);
    pane.add(controls);

    final Animator animator = new Animator();

    this.addWindowListener(new WindowAdapter() {
      @Override public void windowClosing(
        final @Nullable WindowEvent e)
      {
        animator.stop();
        VExampleCaseDisplayImage.this.canvas.destroy();
      }
    });

    animator.add(this.canvas);
    animator.start();
  }
}
