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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

final class SBProjectionMatrixPanel extends JPanel
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 1220191040371483205L;
  }

  private final static class CustomControls implements MatrixCreator
  {
    private final @Nonnull SBMatrix4x4Fields<RTransformProjection> matrix;

    CustomControls()
    {
      this.matrix = new SBMatrix4x4Fields<RTransformProjection>();
    }

    void add(
      final IRowCreator group)
    {
      for (int r = 0; r < 4; ++r) {
        final JTextField f0 = this.matrix.getRowColumnField(r, 0);
        final JTextField f1 = this.matrix.getRowColumnField(r, 1);
        final JTextField f2 = this.matrix.getRowColumnField(r, 2);
        final JTextField f3 = this.matrix.getRowColumnField(r, 3);

        group.grid().add(f0).add(f1).add(f2).add(f3);
      }
    }

    @Override public @Nonnull RMatrixI4x4F<RTransformProjection> getMatrix()
      throws SBExceptionInputError,
        ConstraintError
    {
      return this.matrix.getMatrix4x4f();
    }
  }

  private static final class PerspectiveControls implements MatrixCreator
  {
    private static final float         DEFAULT_FOV    = 30.0f;
    private static final float         DEFAULT_ASPECT = 640.0f / 480.0f;
    private static final float         DEFAULT_FAR_Z  = 100.0f;
    private static final float         DEFAUT_NEAR_Z  = 1.0f;

    private final @Nonnull JTextField  near_z;
    private final @Nonnull JTextField  far_z;
    private final @Nonnull JTextField  aspect;
    private final @Nonnull JTextField  fov;
    private final @Nonnull JButton     defaults;
    private final @Nonnull MatrixM4x4F matrix_temp;

    PerspectiveControls()
    {
      this.near_z = new JTextField();
      this.far_z = new JTextField();
      this.aspect = new JTextField();
      this.fov = new JTextField();
      this.matrix_temp = new MatrixM4x4F();

      this.setDefaults();

      this.defaults = new JButton("Defaults");
      this.defaults.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          PerspectiveControls.this.setDefaults();
        }
      });
    }

    void add(
      final IRowCreator group)
    {
      group.grid().add(new JLabel("Near Z")).add(this.near_z, 3);
      group.grid().add(new JLabel("Far Z")).add(this.far_z, 3);
      group.grid().add(new JLabel("Aspect ratio")).add(this.aspect, 3);
      group.grid().add(new JLabel("FOV (degrees)")).add(this.fov, 3);
      group.right().add(this.defaults);
    }

    @Override public @Nonnull RMatrixI4x4F<RTransformProjection> getMatrix()
      throws SBExceptionInputError,
        ConstraintError
    {
      final float nz = SBTextFieldUtilities.getFieldFloatOrError(this.near_z);
      final float fz = SBTextFieldUtilities.getFieldFloatOrError(this.far_z);
      final float as = SBTextFieldUtilities.getFieldFloatOrError(this.aspect);
      final float fd = SBTextFieldUtilities.getFieldFloatOrError(this.fov);
      final float fr = (float) Math.toRadians(fd);
      ProjectionMatrix.makePerspective(this.matrix_temp, nz, fz, as, fr);
      return new RMatrixI4x4F<RTransformProjection>(this.matrix_temp);
    }

    void setDefaults()
    {
      this.near_z.setText(Float.toString(PerspectiveControls.DEFAUT_NEAR_Z));
      this.far_z.setText(Float.toString(PerspectiveControls.DEFAULT_FAR_Z));
      this.aspect.setText(Float.toString(PerspectiveControls.DEFAULT_ASPECT));
      this.fov.setText(Float.toString(PerspectiveControls.DEFAULT_FOV));
    }
  }

  interface MatrixCreator
  {
    @Nonnull RMatrixI4x4F<RTransformProjection> getMatrix()
      throws SBExceptionInputError,
        ConstraintError;
  }

  private static final class OrthographicControls implements MatrixCreator
  {
    private static final float         DEFAULT_LEFT   = -5.0f;
    private static final float         DEFAULT_RIGHT  = 5.0f;
    private static final float         DEFAULT_TOP    = 5.0f;
    private static final float         DEFAULT_BOTTOM = -5.0f;
    private static final float         DEFAULT_NEAR   = 1.0f;
    private static final float         DEFAULT_FAR    = 100.f;

    private final @Nonnull JTextField  left;
    private final @Nonnull JTextField  right;
    private final @Nonnull JTextField  bottom;
    private final @Nonnull JTextField  top;
    private final @Nonnull JTextField  near;
    private final @Nonnull JTextField  far;
    private final @Nonnull JButton     defaults;
    private final @Nonnull MatrixM4x4F matrix_temp;

    OrthographicControls()
    {
      this.matrix_temp = new MatrixM4x4F();

      this.left = new JTextField();
      this.right = new JTextField();
      this.top = new JTextField();
      this.bottom = new JTextField();
      this.near = new JTextField();
      this.far = new JTextField();

      this.setDefaults();

      this.defaults = new JButton("Defaults");
      this.defaults.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          OrthographicControls.this.setDefaults();
        }
      });
    }

    void add(
      final IRowCreator group)
    {
      group.grid().add(new JLabel("Left")).add(this.left, 3);
      group.grid().add(new JLabel("Right")).add(this.right, 3);
      group.grid().add(new JLabel("Bottom")).add(this.bottom, 3);
      group.grid().add(new JLabel("Top")).add(this.top, 3);
      group.grid().add(new JLabel("Near")).add(this.near, 3);
      group.grid().add(new JLabel("Far")).add(this.far, 3);
      group.right().add(this.defaults);
    }

    @Override public @Nonnull RMatrixI4x4F<RTransformProjection> getMatrix()
      throws SBExceptionInputError,
        ConstraintError
    {
      final float l = SBTextFieldUtilities.getFieldFloatOrError(this.left);
      final float r = SBTextFieldUtilities.getFieldFloatOrError(this.right);
      final float t = SBTextFieldUtilities.getFieldFloatOrError(this.top);
      final float b = SBTextFieldUtilities.getFieldFloatOrError(this.bottom);
      final float n = SBTextFieldUtilities.getFieldFloatOrError(this.near);
      final float f = SBTextFieldUtilities.getFieldFloatOrError(this.far);

      ProjectionMatrix.makeOrthographic(this.matrix_temp, l, r, b, t, n, f);
      return new RMatrixI4x4F<RTransformProjection>(this.matrix_temp);
    }

    protected void setDefaults()
    {
      this.left.setText(Float.toString(OrthographicControls.DEFAULT_LEFT));
      this.right.setText(Float.toString(OrthographicControls.DEFAULT_RIGHT));
      this.top.setText(Float.toString(OrthographicControls.DEFAULT_TOP));
      this.bottom
        .setText(Float.toString(OrthographicControls.DEFAULT_BOTTOM));
      this.near.setText(Float.toString(OrthographicControls.DEFAULT_NEAR));
      this.far.setText(Float.toString(OrthographicControls.DEFAULT_FAR));
    }
  }

  private static final class FrustumControls implements MatrixCreator
  {
    private static final float         DEFAULT_LEFT   = -5.0f;
    private static final float         DEFAULT_RIGHT  = 5.0f;
    private static final float         DEFAULT_TOP    = 5.0f;
    private static final float         DEFAULT_BOTTOM = -5.0f;
    private static final float         DEFAULT_NEAR   = 1.0f;
    private static final float         DEFAULT_FAR    = 100.f;

    private final @Nonnull JTextField  left;
    private final @Nonnull JTextField  right;
    private final @Nonnull JTextField  bottom;
    private final @Nonnull JTextField  top;
    private final @Nonnull JTextField  near;
    private final @Nonnull JTextField  far;
    private final @Nonnull JButton     defaults;
    private final @Nonnull MatrixM4x4F matrix_temp;

    FrustumControls()
    {
      this.matrix_temp = new MatrixM4x4F();

      this.left = new JTextField();
      this.right = new JTextField();
      this.top = new JTextField();
      this.bottom = new JTextField();
      this.near = new JTextField();
      this.far = new JTextField();

      this.setDefaults();

      this.defaults = new JButton("Defaults");
      this.defaults.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          FrustumControls.this.setDefaults();
        }
      });
    }

    void add(
      final IRowCreator group)
    {
      group.grid().add(new JLabel("Left")).add(this.left, 3);
      group.grid().add(new JLabel("Right")).add(this.right, 3);
      group.grid().add(new JLabel("Bottom")).add(this.bottom, 3);
      group.grid().add(new JLabel("Top")).add(this.top, 3);
      group.grid().add(new JLabel("Near")).add(this.near, 3);
      group.grid().add(new JLabel("Far")).add(this.far, 3);
      group.right().add(this.defaults);
    }

    @Override public @Nonnull RMatrixI4x4F<RTransformProjection> getMatrix()
      throws SBExceptionInputError,
        ConstraintError
    {
      final float l = SBTextFieldUtilities.getFieldFloatOrError(this.left);
      final float r = SBTextFieldUtilities.getFieldFloatOrError(this.right);
      final float t = SBTextFieldUtilities.getFieldFloatOrError(this.top);
      final float b = SBTextFieldUtilities.getFieldFloatOrError(this.bottom);
      final float n = SBTextFieldUtilities.getFieldFloatOrError(this.near);
      final float f = SBTextFieldUtilities.getFieldFloatOrError(this.far);

      final float c0r0 = (2.0f * n) / (r - l);
      final float c1r1 = (2.0f * n) / (t - b);
      final float c2r0 = (r + l) / (r - l);
      final float c2r1 = (t + b) / (t - b);
      final float c2r2;
      final float c3r2;

      if (f >= Float.POSITIVE_INFINITY) {
        c2r2 = -1.0f;
        c3r2 = -2.0f * n;
      } else {
        c2r2 = -(f - n) / (f - n);
        c3r2 = -(2.0f * f * n) / (f - n);
      }

      final VectorI4F c0 = new VectorI4F(c0r0, 0.0f, 0.0f, 0.0f);
      final VectorI4F c1 = new VectorI4F(0.0f, c1r1, 0.0f, 0.0f);
      final VectorI4F c2 = new VectorI4F(c2r0, c2r1, c2r2, -1.0f);
      final VectorI4F c3 = new VectorI4F(0.0f, 0.0f, c3r2, 0.0f);

      return new RMatrixI4x4F<RTransformProjection>(c0, c1, c2, c3);
    }

    protected void setDefaults()
    {
      this.left.setText(Float.toString(FrustumControls.DEFAULT_LEFT));
      this.right.setText(Float.toString(FrustumControls.DEFAULT_RIGHT));
      this.top.setText(Float.toString(FrustumControls.DEFAULT_TOP));
      this.bottom.setText(Float.toString(FrustumControls.DEFAULT_BOTTOM));
      this.near.setText(Float.toString(FrustumControls.DEFAULT_NEAR));
      this.far.setText(Float.toString(FrustumControls.DEFAULT_FAR));
    }
  }

  static enum ProjectionType
  {
    PROJECTION_FRUSTUM("Frustum"),
    PROJECTION_ORTHO("Orthographic"),
    PROJECTION_PERSPECTIVE("Perspective"),
    PROJECTION_CUSTOM("Custom");

    private final @Nonnull String name;

    private ProjectionType(
      final @Nonnull String name)
    {
      this.name = name;
    }

    public @Nonnull String getName()
    {
      return this.name;
    }

    @Override public @Nonnull String toString()
    {
      return this.name;
    }
  }

  protected final @Nonnull SBMatrix4x4Fields<RTransformProjection>                        matrix;
  private final @Nonnull SBProjectionMatrixPanel.FrustumControls                          frustum_controls;
  private final @Nonnull SBProjectionMatrixPanel.OrthographicControls                     ortho_controls;
  private final @Nonnull SBProjectionMatrixPanel.PerspectiveControls                      perspective_controls;
  private final @Nonnull SBProjectionMatrixPanel.CustomControls                           custom_controls;
  protected final @Nonnull JComboBox<SBProjectionMatrixPanel.ProjectionType>              selector;
  protected final @Nonnull EnumMap<SBProjectionMatrixPanel.ProjectionType, MatrixCreator> controls;

  SBProjectionMatrixPanel(
    final @Nonnull JFrame owner)
  {
    this.matrix = new SBMatrix4x4Fields<RTransformProjection>();

    this.frustum_controls = new FrustumControls();
    this.ortho_controls = new OrthographicControls();
    this.perspective_controls = new PerspectiveControls();
    this.custom_controls = new CustomControls();

    this.controls =
      new EnumMap<SBProjectionMatrixPanel.ProjectionType, MatrixCreator>(
        SBProjectionMatrixPanel.ProjectionType.class);
    this.controls.put(ProjectionType.PROJECTION_CUSTOM, this.custom_controls);
    this.controls.put(
      ProjectionType.PROJECTION_FRUSTUM,
      this.frustum_controls);
    this.controls.put(ProjectionType.PROJECTION_ORTHO, this.ortho_controls);
    this.controls.put(
      ProjectionType.PROJECTION_PERSPECTIVE,
      this.perspective_controls);

    this.selector = new JComboBox<SBProjectionMatrixPanel.ProjectionType>();
    this.selector.addItem(ProjectionType.PROJECTION_FRUSTUM);
    this.selector.addItem(ProjectionType.PROJECTION_ORTHO);
    this.selector.addItem(ProjectionType.PROJECTION_PERSPECTIVE);
    this.selector.addItem(ProjectionType.PROJECTION_CUSTOM);

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(new JLabel("Projection")).add(this.selector, 3);

    final RowGroup fr_group = new RowGroup();
    final RowGroup or_group = new RowGroup();
    final RowGroup pe_group = new RowGroup();
    final RowGroup cu_group = new RowGroup();

    this.frustum_controls.add(dg.row().group(fr_group));
    this.ortho_controls.add(dg.row().group(or_group));
    this.perspective_controls.add(dg.row().group(pe_group));
    this.custom_controls.add(dg.row().group(cu_group));

    or_group.hide();
    cu_group.hide();
    pe_group.hide();
    fr_group.forceShow();

    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final SBProjectionMatrixPanel.ProjectionType item =
          (SBProjectionMatrixPanel.ProjectionType) SBProjectionMatrixPanel.this.selector
            .getSelectedItem();
        if (item != null) {
          switch (item) {
            case PROJECTION_CUSTOM:
            {
              pe_group.hide();
              fr_group.hide();
              or_group.hide();
              cu_group.forceShow();
              break;
            }
            case PROJECTION_FRUSTUM:
            {
              or_group.hide();
              cu_group.hide();
              pe_group.hide();
              fr_group.forceShow();
              break;
            }
            case PROJECTION_ORTHO:
            {
              cu_group.hide();
              pe_group.hide();
              fr_group.hide();
              or_group.forceShow();
              break;
            }
            case PROJECTION_PERSPECTIVE:
            {
              fr_group.hide();
              or_group.hide();
              cu_group.hide();
              pe_group.forceShow();
              break;
            }
          }
        }

        owner.pack();
      }
    });

    dg.row().grid().add(new JSeparator());
    dg.row().left().add(new JLabel("Projection matrix"));

    for (int r = 0; r < 4; ++r) {
      final JTextField f0 = this.matrix.getRowColumnField(r, 0);
      final JTextField f1 = this.matrix.getRowColumnField(r, 1);
      final JTextField f2 = this.matrix.getRowColumnField(r, 2);
      final JTextField f3 = this.matrix.getRowColumnField(r, 3);

      f0.setEditable(false);
      f1.setEditable(false);
      f2.setEditable(false);
      f3.setEditable(false);

      dg.row().grid().add(f0).add(f1).add(f2).add(f3);
    }
  }

  public @Nonnull RMatrixI4x4F<RTransformProjection> getMatrix()
    throws SBExceptionInputError,
      ConstraintError
  {
    final SBProjectionMatrixPanel.ProjectionType item =
      (SBProjectionMatrixPanel.ProjectionType) this.selector
        .getSelectedItem();

    final MatrixCreator mc = this.controls.get(item);
    final RMatrixI4x4F<RTransformProjection> m = mc.getMatrix();

    this.matrix.setMatrix(m);
    return m;
  }
}
