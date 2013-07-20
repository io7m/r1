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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.Log;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorM3F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

final class SBObjectsPanel extends JPanel
{
  private static class ObjectEditDialog extends JFrame
  {
    private static final long                    serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    private final @Nonnull ObjectEditDialogPanel panel;

    public <C extends SBSceneControllerMeshes & SBSceneControllerObjects & SBSceneControllerTextures> ObjectEditDialog(
      final @Nonnull C controller,
      final @Nonnull ObjectsTableModel data,
      final @Nonnull Log log)
      throws IOException
    {
      this.panel =
        new ObjectEditDialogPanel(this, controller, null, data, log);
      this.setTitle("Create object...");
      this.getContentPane().add(this.panel);
    }

    public <C extends SBSceneControllerMeshes & SBSceneControllerObjects & SBSceneControllerTextures> ObjectEditDialog(
      final @Nonnull C controller,
      final @Nonnull SBObjectDescription object,
      final @Nonnull ObjectsTableModel data,
      final @Nonnull Log log)
      throws IOException
    {
      this.panel =
        new ObjectEditDialogPanel(this, controller, object, data, log);
      this.setTitle("Edit object...");
      this.getContentPane().add(this.panel);
    }
  }

  private static class ObjectEditDialogPanel extends JPanel
  {
    private static final long                serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    protected final @Nonnull JTextField      diffuse_texture;
    private final @Nonnull JButton           diffuse_texture_select;
    protected final @Nonnull JTextField      normal_texture;
    private final @Nonnull JButton           normal_texture_select;
    protected final @Nonnull JTextField      specular_texture;
    private final @Nonnull JButton           specular_texture_select;

    protected final JTextField               model;
    protected final JTextField               model_object;
    private final JButton                    model_select;

    protected final @Nonnull JTextField      position_x;
    protected final @Nonnull JTextField      position_y;
    protected final @Nonnull JTextField      position_z;

    protected final @Nonnull JTextField      orientation_x;
    protected final @Nonnull JTextField      orientation_y;
    protected final @Nonnull JTextField      orientation_z;

    protected final @Nonnull JLabel          error_icon;
    protected final @Nonnull JLabel          error_text;

    private final @Nonnull ObjectsTableModel objects_table_model;
    private final @Nonnull Border            default_field_border;

    public <C extends SBSceneControllerMeshes & SBSceneControllerObjects & SBSceneControllerTextures> ObjectEditDialogPanel(
      final @Nonnull ObjectEditDialog window,
      final @Nonnull C controller,
      final @CheckForNull SBObjectDescription object,
      final @Nonnull ObjectsTableModel objects_table_model,
      final @Nonnull Log log)
      throws IOException
    {
      this.objects_table_model = objects_table_model;

      final DesignGridLayout dg = new DesignGridLayout(this);

      if (object != null) {
        final JTextField id_field = new JTextField(object.getID().toString());
        id_field.setEditable(false);
        dg.row().grid().add(new JLabel("ID")).add(id_field, 3);
      }

      this.error_text = new JLabel("Some informative error text");
      this.error_icon = SBIcons.makeErrorIcon();
      this.error_text.setVisible(false);
      this.error_icon.setVisible(false);

      this.position_x = new JTextField("0.0");
      this.position_y = new JTextField("0.0");
      this.position_z = new JTextField("0.0");

      this.orientation_x = new JTextField("0.0");
      this.orientation_y = new JTextField("0.0");
      this.orientation_z = new JTextField("0.0");

      this.default_field_border = this.position_x.getBorder();

      this.diffuse_texture = new JTextField();
      this.diffuse_texture.setEditable(false);
      this.diffuse_texture_select = new JButton("Select...");
      this.diffuse_texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTexturesWindow twindow =
            new SBTexturesWindow(
              controller,
              ObjectEditDialogPanel.this.diffuse_texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });

      this.normal_texture = new JTextField();
      this.normal_texture.setEditable(false);
      this.normal_texture_select = new JButton("Select...");
      this.normal_texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTexturesWindow twindow =
            new SBTexturesWindow(
              controller,
              ObjectEditDialogPanel.this.normal_texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });

