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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;
import net.java.dev.designgridlayout.RowGroup;
import net.java.dev.designgridlayout.Tag;

import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorM3F;
import com.io7m.renderer.RVectorReadable3F;

final class SBLightsPanel extends JPanel
{
  private static class LightEditDialog extends JFrame
  {
    private static final long                   serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    private final @Nonnull LightEditDialogPanel panel;

    public LightEditDialog(
      final @Nonnull KLight light,
      final @Nonnull LightsTableModel data)
    {
      this.panel = new LightEditDialogPanel(this, light, data);
      this.setTitle("Edit light...");
      this.getContentPane().add(this.panel);
    }

    public LightEditDialog(
      final @Nonnull LightsTableModel data)
    {
      this.panel = new LightEditDialogPanel(this, null, data);
      this.setTitle("Create light...");
      this.getContentPane().add(this.panel);
    }
  }

  private static class LightEditDialogPanel extends JPanel
  {
    private static class LightControlsCone
    {
      private final @Nonnull JTextField position_x;
      private final @Nonnull JTextField position_y;
      private final @Nonnull JTextField position_z;

      public LightControlsCone()
      {
        this.position_x = new JTextField("0.0");
        this.position_y = new JTextField("1.0");
        this.position_z = new JTextField("0.0");
      }

      public void add(
        final IRowCreator group)
      {
        group
          .grid()
          .add(new JLabel("Position"))
          .add(this.position_x)
          .add(this.position_y)
          .add(this.position_z);
      }
    }

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
          .add(this.direction_z);
      }

