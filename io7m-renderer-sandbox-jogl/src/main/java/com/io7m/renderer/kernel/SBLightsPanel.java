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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorM3F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

final class SBLightsPanel extends JPanel implements SBSceneChangeListener
{
  private static class LightEditDialog extends JFrame
  {
    private static final long                   serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    private final @Nonnull LightEditDialogPanel panel;

    public LightEditDialog(
      final @Nonnull SBSceneControllerLights controller,
      final @Nonnull KLight light,
      final @Nonnull LightsTableModel light_table_model)
      throws IOException,
        ConstraintError
    {
      this.panel =
        new LightEditDialogPanel(this, controller, light, light_table_model);
      this.setTitle("Edit light...");
      this.getContentPane().add(this.panel);
    }

    public LightEditDialog(
      final @Nonnull SBSceneControllerLights controller,
      final @Nonnull LightsTableModel light_table_model)
      throws IOException,
        ConstraintError
    {
      this.panel =
        new LightEditDialogPanel(this, controller, null, light_table_model);
      this.setTitle("Create light...");
      this.getContentPane().add(this.panel);
    }
  }

  private static class LightEditDialogPanel extends JPanel
  {
    private static class LightControlsDirectional
    {
      private final @Nonnull JTextField direction_x;
      private final @Nonnull JTextField direction_y;
      private final @Nonnull JTextField direction_z;

      LightControlsDirectional()
      {
        this.direction_x = new JTextField("0.0");
        this.direction_y = new JTextField("1.0");
        this.direction_z = new JTextField("0.0");
      }

      public void add(
        final IRowCreator group)
      {
        group
          .grid()
          .add(new JLabel("Direction"))
          .add(this.direction_x)
          .add(this.direction_y)
          .add(this.direction_z, 2);
      }

      @Nonnull RVectorM3F<RSpaceWorld> getDirection()
        throws SBExceptionInputError
      {
        final RVectorM3F<RSpaceWorld> v = new RVectorM3F<RSpaceWorld>();
        v.x = SBTextFieldUtilities.getFieldFloatOrError(this.direction_x);
        v.y = SBTextFieldUtilities.getFieldFloatOrError(this.direction_y);
        v.z = SBTextFieldUtilities.getFieldFloatOrError(this.direction_z);
        return v;
      }

      void setContents(
        final KLight.KDirectional light)
      {
        final RVectorReadable3F<RSpaceWorld> dir = light.getDirection();
        this.direction_x.setText(Float.toString(dir.getXF()));
        this.direction_y.setText(Float.toString(dir.getYF()));
        this.direction_z.setText(Float.toString(dir.getZF()));
      }
    }

    private static class LightControlsSphere
    {
      private final @Nonnull JTextField       position_x;
      private final @Nonnull JTextField       position_y;
      private final @Nonnull JTextField       position_z;

      protected final @Nonnull SBFloatHSlider radius;
      protected final @Nonnull SBFloatHSlider falloff;

      public LightControlsSphere()
        throws ConstraintError
      {
        this.position_x = new JTextField("0.0");
        this.position_y = new JTextField("1.0");
        this.position_z = new JTextField("0.0");

        this.radius = new SBFloatHSlider("Radius", 0.0f, 128.0f);
        this.falloff = new SBFloatHSlider("Falloff", 0.0f, 64.0f);
      }

      public void add(
        final IRowCreator group)
      {
        group
          .grid()
          .add(new JLabel("Position"))
          .add(this.position_x)
          .add(this.position_y)
          .add(this.position_z, 2);

        group
          .grid()
          .add(this.radius.getLabel())
          .add(this.radius.getSlider(), 3)
          .add(this.radius.getField());

        group
          .grid()
          .add(this.falloff.getLabel())
          .add(this.falloff.getSlider(), 3)
          .add(this.falloff.getField());
      }

      public @Nonnull RVectorM3F<RSpaceWorld> getPosition()
        throws SBExceptionInputError
      {
        final RVectorM3F<RSpaceWorld> v = new RVectorM3F<RSpaceWorld>();
        v.x = SBTextFieldUtilities.getFieldFloatOrError(this.position_x);
        v.y = SBTextFieldUtilities.getFieldFloatOrError(this.position_y);
        v.z = SBTextFieldUtilities.getFieldFloatOrError(this.position_z);
        return v;
      }

