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
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionProjective;
import com.io7m.renderer.kernel.SBProjectionDescription.SBProjectionFrustum;
import com.io7m.renderer.kernel.SBProjectionDescription.SBProjectionOrthographic;
import com.io7m.renderer.kernel.SBProjectionDescription.SBProjectionPerspective;

final class SBProjectionMatrixPanel extends JPanel
{
  interface DescriptionCreator
  {
    public @Nonnull SBProjectionDescription getDescription()
      throws SBExceptionInputError,
        ConstraintError;
  }

  private static final class FrustumControls implements DescriptionCreator
  {
    private final @Nonnull JTextField    left;
    private final @Nonnull JTextField    right;
    private final @Nonnull JTextField    bottom;
    private final @Nonnull JTextField    top;
    private final @Nonnull JTextField    near;
    private final @Nonnull JTextField    far;
    private final @Nonnull JButton       defaults;
    private @Nonnull SBProjectionFrustum current;

    FrustumControls(
      final @Nonnull SBProjectionFrustum initial)
    {
      this.left = new JTextField();
      this.right = new JTextField();
      this.top = new JTextField();
      this.bottom = new JTextField();
      this.near = new JTextField();
      this.far = new JTextField();

      this.set(initial);

      this.defaults = new JButton("Defaults");
      this.defaults.addActionListener(new ActionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          actionPerformed(
            final @Nonnull ActionEvent e)
        {
          try {
            FrustumControls.this.set(new SBProjectionFrustum());
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException(x);
          }
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

    @Override public @Nonnull SBProjectionDescription getDescription()
      throws SBExceptionInputError,
        ConstraintError
    {
      this.current =
        new SBProjectionFrustum(
          SBTextFieldUtilities.getFieldFloatOrError(this.left),
          SBTextFieldUtilities.getFieldFloatOrError(this.right),
          SBTextFieldUtilities.getFieldFloatOrError(this.bottom),
          SBTextFieldUtilities.getFieldFloatOrError(this.top),
          SBTextFieldUtilities.getFieldFloatOrError(this.near),
          SBTextFieldUtilities.getFieldFloatOrError(this.far));
      return this.current;
    }

    private void set(
      final @Nonnull SBProjectionFrustum p)
    {
      this.current = p;
      this.left.setText(Double.toString(p.getLeft()));
      this.right.setText(Double.toString(p.getRight()));
      this.top.setText(Double.toString(p.getTop()));
      this.bottom.setText(Double.toString(p.getBottom()));
      this.near.setText(Double.toString(p.getNear()));
      this.far.setText(Double.toString(p.getFar()));
    }
  }

  private static final class OrthographicControls implements
    DescriptionCreator
  {
    private final @Nonnull JTextField         left;
    private final @Nonnull JTextField         right;
    private final @Nonnull JTextField         bottom;
    private final @Nonnull JTextField         top;
    private final @Nonnull JTextField         near;
    private final @Nonnull JTextField         far;
    private final @Nonnull JButton            defaults;
    private @Nonnull SBProjectionOrthographic current;

    OrthographicControls(
      final @Nonnull SBProjectionOrthographic initial)
    {
      this.left = new JTextField();
      this.right = new JTextField();
      this.top = new JTextField();
      this.bottom = new JTextField();
      this.near = new JTextField();
      this.far = new JTextField();

      this.set(initial);

      this.defaults = new JButton("Defaults");
      this.defaults.addActionListener(new ActionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          actionPerformed(
            final @Nonnull ActionEvent e)
        {
          try {
            OrthographicControls.this.set(new SBProjectionOrthographic());
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException(x);
          }
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

    @Override public @Nonnull SBProjectionDescription getDescription()
      throws SBExceptionInputError,
        ConstraintError
    {
      this.current =
        new SBProjectionOrthographic(
          SBTextFieldUtilities.getFieldFloatOrError(this.left),
          SBTextFieldUtilities.getFieldFloatOrError(this.right),
          SBTextFieldUtilities.getFieldFloatOrError(this.bottom),
          SBTextFieldUtilities.getFieldFloatOrError(this.top),
          SBTextFieldUtilities.getFieldFloatOrError(this.near),
          SBTextFieldUtilities.getFieldFloatOrError(this.far));
      return this.current;
    }

    private void set(
      final @Nonnull SBProjectionOrthographic p)
    {
      this.current = p;
      this.left.setText(Double.toString(p.getLeft()));
      this.right.setText(Double.toString(p.getRight()));
      this.top.setText(Double.toString(p.getTop()));
      this.bottom.setText(Double.toString(p.getBottom()));
      this.near.setText(Double.toString(p.getNear()));
      this.far.setText(Double.toString(p.getFar()));
    }
  }

  private final class PerspectiveControls implements DescriptionCreator
  {
    private final @Nonnull JTextField        near_z;
    private final @Nonnull JTextField        far_z;
    private final @Nonnull JTextField        aspect;
    private final @Nonnull JTextField        fov;
    private final @Nonnull JButton           defaults;
    private @Nonnull SBProjectionPerspective current;

    PerspectiveControls(
      final @Nonnull SBProjectionPerspective initial)
    {
      this.near_z = new JTextField();
      this.far_z = new JTextField();
      this.aspect = new JTextField();
      this.fov = new JTextField();

      this.set(initial);

      this.defaults = new JButton("Defaults");
      this.defaults.addActionListener(new ActionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          actionPerformed(
            final @Nonnull ActionEvent e)
        {
          try {
            PerspectiveControls.this.set(new SBProjectionPerspective());
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException(x);
          }
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

    @Override public @Nonnull SBProjectionDescription getDescription()
      throws SBExceptionInputError,
        ConstraintError
    {
      this.current =
        new SBProjectionPerspective(
          SBTextFieldUtilities.getFieldFloatOrError(this.near_z),
          SBTextFieldUtilities.getFieldFloatOrError(this.far_z),
          SBTextFieldUtilities.getFieldFloatOrError(this.aspect),
          Math.toRadians(SBTextFieldUtilities.getFieldFloatOrError(this.fov)));
      return this.current;
    }

    private void set(
      final SBProjectionPerspective p)
    {
      this.current = p;
      this.near_z.setText(Double.toString(p.getNear()));
      this.far_z.setText(Double.toString(p.getFar()));
      this.aspect.setText(Double.toString(p.getAspect()));
      this.fov.setText(Double.toString(Math.toDegrees(p.getHorizontalFOV())));
    }
  }

  private static final long                                                          serialVersionUID;

  static {
    serialVersionUID = 1220191040371483205L;
  }

  protected final @Nonnull SBMatrix4x4Fields<RTransformProjection>                   matrix;
  private final @Nonnull SBProjectionMatrixPanel.FrustumControls                     frustum_controls;
  private final @Nonnull SBProjectionMatrixPanel.OrthographicControls                ortho_controls;
  private final @Nonnull SBProjectionMatrixPanel.PerspectiveControls                 perspective_controls;
  protected final @Nonnull JComboBox<SBProjectionDescription.Type>                   selector;
  protected final @Nonnull EnumMap<SBProjectionDescription.Type, DescriptionCreator> controls;
  protected final @Nonnull MatrixM4x4F                                               temporary;
  private final @Nonnull RowGroup                                                    fr_group;
  private final @Nonnull RowGroup                                                    or_group;
  private final @Nonnull RowGroup                                                    pe_group;
  private final @Nonnull JFrame                                                      owner;

  private void setCurrentType(
    final @Nonnull SBProjectionDescription.Type item)
  {
    this.selector.setSelectedItem(item);

    switch (item) {
      case PROJECTION_FRUSTUM:
      {
        this.or_group.hide();
        this.pe_group.hide();
        this.fr_group.forceShow();
        break;
      }
      case PROJECTION_ORTHOGRAPHIC:
      {
        this.pe_group.hide();
        this.fr_group.hide();
        this.or_group.forceShow();
        break;
      }
      case PROJECTION_PERSPECTIVE:
      {
        this.fr_group.hide();
        this.or_group.hide();
        this.pe_group.forceShow();
        break;
      }
    }

    this.owner.pack();
  }

  SBProjectionMatrixPanel(
    final @Nonnull JFrame owner,
    final boolean show_matrix)
    throws ConstraintError
  {
    this.owner = owner;
    this.matrix = new SBMatrix4x4Fields<RTransformProjection>();
    this.temporary = new MatrixM4x4F();

    this.frustum_controls =
      new FrustumControls(new SBProjectionDescription.SBProjectionFrustum());
    this.ortho_controls =
      new OrthographicControls(
        new SBProjectionDescription.SBProjectionOrthographic());
    this.perspective_controls =
      new PerspectiveControls(
        new SBProjectionDescription.SBProjectionPerspective());

    this.controls =
      new EnumMap<SBProjectionDescription.Type, DescriptionCreator>(
        SBProjectionDescription.Type.class);
    this.controls.put(
      SBProjectionDescription.Type.PROJECTION_FRUSTUM,
      this.frustum_controls);
    this.controls.put(
      SBProjectionDescription.Type.PROJECTION_ORTHOGRAPHIC,
      this.ortho_controls);
    this.controls.put(
      SBProjectionDescription.Type.PROJECTION_PERSPECTIVE,
      this.perspective_controls);

    this.selector = new JComboBox<SBProjectionDescription.Type>();
    this.selector.addItem(SBProjectionDescription.Type.PROJECTION_FRUSTUM);
    this.selector
      .addItem(SBProjectionDescription.Type.PROJECTION_ORTHOGRAPHIC);
    this.selector
      .addItem(SBProjectionDescription.Type.PROJECTION_PERSPECTIVE);

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(new JLabel("Projection")).add(this.selector, 3);

    this.fr_group = new RowGroup();
    this.or_group = new RowGroup();
    this.pe_group = new RowGroup();

    this.frustum_controls.add(dg.row().group(this.fr_group));
    this.ortho_controls.add(dg.row().group(this.or_group));
    this.perspective_controls.add(dg.row().group(this.pe_group));

    this.or_group.hide();
    this.pe_group.hide();
    this.fr_group.forceShow();

    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final SBProjectionDescription.Type item =
          (SBProjectionDescription.Type) SBProjectionMatrixPanel.this.selector
            .getSelectedItem();
        if (item != null) {
          SBProjectionMatrixPanel.this.setCurrentType(item);
        }
      }
    });

    if (show_matrix) {
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
  }

  public @Nonnull SBProjectionDescription getDescription()
    throws SBExceptionInputError,
      ConstraintError
  {
    final SBProjectionDescription.Type item =
      (SBProjectionDescription.Type) this.selector.getSelectedItem();

    final DescriptionCreator mc = this.controls.get(item);
    return mc.getDescription();
  }

  @SuppressWarnings("synthetic-access") public void setContents(
    final @Nonnull SBLightDescriptionProjective description)
  {
    final SBProjectionDescription p = description.getProjection();
    this.setCurrentType(p.getType());

    switch (p.getType()) {
      case PROJECTION_FRUSTUM:
      {
        this.frustum_controls.set((SBProjectionFrustum) p);
        break;
      }
      case PROJECTION_ORTHOGRAPHIC:
      {
        this.ortho_controls.set((SBProjectionOrthographic) p);
        break;
      }
      case PROJECTION_PERSPECTIVE:
      {
        this.perspective_controls.set((SBProjectionPerspective) p);
        break;
      }
    }
  }
}
