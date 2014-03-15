package com.io7m.renderer.kernel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.types.RSpaceClip;
import com.io7m.renderer.types.RTriangle4F;
import com.io7m.renderer.types.RVectorI4F;

@SuppressWarnings("boxing") public final class ClippingDemo
{
  private static class Panel extends JPanel implements
    MouseListener,
    KeyListener
  {
    private static final int  BORDER = 32;
    private static final long serialVersionUID;

    static {
      serialVersionUID = -1588785802156394754L;
    }

    private static boolean clipInX(
      final Point2H p)
    {
      final boolean in_lower_x = (p.z + p.x) > 0;
      final boolean in_upper_x = (p.z - p.x) > 0;
      final boolean in_x = in_lower_x && in_upper_x;
      return in_x;
    }

    private static boolean clipInY(
      final Point2H p)
    {
      final boolean in_lower_y = (p.z + p.y) > 0;
      final boolean in_upper_y = (p.z - p.y) > 0;
      final boolean in_y = in_lower_y && in_upper_y;
      return in_y;
    }

    private Point2H tri_p0;
    private Point2H tri_p1;
    private Point2H tri_p2;
    private int     point_current = 1;

    Panel()
    {
      this.addMouseListener(this);
      this.addKeyListener(this);

      this.tri_p0 = new Point2H(0.0f, 0.0f, 1.0f);
      this.tri_p1 = new Point2H(0.0f, 0.0f, 1.0f);
      this.tri_p2 = new Point2H(0.0f, 0.0f, 1.0f);
    }

    private void drawAxes(
      final Graphics g)
    {
      final int x_max = this.getSize().width;
      final int y_max = this.getSize().height;

      final int bo2 = Panel.BORDER / 2;
      final int bo3 = Panel.BORDER / 3;
      final int bo4 = Panel.BORDER / 4;
      final int bm2 = Panel.BORDER * 2;

      /**
       * Draw clipping volume
       */

      {
        final Point2P p0 =
          Point2P.fromPointH(
            new Point2H(-1.0f, -1.0f, 1.0f),
            Panel.BORDER,
            Panel.BORDER,
            x_max - bm2,
            y_max - bm2);

        final Point2P p1 =
          Point2P.fromPointH(
            new Point2H(1.0f, 1.0f, 1.0f),
            Panel.BORDER,
            Panel.BORDER,
            x_max - bm2,
            y_max - bm2);

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(
          (int) p0.x,
          (int) p0.y,
          (int) (p1.x - p0.x),
          (int) (p1.y - p0.y));
      }

      /**
       * Axes
       */

      {
        final Point2P p0 =
          Point2P.fromPointH(
            new Point2H(-2.0f, 0.0f, 1.0f),
            Panel.BORDER,
            Panel.BORDER,
            x_max - bm2,
            y_max - bm2);

        final Point2P p1 =
          Point2P.fromPointH(
            new Point2H(2.0f, 0.0f, 1.0f),
            Panel.BORDER,
            Panel.BORDER,
            x_max - bm2,
            y_max - bm2);

        g.setColor(Color.black);
        g.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
        g.drawString("-2", (int) p0.x - bo2, (int) p0.y + bo4);
        g.drawString("+2", (int) p1.x + bo4, (int) p1.y + bo4);
      }

      {
        final Point2P p0 =
          Point2P.fromPointH(
            new Point2H(0.0f, -2.0f, 1.0f),
            Panel.BORDER,
            Panel.BORDER,
            x_max - bm2,
            y_max - bm2);

        final Point2P p1 =
          Point2P.fromPointH(
            new Point2H(0.0f, 2.0f, 1.0f),
            Panel.BORDER,
            Panel.BORDER,
            x_max - bm2,
            y_max - bm2);

        g.setColor(Color.black);
        g.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
        g.drawString("-2", (int) p0.x, (int) p0.y - bo3);
        g.drawString("+2", (int) p1.x - bo3, (int) p1.y + bo2);
      }
    }

    private void drawLine(
      final Graphics g,
      final Color color,
      final Point2H p0,
      final Point2H p1)
    {
      final Point2P pp0 =
        Point2P.fromPointH(
          p0,
          Panel.BORDER,
          Panel.BORDER,
          this.getBorderedWidth(),
          this.getBorderedHeight());
      final Point2P pp1 =
        Point2P.fromPointH(
          p1,
          Panel.BORDER,
          Panel.BORDER,
          this.getBorderedWidth(),
          this.getBorderedHeight());

      g.setColor(color);
      g.drawLine((int) pp0.x, (int) pp0.y, (int) pp1.x, (int) pp1.y);
    }

    private void drawPoint(
      final Graphics g,
      final Color color,
      final Point2H p)
    {
      final boolean in_x = Panel.clipInX(p);
      final boolean in_y = Panel.clipInY(p);

      final Point2P pp =
        Point2P.fromPointH(
          p,
          Panel.BORDER,
          Panel.BORDER,
          this.getBorderedWidth(),
          this.getBorderedHeight());

      g.setColor(color);
      g.fillOval((int) pp.x - 5, (int) pp.y - 5, 10, 10);

      g.drawString(String.format("(%s, %s)", in_x ? "in x" : "out x", in_y
        ? "in y"
        : "out y"), (int) pp.x - 10, (int) pp.y - 30);
      g.drawString(
        String.format("(%.2f,%.2f,%.2f)", p.x, p.y, p.z),
        (int) pp.x - 10,
        (int) pp.y - 10);

    }

    private int getBorderedHeight()
    {
      final int bm2 = Panel.BORDER * 2;
      return this.getSize().height - bm2;
    }

    private int getBorderedWidth()
    {
      final int bm2 = Panel.BORDER * 2;
      return this.getSize().width - bm2;
    }

    @Override public void mouseClicked(
      final MouseEvent e)
    {
      // Nothing
    }

    @Override public void mouseEntered(
      final MouseEvent e)
    {
      // Nothing
    }

    @Override public void mouseExited(
      final MouseEvent e)
    {
      // Nothing
    }

    @Override public void mousePressed(
      final MouseEvent e)
    {
      // Nothing
    }

    @Override public void mouseReleased(
      final MouseEvent e)
    {
      final int bm2 = Panel.BORDER * 2;
      final int w = this.getSize().width - bm2;
      final int h = this.getSize().height - bm2;

      final Point2H p =
        Point2H.fromPointP(
          new Point2P(e.getX(), e.getY()),
          Panel.BORDER,
          Panel.BORDER,
          w,
          h);

      System.out.println("Click " + p);

      switch (this.point_current) {
        case 1:
        {
          this.tri_p0 = p;
          break;
        }
        case 2:
        {
          this.tri_p1 = p;
          break;
        }
        case 3:
        {
          this.tri_p2 = p;
          break;
        }
      }
    }

    @Override public void paint(
      final Graphics g)
    {
      try {
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);

        this.drawAxes(g);
        this.drawPoint(g, Color.RED, this.tri_p0);
        this.drawPoint(g, Color.BLUE, this.tri_p1);
        this.drawPoint(g, Color.GREEN, this.tri_p2);

        this.drawLine(g, Color.BLACK, this.tri_p0, this.tri_p1);
        this.drawLine(g, Color.BLACK, this.tri_p1, this.tri_p2);
        this.drawLine(g, Color.BLACK, this.tri_p2, this.tri_p0);
        this.drawClip(g);

        g.setColor(Color.BLACK);
        g.drawString(
          String.format("Current point: %d", this.point_current),
          Panel.BORDER,
          Panel.BORDER);

        this.repaint(1000);
      } catch (final Throwable x) {
        x.printStackTrace();
      }
    }