      public void setContents(
        final KLight.KSphere light)
      {
        final RVectorReadable3F<RSpaceWorld> p = light.getPosition();
        this.position_x.setText(Float.toString(p.getXF()));
        this.position_y.setText(Float.toString(p.getYF()));
        this.position_z.setText(Float.toString(p.getZF()));

        this.radius.setCurrent(light.getRadius());
        this.falloff.setCurrent(light.getExponent());
      }

      public float getRadius()
        throws SBExceptionInputError
      {
        return SBTextFieldUtilities.getFieldFloatOrError(this.radius
          .getField());
      }

      public float getExponent()
        throws SBExceptionInputError
      {
        return SBTextFieldUtilities.getFieldFloatOrError(this.falloff
          .getField());
      }
    }

    private static final long                       serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    protected final @Nonnull JTextField             colour_r;
    protected final @Nonnull JTextField             colour_g;
    protected final @Nonnull JTextField             colour_b;
    protected final @Nonnull JButton                colour_select;
    protected final @Nonnull SBFloatHSlider         intensity;
    protected final @Nonnull SBLightTypeSelector    type_select;
    private final @Nonnull LightControlsDirectional directional_controls;
    private final @Nonnull LightControlsSphere      sphere_controls;
    private final @Nonnull JLabel                   error_text;
    private final @Nonnull JLabel                   error_icon;
    private final @Nonnull SBSceneControllerLights  controller;
    protected final @Nonnull LightsTableModel       light_table_model;

