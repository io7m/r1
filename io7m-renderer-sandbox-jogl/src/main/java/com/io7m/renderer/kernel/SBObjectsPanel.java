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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.Log;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

final class SBObjectsPanel extends JPanel implements SBSceneChangeListener
{
  private static class ObjectEditDialog extends JFrame
  {
    private static final long                    serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    private final @Nonnull ObjectEditDialogPanel panel;

    public <C extends SBSceneControllerMeshes & SBSceneControllerInstances & SBSceneControllerTextures> ObjectEditDialog(
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

    public <C extends SBSceneControllerMeshes & SBSceneControllerInstances & SBSceneControllerTextures> ObjectEditDialog(
      final @Nonnull C controller,
      final @Nonnull SBInstanceDescription initial_desc,
      final @Nonnull ObjectsTableModel data,
      final @Nonnull Log log)
      throws IOException
    {
      this.panel =
        new ObjectEditDialogPanel(this, controller, initial_desc, data, log);
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

    protected @Nonnull Map<String, SBMesh>   meshes;
    protected final JComboBox<String>        mesh_selector;
    private final JButton                    mesh_load;

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

    protected void meshesRefresh()
    {
      this.mesh_selector.removeAllItems();
      for (final String name : this.meshes.keySet()) {
        this.mesh_selector.addItem(name);
      }
    }

    public <C extends SBSceneControllerMeshes & SBSceneControllerInstances & SBSceneControllerTextures> ObjectEditDialogPanel(
      final @Nonnull ObjectEditDialog window,
      final @Nonnull C controller,
      final @CheckForNull SBInstanceDescription initial_desc,
      final @Nonnull ObjectsTableModel objects_table_model,
      final @Nonnull Log log)
      throws IOException
    {
      this.objects_table_model = objects_table_model;

      final DesignGridLayout dg = new DesignGridLayout(this);

      if (initial_desc != null) {
        final JTextField id_field =
          new JTextField(initial_desc.getID().toString());
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

      this.meshes = new HashMap<String, SBMesh>();
      this.mesh_selector = new JComboBox<String>();

      this.mesh_load = new JButton("Open...");
      this.mesh_load.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final JFileChooser chooser = new JFileChooser();
          chooser.setMultiSelectionEnabled(false);

          final int r = chooser.showOpenDialog(ObjectEditDialogPanel.this);
          switch (r) {
            case JFileChooser.APPROVE_OPTION:
            {
              final File file = chooser.getSelectedFile();

              final SwingWorker<SBMesh, Void> worker =
                new SwingWorker<SBMesh, Void>() {
                  @Override protected SBMesh doInBackground()
                    throws Exception
                  {
                    return controller.sceneMeshLoad(file).get();
                  }

                  @Override protected void done()
                  {
                    try {
                      this.get();

                      ObjectEditDialogPanel.this.meshes =
                        controller.sceneMeshesGet();
                      ObjectEditDialogPanel.this.meshesRefresh();

                    } catch (final InterruptedException x) {
                      SBErrorBox.showError(log, "Interrupted operation", x);
                    } catch (final ExecutionException x) {
                      SBErrorBox.showError(
                        log,
                        "Mesh loading error",
                        x.getCause());
                    }
                  }
                };

              worker.execute();
              break;
            }
            case JFileChooser.CANCEL_OPTION:
            case JFileChooser.ERROR_OPTION:
            {
              break;
            }
          }
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
            ObjectEditDialogPanel.this.saveObject(controller, initial_desc);
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
            ObjectEditDialogPanel.this.saveObject(controller, initial_desc);
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
        .add(new JLabel("Mesh"))
        .add(this.mesh_selector, 2)
        .add(this.mesh_load);

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

      if (initial_desc != null) {
        this.loadObject(initial_desc);
      }
    }

    private void loadObject(
      final @Nonnull SBInstanceDescription initial_desc)
    {
      final RVectorReadable3F<RSpaceWorld> pos = initial_desc.getPosition();
      final RVectorReadable3F<SBDegrees> ori = initial_desc.getOrientation();

      this.position_x.setText(Float.toString(pos.getXF()));
      this.position_y.setText(Float.toString(pos.getYF()));
      this.position_z.setText(Float.toString(pos.getZF()));

      this.orientation_x.setText(Float.toString(ori.getXF()));
      this.orientation_y.setText(Float.toString(ori.getYF()));
      this.orientation_z.setText(Float.toString(ori.getZF()));

      this.diffuse_texture.setText(initial_desc.getDiffuse() == null
        ? ""
        : initial_desc.getDiffuse().toString());
      this.normal_texture.setText(initial_desc.getNormal() == null
        ? ""
        : initial_desc.getNormal().toString());
      this.specular_texture.setText(initial_desc.getSpecular() == null
        ? ""
        : initial_desc.getSpecular().toString());
    }

    protected void saveObject(
      final @Nonnull SBSceneControllerInstances controller,
      final @CheckForNull SBInstanceDescription initial)
      throws SBExceptionInputError
    {
      final Integer id =
        (initial == null) ? controller.sceneInstanceFreshID() : initial
          .getID();

      final RVectorI3F<RSpaceWorld> position =
        new RVectorI3F<RSpaceWorld>(
          SBTextFieldUtilities.getFieldFloatOrError(this.position_x),
          SBTextFieldUtilities.getFieldFloatOrError(this.position_y),
          SBTextFieldUtilities.getFieldFloatOrError(this.position_z));

      final RVectorI3F<SBDegrees> orientation =
        new RVectorI3F<SBDegrees>(
          SBTextFieldUtilities.getFieldFloatOrError(this.orientation_x),
          SBTextFieldUtilities.getFieldFloatOrError(this.orientation_y),
          SBTextFieldUtilities.getFieldFloatOrError(this.orientation_z));

      final String diffuse =
        (this.diffuse_texture.getText().equals(""))
          ? null
          : this.diffuse_texture.getText();
      final String normal =
        (this.normal_texture.getText().equals(""))
          ? null
          : this.normal_texture.getText();
      final String specular =
        (this.specular_texture.getText().equals(""))
          ? null
          : this.specular_texture.getText();

      final String mesh_name = (String) this.mesh_selector.getSelectedItem();

      final SBInstanceDescription d =
        new SBInstanceDescription(
          id,
          position,
          orientation,
          mesh_name,
          diffuse,
          normal,
          specular);

      controller.sceneInstanceAddByDescription(d);

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
      this.getColumnModel().getColumn(1).setPreferredWidth(300);

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
    private final @Nonnull SBSceneControllerInstances   controller;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public ObjectsTableModel(
      final @Nonnull SBSceneControllerInstances controller,
      final @Nonnull Log log)
    {
      this.log = new Log(log, "object-table");
      this.column_names = new String[] { "ID", "Mesh" };
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

    protected @Nonnull SBInstance getInstanceAt(
      final int row)
    {
      final ArrayList<String> row_data = this.data.get(row);
      assert row_data != null;
      final String id_text = row_data.get(0);
      final Integer id = Integer.valueOf(id_text);
      assert this.controller.sceneInstanceExists(id);
      return this.controller.sceneInstanceGet(id);
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
      final Collection<SBInstance> objects =
        this.controller.sceneInstancesGetAll();
      for (final SBInstance o : objects) {
        final ArrayList<String> row = new ArrayList<String>();
        row.add(o.getID().toString());
        row.add(o.getMesh());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static final long                  serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }

  protected final @Nonnull ObjectsTableModel objects_model;
  protected final @Nonnull ObjectsTable      objects;
  private final @Nonnull JScrollPane         scroller;

  public <C extends SBSceneControllerMeshes & SBSceneControllerInstances & SBSceneControllerTextures> SBObjectsPanel(
    final @Nonnull C controller,
    final @Nonnull Log log)
  {
    this.objects_model = new ObjectsTableModel(controller, log);
    this.objects = new ObjectsTable(this.objects_model);
    this.scroller = new JScrollPane(this.objects);

    final JButton add = new JButton("Add...");
    add.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final ObjectEditDialog dialog =
            new ObjectEditDialog(
              controller,
              SBObjectsPanel.this.objects_model,
              log);
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
          final int view_row = SBObjectsPanel.this.objects.getSelectedRow();
          assert view_row != -1;
          final int model_row =
            SBObjectsPanel.this.objects.convertRowIndexToModel(view_row);
          final SBInstance object =
            SBObjectsPanel.this.objects_model.getInstanceAt(model_row);
          assert object != null;

          final ObjectEditDialog dialog =
            new ObjectEditDialog(
              controller,
              object.getDescription(),
              SBObjectsPanel.this.objects_model,
              log);
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
        final int view_row = SBObjectsPanel.this.objects.getSelectedRow();
        assert view_row != -1;
        final int model_row =
          SBObjectsPanel.this.objects.convertRowIndexToModel(view_row);
        final SBInstance object =
          SBObjectsPanel.this.objects_model.getInstanceAt(model_row);
        assert object != null;

        controller.sceneInstanceRemove(object.getID());
        SBObjectsPanel.this.objects_model.refreshObjects();
      }
    });

    this.objects.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        @Override public void valueChanged(
          final @Nonnull ListSelectionEvent e)
        {
          if (SBObjectsPanel.this.objects.getSelectedRow() == -1) {
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

    controller.changeListenerAdd(this);
  }

  @Override public void sceneChanged()
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        SBObjectsPanel.this.objects_model.refreshObjects();
      }
    });
  }

  @Override public String toString()
  {
    return "[SBObjectsPanel]";
  }

}