      @Nonnull RVectorM3F<RSpaceWorld> getDirection()
      {
        final RVectorM3F<RSpaceWorld> v = new RVectorM3F<RSpaceWorld>();
        v.x = Float.parseFloat(this.direction_x.getText());
        v.y = Float.parseFloat(this.direction_y.getText());
        v.z = Float.parseFloat(this.direction_z.getText());
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

    private static class LightControlsPoint
    {
      private final @Nonnull JTextField position_x;
      private final @Nonnull JTextField position_y;
      private final @Nonnull JTextField position_z;

      public LightControlsPoint()
      {
        this.position_x = new JTextField("0.0");
        this.position_y = new JTextField("1.0");
        this.position_z = new JTextField("0.0");
      }

      public void add(
        final IRowCreator group)
      {
        group
          .grid()
          .add(new JLabel("Position"))
          .add(this.position_x)
          .add(this.position_y)
          .add(this.position_z);
      }
    }

    private static final long                    serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    protected final @Nonnull JTextField          colour_r;
    protected final @Nonnull JTextField          colour_g;
    protected final @Nonnull JTextField          colour_b;
    protected final @Nonnull JTextField          intensity;
    protected final @Nonnull SBLightTypeSelector type_select;
    private final LightControlsDirectional       directional_controls;
    private final LightControlsPoint             point_controls;
    private final LightControlsCone              cone_controls;

    public LightEditDialogPanel(
      final @Nonnull LightEditDialog window,
      final @CheckForNull KLight light,
      final @Nonnull LightsTableModel data)
    {
      this.type_select = new SBLightTypeSelector();
      this.colour_r = new JTextField("1.0");
      this.colour_g = new JTextField("1.0");
      this.colour_b = new JTextField("1.0");
      this.intensity = new JTextField("1.0");

      final DesignGridLayout dg = new DesignGridLayout(this);

      if (light != null) {
        final JTextField id_field = new JTextField(light.getID().toString());
        id_field.setEditable(false);
        dg.row().grid().add(new JLabel("ID")).add(id_field, 3);
      }

      dg
        .row()
        .grid()
        .add(new JLabel("Colour"))
        .add(this.colour_r)
        .add(this.colour_g)
        .add(this.colour_b);

      dg.row().grid().add(new JLabel("Intensity")).add(this.intensity, 3);
      dg.row().grid().add(new JLabel("Type")).add(this.type_select, 3);

      this.directional_controls = new LightControlsDirectional();
      this.point_controls = new LightControlsPoint();
      this.cone_controls = new LightControlsCone();

      final RowGroup directional_group = new RowGroup();
      final RowGroup point_group = new RowGroup();
      final RowGroup cone_group = new RowGroup();

      this.directional_controls.add(dg.row().group(directional_group));
      this.point_controls.add(dg.row().group(point_group));
      this.cone_controls.add(dg.row().group(cone_group));

      directional_group.show();
      point_group.hide();
      cone_group.hide();

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
          LightEditDialogPanel.this.saveLight(data, light);
        }
      });

      final JButton finish = new JButton("Finish");
      finish.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          LightEditDialogPanel.this.saveLight(data, light);
          SBWindowUtilities.closeWindow(window);
        }
      });

      dg
        .row()
        .bar()
        .add(cancel, Tag.CANCEL)
        .add(apply, Tag.APPLY)
        .add(finish, Tag.FINISH);

      this.type_select.addItemListener(new ItemListener() {
        @Override public void itemStateChanged(
          final @Nonnull ItemEvent e)
        {
          final KLight.Type selected =
            (KLight.Type) LightEditDialogPanel.this.type_select
              .getSelectedItem();

          switch (selected) {
            case LIGHT_CONE:
            {
              directional_group.hide();
              point_group.hide();
              cone_group.show();
              cone_group.show();
              break;
            }
            case LIGHT_DIRECTIONAL:
            {
              cone_group.hide();
              point_group.hide();
              directional_group.show();
              directional_group.show();
              break;
            }
            case LIGHT_POINT:
            {
              directional_group.hide();
              cone_group.hide();
              point_group.show();
              point_group.show();
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
          case LIGHT_CONE:
          {
            break;
          }
          case LIGHT_DIRECTIONAL:
          {
            final KLight.KDirectional d = (KLight.KDirectional) light;
            this.directional_controls.setContents(d);
            break;
          }
          case LIGHT_POINT:
          {
            break;
          }
        }
      }
    }

    private @Nonnull RVectorReadable3F<RSpaceRGB> getColour()
    {
      final RVectorM3F<RSpaceRGB> v = new RVectorM3F<RSpaceRGB>();
      v.x = Float.parseFloat(this.colour_r.getText());
      v.y = Float.parseFloat(this.colour_g.getText());
      v.z = Float.parseFloat(this.colour_b.getText());
      return v;
    }

    private float getIntensity()
    {
      return Float.parseFloat(this.intensity.getText());
    }

    protected @Nonnull KLight makeLight(
      final @Nonnull Integer id,
      final @Nonnull KLight.Type type,
      final @Nonnull RVectorReadable3F<RSpaceRGB> colour)
    {
      switch (type) {
        case LIGHT_CONE:
        {
          throw new UnimplementedCodeException();
        }
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
        case LIGHT_POINT:
        {
          throw new UnimplementedCodeException();
        }
      }

      throw new UnreachableCodeException();
    }

    protected void saveLight(
      final @Nonnull LightsTableModel data,
      final @CheckForNull KLight initial)
    {
      final Integer id =
        (initial == null) ? data.lightFreshID() : initial.getID();

      final KLight light =
        this.makeLight(
          id,
          (KLight.Type) this.type_select.getSelectedItem(),
          this.getColour());

      data.addLight(light);
    }

    private void setColour(
      final RVectorReadable3F<RSpaceRGB> color)
    {
      this.colour_r.setText(Float.toString(color.getXF()));
      this.colour_g.setText(Float.toString(color.getYF()));
      this.colour_b.setText(Float.toString(color.getZF()));
    }

    private void setIntensity(
      final float value)
    {
      this.intensity.setText(Float.toString(value));
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

    protected @Nonnull KLight getLightAt(
      final int row)
    {
      return this.model.getLightAt(row);
    }

    protected void removeLightAt(
      final int row)
    {
      this.model.removeLightAt(row);
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

    void addLight(
      final @Nonnull KLight light)
    {
      if (this.controller.lightExists(light.getID())) {
        this.log.debug("Replacing light "
          + light.getID()
          + " with type "
          + light.getType());

        for (final ArrayList<String> row : this.data) {
          final Integer id = Integer.valueOf(row.get(0));
          if (id.equals(light.getID())) {
            row.set(1, light.getType().getName());
          }
        }
      } else {
        this.log.debug("Adding light "
          + light.getID()
          + " of type "
          + light.getType());

        final ArrayList<String> row = new ArrayList<String>();
        row.add(light.getID().toString());
        row.add(light.getType().getName());
        this.data.add(row);
      }

      this.controller.lightAdd(light);
      this.fireTableDataChanged();
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
    {
      final Integer key = Integer.valueOf(row);
      assert this.controller.lightExists(key);
      return this.controller.lightGet(key);
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

    @Nonnull Integer lightFreshID()
    {
      return this.controller.lightFreshID();
    }

    void removeLightAt(
      final int row)
    {
      assert row != -1;
      assert row < this.data.size();

      final ArrayList<String> row_data = this.data.get(row);
      final Integer id = Integer.valueOf(row_data.get(0));

      this.log.debug("Removing light " + id);
      this.controller.lightRemove(id);
      this.data.remove(row);
      this.fireTableDataChanged();
    }
  }

  private static final long serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }

  public SBLightsPanel(
    final @Nonnull SBSceneControllerLights state,
    final @Nonnull Log log)
  {
    final LightsTableModel lights_model = new LightsTableModel(state, log);
    final LightsTable lights = new LightsTable(lights_model);
    final JScrollPane scroller = new JScrollPane(lights);

    final JButton add = new JButton("Add...");
    add.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final LightEditDialog dialog = new LightEditDialog(lights_model);
        dialog.pack();
        dialog.setVisible(true);
      }
    });

    final JButton edit = new JButton("Edit...");
    edit.setEnabled(false);
    edit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final int row = lights.getSelectedRow();
        assert row != -1;
        final KLight light = lights.getLightAt(row);
        assert light != null;

        final LightEditDialog dialog =
          new LightEditDialog(light, lights_model);
        dialog.pack();
        dialog.setVisible(true);
      }
    });

    final JButton remove = new JButton("Remove...");
    remove.setEnabled(false);
    remove.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final int row = lights.getSelectedRow();
        assert row != -1;
        lights.removeLightAt(row);
      }
    });

    lights.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        @Override public void valueChanged(
          final @Nonnull ListSelectionEvent e)
        {
          if (lights.getSelectedRow() == -1) {
            edit.setEnabled(false);
            remove.setEnabled(false);
          } else {
            edit.setEnabled(true);
            remove.setEnabled(true);
          }
        }
      });

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(scroller);
    dg.row().grid().add(add).add(edit).add(remove);
  }

}