    public LightEditDialogPanel(
      final @Nonnull LightEditDialog window,
      final @Nonnull SBSceneControllerLights controller,
      final @CheckForNull KLight light,
      final @Nonnull LightsTableModel light_table_model)
      throws IOException,
        ConstraintError
    {
      this.controller = controller;
      this.light_table_model = light_table_model;

      this.error_text = new JLabel("Some informative error text");
      this.error_icon = SBIcons.makeErrorIcon();
      this.error_text.setVisible(false);
      this.error_icon.setVisible(false);

      this.type_select = new SBLightTypeSelector();
      this.colour_r = new JTextField("1.0");
      this.colour_g = new JTextField("1.0");
      this.colour_b = new JTextField("1.0");
      this.colour_select = new JButton("Select...");
      this.colour_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final Color c =
            JColorChooser.showDialog(
              LightEditDialogPanel.this,
              "Select colour...",
              Color.WHITE);
          if (c != null) {
            final float[] rgb = c.getRGBColorComponents(null);
            LightEditDialogPanel.this.colour_r.setText(Float.toString(rgb[0]));
            LightEditDialogPanel.this.colour_g.setText(Float.toString(rgb[1]));
            LightEditDialogPanel.this.colour_b.setText(Float.toString(rgb[2]));
          }
        }
      });

      this.intensity = new SBFloatHSlider("Intensity", 0.0f, 2.0f);

      final DesignGridLayout dg = new DesignGridLayout(this);

      if (light != null) {
        final JTextField id_field = new JTextField(light.getID().toString());
        id_field.setEditable(false);
        dg.row().grid().add(new JLabel("ID")).add(id_field, 4);
      }

      dg
        .row()
        .grid()
        .add(new JLabel("Colour"))
        .add(this.colour_r)
        .add(this.colour_g)
        .add(this.colour_b)
        .add(this.colour_select);

      dg
        .row()
        .grid()
        .add(this.intensity.getLabel())
        .add(this.intensity.getSlider(), 3)
        .add(this.intensity.getField());
      dg.row().grid().add(new JLabel("Type")).add(this.type_select, 4);

      this.directional_controls = new LightControlsDirectional();
      this.sphere_controls = new LightControlsSphere();

      final RowGroup directional_group = new RowGroup();
      final RowGroup sphere_group = new RowGroup();

      this.directional_controls.add(dg.row().group(directional_group));
      this.sphere_controls.add(dg.row().group(sphere_group));

      sphere_group.hide();
      directional_group.forceShow();

      final JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          SBWindowUtilities.closeWindow(window);
        }
      });

      final JButton apply = new JButton("Apply");
      apply.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            LightEditDialogPanel.this.saveLight(light);
          } catch (final SBExceptionInputError x) {
            LightEditDialogPanel.this.setError(x.getMessage());
          } catch (final ConstraintError x) {
            LightEditDialogPanel.this.setError(x.getMessage());
          }
        }
      });

      final JButton finish = new JButton("Finish");
      finish.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            LightEditDialogPanel.this.saveLight(light);
            SBWindowUtilities.closeWindow(window);
          } catch (final SBExceptionInputError x) {
            LightEditDialogPanel.this.setError(x.getMessage());
          } catch (final ConstraintError x) {
            LightEditDialogPanel.this.setError(x.getMessage());
          }
        }
      });

      dg.emptyRow();
      dg.row().grid().add(apply).add(cancel).add(finish);
      dg.emptyRow();
      dg.row().left().add(this.error_icon).add(this.error_text);

      this.type_select.addItemListener(new ItemListener() {
        @Override public void itemStateChanged(
          final @Nonnull ItemEvent e)
        {
          final KLight.Type selected =
            (KLight.Type) LightEditDialogPanel.this.type_select
              .getSelectedItem();

          switch (selected) {
            case LIGHT_DIRECTIONAL:
            {
              sphere_group.hide();
              directional_group.forceShow();
              window.pack();
              break;
            }
            case LIGHT_SPHERE:
            {
              directional_group.hide();
              sphere_group.forceShow();
              window.pack();
              break;
            }
          }
        }
      });

      if (light != null) {
        final KLight.Type type = light.getType();
        this.type_select.setSelectedItem(type);

        this.setColour(light.getColour());
        this.setIntensity(light.getIntensity());

        switch (light.getType()) {
          case LIGHT_DIRECTIONAL:
          {
            final KLight.KDirectional d = (KLight.KDirectional) light;
            this.directional_controls.setContents(d);
            break;
          }
          case LIGHT_SPHERE:
          {
            final KSphere p = (KLight.KSphere) light;
            this.sphere_controls.setContents(p);
            break;
          }
        }
      }
    }

    private @Nonnull RVectorReadable3F<RSpaceRGB> getColour()
      throws SBExceptionInputError
    {
      final RVectorM3F<RSpaceRGB> v = new RVectorM3F<RSpaceRGB>();
      v.x = SBTextFieldUtilities.getFieldFloatOrError(this.colour_r);
      v.y = SBTextFieldUtilities.getFieldFloatOrError(this.colour_g);
      v.z = SBTextFieldUtilities.getFieldFloatOrError(this.colour_b);
      return v;
    }

    private float getIntensity()
      throws SBExceptionInputError
    {
      return SBTextFieldUtilities.getFieldFloatOrError(this.intensity
        .getField());
    }

    protected @Nonnull KLight makeLight(
      final @Nonnull Integer id,
      final @Nonnull KLight.Type type,
      final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
      throws SBExceptionInputError
    {
      switch (type) {
        case LIGHT_DIRECTIONAL:
        {
          final RVectorM3F<RSpaceWorld> direction =
            this.directional_controls.getDirection();

          final KLight l =
            new KLight.KDirectional(
              id,
              direction,
              colour,
              this.getIntensity());
          return l;
        }
        case LIGHT_SPHERE:
        {
          final RVectorM3F<RSpaceWorld> position =
            this.sphere_controls.getPosition();
          final float radius = this.sphere_controls.getRadius();
          final float exponent = this.sphere_controls.getExponent();

          final KLight l =
            new KLight.KSphere(
              id,
              colour,
              this.getIntensity(),
              position,
              radius,
              exponent);
          return l;
        }
      }

      throw new UnreachableCodeException();
    }

    protected void saveLight(
      final @CheckForNull KLight initial)
      throws SBExceptionInputError,
        ConstraintError
    {
      final Integer id =
        (initial == null) ? this.controller.sceneLightFreshID() : initial
          .getID();

      final KLight light =
        this.makeLight(
          id,
          (KLight.Type) this.type_select.getSelectedItem(),
          this.getColour());

      this.controller.sceneLightAdd(light);
      this.light_table_model.refreshLights();
      this.unsetError();
    }

    private void setColour(
      final RVectorReadable3F<RSpaceRGB> color)
    {
      this.colour_r.setText(Float.toString(color.getXF()));
      this.colour_g.setText(Float.toString(color.getYF()));
      this.colour_b.setText(Float.toString(color.getZF()));
    }

    protected void setError(
      final @Nonnull String message)
    {
      this.error_icon.setVisible(true);
      this.error_text.setText(message);
      this.error_text.setVisible(true);
    }

    private void setIntensity(
      final float value)
    {
      this.intensity.setCurrent(value);
    }

    protected void unsetError()
    {
      this.error_icon.setVisible(false);
      this.error_text.setVisible(false);
    }
  }

  private static class LightsTable extends JTable
  {
    private static final long               serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final @Nonnull LightsTableModel model;

    public LightsTable(
      final LightsTableModel model)
    {
      super(model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.getColumnModel().getColumn(0).setPreferredWidth(16);
      this.getColumnModel().getColumn(1).setPreferredWidth(300);

      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = model;
    }
  }

  private static class LightsTableModel extends AbstractTableModel
  {
    private static final long                           serialVersionUID;
    private final @Nonnull String[]                     column_names;
    private final @Nonnull ArrayList<ArrayList<String>> data;
    private final @Nonnull Log                          log;
    private final @Nonnull SBSceneControllerLights      controller;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public LightsTableModel(
      final @Nonnull SBSceneControllerLights controller,
      final @Nonnull Log log)
    {
      this.log = new Log(log, "light-table");
      this.column_names = new String[] { "ID", "Type" };
      this.data = new ArrayList<ArrayList<String>>();
      this.controller = controller;
    }

    @Override public int getColumnCount()
    {
      return this.column_names.length;
    }

    @Override public String getColumnName(
      final int column)
    {
      return this.column_names[column];
    }

    protected @Nonnull KLight getLightAt(
      final int row)
      throws ConstraintError
    {
      final ArrayList<String> row_data = this.data.get(row);
      assert row_data != null;
      final String id_text = row_data.get(0);
      final Integer id = Integer.valueOf(id_text);
      assert this.controller.sceneLightExists(id);
      return this.controller.sceneLightGet(id);
    }

    @Override public int getRowCount()
    {
      return this.data.size();
    }

    @Override public Object getValueAt(
      final int rowIndex,
      final int columnIndex)
    {
      return this.data.get(rowIndex).get(columnIndex);
    }

    void refreshLights()
    {
      this.data.clear();
      final Collection<KLight> lights = this.controller.sceneLightsGetAll();

      for (final KLight l : lights) {
        final ArrayList<String> row = new ArrayList<String>();
        row.add(l.getID().toString());
        row.add(l.getType().getName());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static final long                 serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }

  protected final @Nonnull LightsTableModel lights_model;
  protected final @Nonnull LightsTable      lights;
  protected final @Nonnull JScrollPane      scroller;

  public SBLightsPanel(
    final @Nonnull SBSceneControllerLights controller,
    final @Nonnull Log log)
  {
    this.lights_model = new LightsTableModel(controller, log);
    this.lights = new LightsTable(this.lights_model);
    this.scroller = new JScrollPane(this.lights);

    final JButton add = new JButton("Add...");
    add.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final LightEditDialog dialog =
            new LightEditDialog(controller, SBLightsPanel.this.lights_model);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final IOException x) {
          log.critical("Unable to open edit dialogue: " + x.getMessage());
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
      }
    });

    final JButton edit = new JButton("Edit...");
    edit.setEnabled(false);
    edit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final int view_row = SBLightsPanel.this.lights.getSelectedRow();
        assert view_row != -1;
        final int model_row =
          SBLightsPanel.this.lights.convertRowIndexToModel(view_row);
        KLight light;

        try {
          light = SBLightsPanel.this.lights_model.getLightAt(model_row);
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
        assert light != null;

        try {
          final LightEditDialog dialog =
            new LightEditDialog(
              controller,
              light,
              SBLightsPanel.this.lights_model);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final IOException x) {
          log.critical("Unable to open edit dialogue: " + x.getMessage());
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
      }
    });

    final JButton remove = new JButton("Remove...");
    remove.setEnabled(false);
    remove.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final int view_row = SBLightsPanel.this.lights.getSelectedRow();
          assert view_row != -1;
          final int model_row =
            SBLightsPanel.this.lights.convertRowIndexToModel(view_row);
          KLight light;

          light = SBLightsPanel.this.lights_model.getLightAt(model_row);
          assert light != null;
          controller.sceneLightRemove(light.getID());

          SBLightsPanel.this.lights_model.refreshLights();
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
      }
    });

    this.lights.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        @Override public void valueChanged(
          final @Nonnull ListSelectionEvent e)
        {
          if (SBLightsPanel.this.lights.getSelectedRow() == -1) {
            edit.setEnabled(false);
            remove.setEnabled(false);
          } else {
            edit.setEnabled(true);
            remove.setEnabled(true);
          }
        }
      });

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(this.scroller);
    dg.row().grid().add(add).add(edit).add(remove);

    controller.sceneChangeListenerAdd(this);
  }

  @Override public void sceneChanged()
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        SBLightsPanel.this.lights_model.refreshLights();
      }
    });
  }

  @Override public String toString()
  {
    return "[SBLightsPanel]";
  }
}