    private void drawClip(
      final Graphics g)
      throws ConstraintError
    {
      final RVectorI4F<RSpaceClip> p0 =
        new RVectorI4F<RSpaceClip>(this.tri_p0.x, this.tri_p0.y, 0.0f, 1.0f);
      final RVectorI4F<RSpaceClip> p1 =
        new RVectorI4F<RSpaceClip>(this.tri_p1.x, this.tri_p1.y, 0.0f, 1.0f);
      final RVectorI4F<RSpaceClip> p2 =
        new RVectorI4F<RSpaceClip>(this.tri_p2.x, this.tri_p2.y, 0.0f, 1.0f);

      final RTriangle4F<RSpaceClip> tri = RTriangle4F.newTriangle(p0, p1, p2);

      final List<RTriangle4F<RSpaceClip>> results =
        KTriangleClipping.clipTrianglePlanes(tri, KTriangleClipping.PLANES);

      for (final RTriangle4F<RSpaceClip> t : results) {
        this.drawTriangle(g, Color.MAGENTA, t);
      }
    }

    private void drawTriangle(
      final Graphics g,
      final @Nonnull Color c,
      final @Nonnull RTriangle4F<RSpaceClip> t)
    {
      final VectorReadable4F p0 = t.getP0();
      final VectorReadable4F p1 = t.getP1();
      final VectorReadable4F p2 = t.getP2();

      final Point2H p0_h = new Point2H(p0.getXF(), p0.getYF(), p0.getWF());
      final Point2H p1_h = new Point2H(p1.getXF(), p1.getYF(), p1.getWF());
      final Point2H p2_h = new Point2H(p2.getXF(), p2.getYF(), p2.getWF());

      this.drawLine(g, c, p0_h, p1_h);
      this.drawLine(g, c, p1_h, p2_h);
      this.drawLine(g, c, p2_h, p0_h);
    }

    @Override public void keyTyped(
      final KeyEvent e)
    {
      // Nothing
    }

    @Override public void keyPressed(
      final KeyEvent e)
    {
      // Nothing
    }

    @Override public void keyReleased(
      final KeyEvent e)
    {
      if (e.getKeyCode() == KeyEvent.VK_1) {
        this.point_current = 1;
      } else if (e.getKeyCode() == KeyEvent.VK_2) {
        this.point_current = 2;
      } else if (e.getKeyCode() == KeyEvent.VK_3) {
        this.point_current = 3;
      }

      System.out.printf("Point current: %d\n", this.point_current);
    }
  }

  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        final JFrame frame = new JFrame(ClippingDemo.class.getCanonicalName());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(640, 480));
        final Panel panel = new Panel();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        panel.requestFocusInWindow();
      }
    });
  }
}
