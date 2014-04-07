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

package com.io7m.renderer.kernel.sandbox;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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

public final class SBInstancesControls implements
  SBSceneChangeListener,
  SBControls
{
  private static class InstanceEditDialog extends JFrame
  {
    private static final long           serialVersionUID;

    static {
      serialVersionUID = -443107596466630590L;
    }

    private final @Nonnull SBErrorField error;

    public <C extends SBSceneControllerInstances & SBSceneControllerTextures & SBSceneControllerMeshes & SBSceneControllerMaterials> InstanceEditDialog(
      final @Nonnull Log log,
      final @Nonnull C controller,
      final @Nonnull Integer id,
      final @CheckForNull SBInstance instance)
      throws IOException
    {
      final Container content = this.getContentPane();
      final JPanel inner = new JPanel();
      final JScrollPane pane = new JScrollPane(inner);
      content.add(pane);

      final DesignGridLayout layout = new DesignGridLayout(inner);
      final SBInstanceControls controls =
        SBInstanceControls.newControls(this, log, controller, id);
      controls.controlsAddToLayout(layout);

      layout.row().left().add(new JSeparator()).fill();
      this.error = new SBErrorField();
      this.error.controlsAddToLayout(layout);

      if (instance != null) {
        controls.controlsLoadFrom(instance);
      }

      final JButton ok = new JButton("OK");
      ok.addActionListener(new ActionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          actionPerformed(
            final @Nonnull ActionEvent e)
        {
          try {
            final SBInstance i = controls.controlsSave();
            controller.sceneInstancePut(i);
            SBWindowUtilities.closeWindow(InstanceEditDialog.this);
          } catch (final Throwable x) {
            log.critical(x.getMessage());
            InstanceEditDialog.this.error.errorSet(x.getMessage());
          }
        }
      });

      final JButton apply = new JButton("Apply");
      apply.addActionListener(new ActionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          actionPerformed(
            final @Nonnull ActionEvent e)
        {
          try {
            final SBInstance i = controls.controlsSave();
            controller.sceneInstancePut(i);
          } catch (final Throwable x) {
            log.critical(x.getMessage());
            InstanceEditDialog.this.error.errorSet(x.getMessage());
          }
        }
      });

      final JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          SBWindowUtilities.closeWindow(InstanceEditDialog.this);
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

  private static class InstancesTable extends JTable
  {
    private static final long                  serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final @Nonnull InstancesTableModel model;

    public InstancesTable(
      final InstancesTableModel in_model)
    {
      super(in_model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.getColumnModel().getColumn(0).setPreferredWidth(16);
      this.getColumnModel().getColumn(1).setPreferredWidth(300);

      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = in_model;
    }
  }

  private static class InstancesTableModel extends AbstractTableModel
  {
    private static final long                           serialVersionUID;
    static {
      serialVersionUID = 6499860137289000995L;
    }
    private final @Nonnull String[]                     column_names;
    private final @Nonnull SBSceneControllerInstances   controller;
    private final @Nonnull ArrayList<ArrayList<String>> data;

    private final @Nonnull Log                          log;

    public InstancesTableModel(
      final @Nonnull SBSceneControllerInstances in_controller,
      final @Nonnull Log in_log)
    {
      this.log = new Log(in_log, "instance-table");
      this.column_names = new String[] { "ID", "Mesh" };
      this.data = new ArrayList<ArrayList<String>>();
      this.controller = in_controller;
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
      throws ConstraintError
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

    protected void refreshInstances()
    {
      this.data.clear();
      final Collection<SBInstance> objects =
        this.controller.sceneInstancesGetAll();

      for (final SBInstance m : objects) {
        final ArrayList<String> row = new ArrayList<String>();
        row.add(m.getID().toString());
        row.add(m.getMesh().toString());
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
        new SBExampleWindow("Instance") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final Log log =
              new Log(new Properties(), "com.io7m.renderer", "sandbox");
            final SBExampleController controller = new SBExampleController();
            final SBInstancesControls controls =
              new SBInstancesControls(controller, log);
            controls.controlsAddToLayout(layout);

            final JButton hide = new JButton("Hide");
            hide.addActionListener(new ActionListener() {
              @Override public void actionPerformed(
                final ActionEvent e)
              {
                controls.controlsHide();
              }
            });
            final JButton show = new JButton("Show");
            show.addActionListener(new ActionListener() {
              @Override public void actionPerformed(
                final ActionEvent e)
              {
                controls.controlsShow();
              }
            });
            layout.row().grid().add(hide).add(show);
          }
        };
      }
    });
  }

  private final @Nonnull JButton               add;
  private final @Nonnull JButton               edit;
  private final @Nonnull RowGroup              group;
  protected final @Nonnull InstancesTable      instances;
  protected final @Nonnull InstancesTableModel instances_model;
  private final @Nonnull JButton               remove;
  private final @Nonnull JScrollPane           scroller;

  public <C extends SBSceneControllerInstances & SBSceneControllerTextures & SBSceneControllerMeshes & SBSceneControllerMaterials> SBInstancesControls(
    final @Nonnull C controller,
    final @Nonnull Log log)
  {
    this.group = new RowGroup();
    this.instances_model = new InstancesTableModel(controller, log);
    this.instances = new InstancesTable(this.instances_model);
    this.scroller = new JScrollPane(this.instances);

    this.add = new JButton("Add...");
    this.add.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final Integer id = controller.sceneInstanceFreshID();
          final InstanceEditDialog dialog =
            new InstanceEditDialog(log, controller, id, null);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final Throwable x) {
          x.printStackTrace();
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
          final int view_row =
            SBInstancesControls.this.instances.getSelectedRow();
          assert view_row != -1;
          final int model_row =
            SBInstancesControls.this.instances
              .convertRowIndexToModel(view_row);
          final SBInstance i =
            SBInstancesControls.this.instances_model.getInstanceAt(model_row);
          assert i != null;

          final InstanceEditDialog dialog =
            new InstanceEditDialog(log, controller, i.getID(), i);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final Throwable x) {
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
        throw new UnimplementedCodeException();
      }
    });

    this.instances.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          valueChanged(
            final @Nonnull ListSelectionEvent e)
        {
          if (SBInstancesControls.this.instances.getSelectedRow() == -1) {
            SBInstancesControls.this.edit.setEnabled(false);
            SBInstancesControls.this.remove.setEnabled(false);
          } else {
            SBInstancesControls.this.edit.setEnabled(true);
            SBInstancesControls.this.remove.setEnabled(true);
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

  @Override public void sceneChanged()
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        SBInstancesControls.this.instances_model.refreshInstances();
      }
    });
  }

  @Override public String toString()
  {
    return "[SBInstancesPanel]";
  }
}
