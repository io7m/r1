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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.examples.ExampleImages;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRendererName;
import com.io7m.r1.examples.ExampleSceneType;
import com.jogamp.opengl.util.Animator;

/**
 * A window showing the expected and actual results of rendering a given
 * example.
 */

@SuppressWarnings({ "boxing", "synthetic-access" }) public final class VExampleCaseDisplayScene extends
  JFrame
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 857163604270367488L;
  }

  private static String getViewText(
    final int i,
    final int size)
  {
    final String text = String.format("%d of %d", i + 1, size);
    assert text != null;
    return text;
  }

  private final GLJPanel                       canvas;
  private final JLabel                         canvas_label;
  private final AtomicInteger                  current_view_index;
  private final VExpectedImage                 expected;
  private final JLabel                         expected_label;
  private final JComboBox<ExampleRendererName> expected_selector;
  private final JLabel                         label_example;
  private final JLabel                         label_renderer;
  private final JLabel                         label_view;
  private final JLabel                         renderer_label;
  private final ExampleSceneType               example;
  private final VExampleRunnerScene            runner;

  /**
   * Construct a scene display.
   *
   * @param in_log
   *          A log interface
   * @param in_config
   *          The configuration
   * @param in_example_images
   *          An example image cache
   * @param in_example
   *          The current example
   * @param in_renderer_cons
   *          A renderer constructor
   * @param in_renderer_name
   *          The name of the renderer
   * @throws Exception
   *           On errors
   */

  public VExampleCaseDisplayScene(
    final LogUsableType in_log,
    final VExampleConfig in_config,
    final ExampleImages<BufferedImage> in_example_images,
    final ExampleSceneType in_example,
    final ExampleRendererConstructorType in_renderer_cons,
    final ExampleRendererName in_renderer_name)
    throws Exception
  {
    this.example = NullCheck.notNull(in_example, "Example");

    this.setTitle(this.example.exampleGetName());

    final GLProfile profile = VGLImplementation.getGLProfile(in_log);
    final GLCapabilities caps = new GLCapabilities(profile);

    this.runner =
      new VExampleRunnerScene(
        in_config,
        in_example,
        in_renderer_cons,
        null,
        in_log);

    this.current_view_index = new AtomicInteger(0);

    this.label_renderer = new JLabel("");
    this.label_view =
      new JLabel(VExampleCaseDisplayScene.getViewText(
        this.current_view_index.get(),
        in_example.exampleViewpoints().size()));

    this.label_example = new JLabel(in_example.exampleGetName());

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

    this.renderer_label = new JLabel("");
    this.expected_label = new JLabel("Expected");
    this.canvas_label = new JLabel("OpenGL");

    this.expected_selector = new JComboBox<ExampleRendererName>();

    {
      final Class<? extends ExampleSceneType> c =
        NullCheck.notNull(in_example.getClass());
      final List<ExampleRendererName> renderer_results =
        in_example_images.getSceneRenderers(c);
      for (final ExampleRendererName rc : renderer_results) {
        this.expected_selector.addItem(rc);
      }

      this.expected_selector.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent _)
        {
          try {
            final ExampleRendererName name =
              (ExampleRendererName) VExampleCaseDisplayScene.this.expected_selector
                .getSelectedItem();
            assert name != null;

            VExampleCaseDisplayScene.this.expected.update(name);
          } catch (final Exception e) {
            throw new UnreachableCodeException(e);
          }
        }
      });

      this.expected.update(in_renderer_name);
    }

    final JPanel top_panel = new JPanel();
    final DesignGridLayout top_layout = new DesignGridLayout(top_panel);
    top_layout.row().grid().add(this.expected_label).add(this.canvas_label);
    top_layout
      .row()
      .grid()
      .add(this.expected_selector)
      .add(this.renderer_label);
    top_layout.row().grid().add(this.expected).add(this.canvas);

    this.canvas.setPreferredSize(new Dimension(
      ExampleImages.EXAMPLE_RESULT_IMAGE_WIDTH,
      ExampleImages.EXAMPLE_RESULT_IMAGE_HEIGHT));

    final JButton previous = new JButton("Previous view");
    previous.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        try {
          in_log.debug("Previous view requested");
          VExampleCaseDisplayScene.this.runner.viewPrevious();
          VExampleCaseDisplayScene.this.updateImageAndText();
        } catch (final Exception x) {
          throw new UnreachableCodeException(x);
        }
      }
    });

    final JButton next = new JButton("Next view");
    next.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        try {
          in_log.debug("Next view requested");
          VExampleCaseDisplayScene.this.runner.viewNext();
          VExampleCaseDisplayScene.this.updateImageAndText();
        } catch (final Exception x) {
          throw new UnreachableCodeException(x);
        }
      }
    });

    final JButton save = new JButton("Save view");
    save.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        in_log.debug("Save requested");
        VExampleCaseDisplayScene.this.runner.requestImageSave();
      }
    });

    final JPanel controls = new JPanel();
    final DesignGridLayout controls_layout = new DesignGridLayout(controls);
    controls_layout.row().left().add(previous).add(next).add(save);
    controls_layout.emptyRow();
    controls_layout
      .row()
      .grid(new JLabel("Renderer"))
      .add(this.label_renderer);
    controls_layout.row().grid(new JLabel("Example")).add(this.label_example);
    controls_layout.row().grid(new JLabel("View")).add(this.label_view);

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
        VExampleCaseDisplayScene.this.canvas.destroy();
      }
    });

    animator.add(this.canvas);
    animator.start();
  }

  private void updateImageAndText()
    throws Exception
  {
    this.current_view_index.set(this.runner.viewGetIndex());
    this.label_view.setText(VExampleCaseDisplayScene.getViewText(
      this.current_view_index.get(),
      this.example.exampleViewpoints().size()));
    this.expected.updateWithSameRenderer();
  }
}
