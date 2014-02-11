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

package com.io7m.renderer.kernel;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;
import net.java.dev.designgridlayout.Tag;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jlog.Log;

public final class SBMaterialsControls implements
  SBSceneChangeListener,
  SBControls
{
  private static class MaterialEditDialog extends JFrame
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -443107596466630590L;
    }

    public <C extends SBSceneControllerMaterials & SBSceneControllerTextures> MaterialEditDialog(
      final @Nonnull Log log,
      final @Nonnull C controller,
      final @Nonnull Integer id,
      final @Nonnull SBMaterialDescription material)
      throws ConstraintError
    {
      final Container content = this.getContentPane();
      final JPanel inner = new JPanel();
      final JScrollPane pane = new JScrollPane(inner);
      content.add(pane);

      final DesignGridLayout layout = new DesignGridLayout(inner);
      final SBMaterialControls controls =
        SBMaterialControls.newControls(this, log, controller, id);
      controls.controlsAddToLayout(layout);
      controls.controlsLoadFrom(material);

      final JButton ok = new JButton("OK");
      ok.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            final SBMaterialDescription desc = controls.controlsSave();
            controller.sceneMaterialPutByDescription(id, desc);
            SBWindowUtilities.closeWindow(MaterialEditDialog.this);
          } catch (final Throwable x) {
            final String message =
              "Could not add material: " + x.getMessage();
            log.critical(message);
          }
        }
      });

      final JButton apply = new JButton("Apply");
      apply.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            final SBMaterialDescription desc = controls.controlsSave();
            controller.sceneMaterialPutByDescription(id, desc);
          } catch (final Throwable x) {
            final String message =
              "Could not add material: " + x.getMessage();
            log.critical(message);
          }
        }
      });

      final JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          SBWindowUtilities.closeWindow(MaterialEditDialog.this);
        }
      });

      layout
        .row()
        .bar()
        .add(cancel, Tag.CANCEL)
        .add(ok, Tag.OK)
        .add(apply, Tag.APPLY);
    }
  }

  private static class MaterialsTable extends JTable
  {
    private static final long                  serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final @Nonnull MaterialsTableModel model;

    public MaterialsTable(
      final MaterialsTableModel model)
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

  private static class MaterialsTableModel extends AbstractTableModel
  {
    private static final long                           serialVersionUID;
    static {
      serialVersionUID = 6499860137289000995L;
    }
    private final @Nonnull String[]                     column_names;
    private final @Nonnull SBSceneControllerMaterials   controller;
    private final @Nonnull ArrayList<ArrayList<String>> data;

    private final @Nonnull Log                          log;

    public MaterialsTableModel(
      final @Nonnull SBSceneControllerMaterials controller,
      final @Nonnull Log log)
    {
      this.log = new Log(log, "material-table");
      this.column_names = new String[] { "ID", "Name" };
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

    protected @Nonnull SBMaterial getMaterialAt(
      final int row)
      throws ConstraintError
    {
      final ArrayList<String> row_data = this.data.get(row);
      assert row_data != null;
      final String id_text = row_data.get(0);
      final Integer id = Integer.valueOf(id_text);
      assert this.controller.sceneMaterialExists(id);
      return this.controller.sceneMaterialGet(id);
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

    protected void refreshMaterials()
    {
      this.data.clear();
      final Collection<SBMaterial> objects =
        this.controller.sceneMaterialsGetAll();

      for (final SBMaterial m : objects) {
        final ArrayList<String> row = new ArrayList<String>();
        row.add(m.materialGetID().toString());
        row.add(m.materialGetName());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Material") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final Log log =
              new Log(new Properties(), "com.io7m.renderer", "sandbox");
            final SBExampleController controller = new SBExampleController();
            final SBMaterialsControls controls =
              new SBMaterialsControls(controller, log);
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  private final @Nonnull JButton               add;
  private final @Nonnull JButton               edit;
  private final @Nonnull RowGroup              group;
  protected final @Nonnull MaterialsTable      materials;
  protected final @Nonnull MaterialsTableModel materials_model;
  private final @Nonnull JButton               remove;
  private final @Nonnull JScrollPane           scroller;

  public <C extends SBSceneControllerMaterials & SBSceneControllerTextures> SBMaterialsControls(
    final @Nonnull C controller,
    final @Nonnull Log log)
  {
    this.group = new RowGroup();
    this.materials_model = new MaterialsTableModel(controller, log);
    this.materials = new MaterialsTable(this.materials_model);
    this.scroller = new JScrollPane(this.materials);

    this.add = new JButton("Add...");
    this.add.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final Integer id = controller.sceneMaterialFreshID();
          final SBMaterialDescriptionOpaqueRegular base =
            SBMaterialDescriptionOpaqueRegular.getDefault();
          final MaterialEditDialog dialog =
            new MaterialEditDialog(log, controller, id, base);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final ConstraintError x) {
          log.critical("Unable to open edit dialog: " + x.getMessage());
        }
      }
    });

    this.edit = new JButton("Edit...");
    this.edit.setEnabled(false);
    this.edit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final SBMaterial material =
            SBMaterialsControls.this.getSelectedMaterial();
          assert material != null;

          final Integer id = material.materialGetID();
          final MaterialEditDialog dialog =
            new MaterialEditDialog(log, controller, id, material
              .materialGetDescription());
          dialog.pack();
          dialog.setVisible(true);
        } catch (final ConstraintError x) {
          log.critical("Unable to open edit dialog: " + x.getMessage());
        }
      }
    });

    this.remove = new JButton("Remove...");
    this.remove.setEnabled(false);
    this.remove.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        // TODO
        throw new UnimplementedCodeException();
      }
    });

    this.materials.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          valueChanged(
            final @Nonnull ListSelectionEvent e)
        {
          if (SBMaterialsControls.this.materials.getSelectedRow() == -1) {
            SBMaterialsControls.this.edit.setEnabled(false);
            SBMaterialsControls.this.remove.setEnabled(false);
          } else {
            SBMaterialsControls.this.edit.setEnabled(true);
            SBMaterialsControls.this.remove.setEnabled(true);
          }
        }
      });

    controller.sceneChangeListenerAdd(this);
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    layout.row().group(this.group).grid().add(this.scroller);
    layout
      .row()
      .group(this.group)
      .grid()
      .add(this.add)
      .add(this.edit)
      .add(this.remove);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }

  public @Nonnull JTable getMaterialTable()
  {
    return this.materials;
  }

  public @Nonnull SBMaterial getSelectedMaterial()
    throws ConstraintError
  {
    final int view_row = SBMaterialsControls.this.materials.getSelectedRow();
    assert view_row != -1;
    final int model_row =
      SBMaterialsControls.this.materials.convertRowIndexToModel(view_row);
    final SBMaterial material =
      SBMaterialsControls.this.materials_model.getMaterialAt(model_row);
    return material;
  }

  @Override public void sceneChanged()
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        SBMaterialsControls.this.materials_model.refreshMaterials();
      }
    });
  }

  @Override public String toString()
  {
    return "[SBMaterialsPanel]";
  }
}
