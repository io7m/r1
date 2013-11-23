package com.io7m.renderer.kernel;

import java.awt.Container;
import java.awt.Dimension;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;

abstract class SBExampleWindow extends JFrame
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 4254766657404535893L;
  }

  public SBExampleWindow(
    final @Nonnull String title)
  {
    super(title);

    final Container panel = this.getContentPane();
    final DesignGridLayout layout = new DesignGridLayout(panel);
    try {
      this.addToLayout(layout);
    } catch (final ConstraintError e) {
      e.printStackTrace();
    }
    this.setMinimumSize(new Dimension(640, 480));
    this.pack();
    this.setVisible(true);
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  public abstract void addToLayout(
    final @Nonnull DesignGridLayout layout)
    throws ConstraintError;
}