      this.specular_texture = new JTextField();
      this.specular_texture.setEditable(false);
      this.specular_texture_select = new JButton("Select...");
      this.specular_texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTexturesWindow twindow =
            new SBTexturesWindow(
              controller,
              ObjectEditDialogPanel.this.specular_texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });

      this.model = new JTextField();
      this.model.setEditable(false);
      this.model_object = new JTextField();
      this.model_object.setEditable(false);
      this.model_select = new JButton("Select...");
      this.model_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBMeshesWindow mwindow =
            new SBMeshesWindow(
              controller,
              ObjectEditDialogPanel.this.model,
              ObjectEditDialogPanel.this.model_object,
              log);
          mwindow.pack();
          mwindow.setVisible(true);
        }
      });

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
            ObjectEditDialogPanel.this.saveObject(controller, object);
          } catch (final SBExceptionInputError x) {
            ObjectEditDialogPanel.this.setError(x.getMessage());
          }
        }
      });

      final JButton finish = new JButton("Finish");
      finish.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            ObjectEditDialogPanel.this.saveObject(controller, object);
            SBWindowUtilities.closeWindow(window);
          } catch (final SBExceptionInputError x) {
            ObjectEditDialogPanel.this.setError(x.getMessage());
          }
        }
      });

      dg
        .row()
        .grid()
        .add(new JLabel("Diffuse texture"))
        .add(this.diffuse_texture, 2)
        .add(this.diffuse_texture_select);

      dg
        .row()
        .grid()
        .add(new JLabel("Normal map"))
        .add(this.normal_texture, 2)
        .add(this.normal_texture_select);

      dg
        .row()
        .grid()
        .add(new JLabel("Specular map"))
        .add(this.specular_texture, 2)
        .add(this.specular_texture_select);

      dg
        .row()
        .grid()
        .add(new JLabel("Model"))
        .add(this.model, 2)
        .add(this.model_select);

      dg
        .row()
        .grid()
        .add(new JLabel("Object"))
        .add(this.model_object, 2)
        .spanRow();

      dg.emptyRow();

      dg
        .row()
        .grid()
        .add(new JLabel("Position"))
        .add(this.position_x)
        .add(this.position_y)
        .add(this.position_z);

      dg
        .row()
        .grid()
        .add(new JLabel("Orientation"))
        .add(this.orientation_x)
        .add(this.orientation_y)
        .add(this.orientation_z);

      dg.emptyRow();
      dg.row().grid().add(apply).add(cancel).add(finish);
      dg.emptyRow();
      dg.row().left().add(this.error_icon).add(this.error_text);

      if (object != null) {
        this.loadObject(object);
      }
    }

    private void loadObject(
      final @Nonnull SBObjectDescription object)
    {
      final RVectorReadable3F<RSpaceWorld> pos = object.getPosition();
      final RVectorReadable3F<SBDegrees> ori = object.getOrientation();

      this.position_x.setText(Float.toString(pos.getXF()));
      this.position_y.setText(Float.toString(pos.getYF()));
      this.position_z.setText(Float.toString(pos.getZF()));

      this.orientation_x.setText(Float.toString(ori.getXF()));
      this.orientation_y.setText(Float.toString(ori.getYF()));
      this.orientation_z.setText(Float.toString(ori.getZF()));

      this.diffuse_texture.setText(object.getDiffuseTexture() == null
        ? ""
        : object.getDiffuseTexture().toString());
      this.normal_texture.setText(object.getNormalTexture() == null
        ? ""
        : object.getNormalTexture().toString());
      this.specular_texture.setText(object.getSpecularTexture() == null
        ? ""
        : object.getSpecularTexture().toString());

      this.model.setText(object.getModel().toString());
      this.model_object.setText(object.getModelObject());
    }

    protected void saveObject(
      final @Nonnull SBSceneControllerObjects controller,
      final @CheckForNull SBObjectDescription initial)
      throws SBExceptionInputError
    {
      final Integer id =
        (initial == null) ? controller.objectFreshID() : initial.getID();

      final RVectorM3F<RSpaceWorld> position = new RVectorM3F<RSpaceWorld>();
      final RVectorM3F<SBDegrees> orientation = new RVectorM3F<SBDegrees>();

      position.x = SBTextFieldUtilities.getFieldFloatOrError(this.position_x);
      position.y = SBTextFieldUtilities.getFieldFloatOrError(this.position_y);
      position.z = SBTextFieldUtilities.getFieldFloatOrError(this.position_z);

      orientation.x =
        SBTextFieldUtilities.getFieldFloatOrError(this.orientation_x);
      orientation.y =
        SBTextFieldUtilities.getFieldFloatOrError(this.orientation_y);
      orientation.z =
        SBTextFieldUtilities.getFieldFloatOrError(this.orientation_z);

      final File diffuse =
        (this.diffuse_texture.getText().equals("")) ? null : new File(
          this.diffuse_texture.getText());
      final File normal =
        (this.normal_texture.getText().equals("")) ? null : new File(
          this.normal_texture.getText());
      final File specular =
        (this.specular_texture.getText().equals("")) ? null : new File(
          this.specular_texture.getText());

      final String model_name =
        SBTextFieldUtilities.getFieldNonEmptyStringOrError(this.model);
      final String model_object_name =
        SBTextFieldUtilities.getFieldNonEmptyStringOrError(this.model_object);

      final SBObjectDescription o =
        new SBObjectDescription(
          id,
          new File(model_name),
          model_object_name,
          position,
          orientation,
          diffuse,
          normal,
          specular);

      controller.objectAdd(o);

      this.objects_table_model.refreshObjects();
      this.unsetError();
    }

    protected void setError(
      final @Nonnull String message)
    {
      this.error_icon.setVisible(true);
      this.error_text.setText(message);
      this.error_text.setVisible(true);
    }

    protected void unsetError()
    {
      this.error_icon.setVisible(false);
      this.error_text.setVisible(false);
    }
  }

  private static class ObjectsTable extends JTable
  {
    private static final long                serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final @Nonnull ObjectsTableModel model;

    public ObjectsTable(
      final ObjectsTableModel model)
    {
      super(model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.getColumnModel().getColumn(0).setPreferredWidth(16);
      this.getColumnModel().getColumn(1).setPreferredWidth(32);
      this.getColumnModel().getColumn(2).setPreferredWidth(300);

      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = model;
    }
  }

  private static class ObjectsTableModel extends AbstractTableModel
  {
    private static final long                           serialVersionUID;
    private final @Nonnull String[]                     column_names;
    private final @Nonnull ArrayList<ArrayList<String>> data;
    private final @Nonnull Log                          log;
    private final @Nonnull SBSceneControllerObjects     controller;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public ObjectsTableModel(
      final @Nonnull SBSceneControllerObjects controller,
      final @Nonnull Log log)
    {
      this.log = new Log(log, "object-table");
      this.column_names = new String[] { "ID", "File", "Mesh" };
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

    protected @Nonnull SBObjectDescription getObjectAt(
      final int row)
    {
      final ArrayList<String> row_data = this.data.get(row);
      assert row_data != null;
      final String id_text = row_data.get(0);
      final Integer id = Integer.valueOf(id_text);
      assert this.controller.objectExists(id);
      return this.controller.objectGet(id);
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

    protected void refreshObjects()
    {
      this.data.clear();
      final List<SBObjectDescription> objects =
        this.controller.objectsGetAll();
      for (final SBObjectDescription o : objects) {
        final ArrayList<String> row = new ArrayList<String>();
        row.add(o.getID().toString());
        row.add(o.getModel().getName());
        row.add(o.getModelObject());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static final long serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }

  public <C extends SBSceneControllerMeshes & SBSceneControllerObjects & SBSceneControllerTextures> SBObjectsPanel(
    final @Nonnull C controller,
    final @Nonnull Log log)
  {
    final ObjectsTableModel objects_model =
      new ObjectsTableModel(controller, log);
    final ObjectsTable objects = new ObjectsTable(objects_model);
    final JScrollPane scroller = new JScrollPane(objects);

    final JButton add = new JButton("Add...");
    add.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final ObjectEditDialog dialog =
            new ObjectEditDialog(controller, objects_model, log);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final IOException x) {
          log.critical("Unable to open edit dialog: " + x.getMessage());
        }
      }
    });

    final JButton edit = new JButton("Edit...");
    edit.setEnabled(false);
    edit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final int view_row = objects.getSelectedRow();
          assert view_row != -1;
          final int model_row = objects.convertRowIndexToModel(view_row);
          final SBObjectDescription object =
            objects_model.getObjectAt(model_row);
          assert object != null;

          final ObjectEditDialog dialog =
            new ObjectEditDialog(controller, object, objects_model, log);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final IOException x) {
          log.critical("Unable to open edit dialog: " + x.getMessage());
        }
      }
    });

    final JButton remove = new JButton("Remove...");
    remove.setEnabled(false);
    remove.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final int view_row = objects.getSelectedRow();
        assert view_row != -1;
        final int model_row = objects.convertRowIndexToModel(view_row);
        final SBObjectDescription object =
          objects_model.getObjectAt(model_row);
        assert object != null;

        controller.objectRemove(object.getID());
        objects_model.refreshObjects();
      }
    });

    objects.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        @Override public void valueChanged(
          final @Nonnull ListSelectionEvent e)
        {
          if (objects.getSelectedRow() == -1) {
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
