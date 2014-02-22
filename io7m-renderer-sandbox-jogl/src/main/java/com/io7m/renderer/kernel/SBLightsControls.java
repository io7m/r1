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
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;

final class SBLightsControls implements SBSceneChangeListener, SBControls
{
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
            final SBLightsControls controls =
              new SBLightsControls(controller, log);
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

  private static class LightEditDialog extends JFrame
  {
    private static final long           serialVersionUID;

    static {
      serialVersionUID = -443107596466630590L;
    }

    private final @Nonnull SBErrorField error;

    public <C extends SBSceneControllerLights & SBSceneControllerTextures> LightEditDialog(
      final @Nonnull Log log,
      final @Nonnull C controller,
      final @Nonnull Integer id,
      final @CheckForNull SBLightDescription instance)
      throws ConstraintError,
        IOException
    {
      final Container content = this.getContentPane();
      final JPanel inner = new JPanel();
      final JScrollPane pane = new JScrollPane(inner);
      content.add(pane);

      final DesignGridLayout layout = new DesignGridLayout(inner);
      final SBLightControls controls =
        SBLightControls.newControls(this, controller, id, log);
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
            final SBLightDescription i = controls.controlsSave();
            controller.sceneLightPut(i);
            SBWindowUtilities.closeWindow(LightEditDialog.this);
          } catch (final Throwable x) {
            final String message = "Could not add light: " + x.getMessage();
            log.critical(message);
            LightEditDialog.this.error.errorSet(x.getMessage());
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
            final SBLightDescription i = controls.controlsSave();
            controller.sceneLightPut(i);
          } catch (final Throwable x) {
            final String message = "Could not add light: " + x.getMessage();
            log.critical(message);
            LightEditDialog.this.error.errorSet(x.getMessage());
          }
        }
      });

      final JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          LightEditDialog.this.dispose();
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

    static {
      serialVersionUID = 6499860137289000995L;
    }

    private final @Nonnull String[]                     column_names;
    private final @Nonnull SBSceneControllerLights      controller;
    private final @Nonnull ArrayList<ArrayList<String>> data;
    private final @Nonnull Log                          log;

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

    protected @Nonnull SBLight getLightAt(
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
      final Collection<SBLight> lights = this.controller.sceneLightsGetAll();

      for (final SBLight l : lights) {
        final ArrayList<String> row = new ArrayList<String>();
        row.add(l.getID().toString());
        row.add(SBLightType.fromLight(l).toString());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private final @Nonnull JButton            add;
  private final @Nonnull JButton            edit;
  private final @Nonnull RowGroup           group;
  protected final @Nonnull LightsTable      lights;
  protected final @Nonnull LightsTableModel lights_model;
  private final @Nonnull JButton            remove;
  protected final @Nonnull JScrollPane      scroller;

  public <C extends SBSceneControllerTextures & SBSceneControllerLights> SBLightsControls(
    final @Nonnull C controller,
    final @Nonnull Log log)
  {
    this.group = new RowGroup();
    this.lights_model = new LightsTableModel(controller, log);
    this.lights = new LightsTable(this.lights_model);
    this.scroller = new JScrollPane(this.lights);

    this.add = new JButton("Add...");
    this.add.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final Integer id = controller.sceneLightFreshID();
          final LightEditDialog dialog =
            new LightEditDialog(log, controller, id, null);
          dialog.pack();
          dialog.setVisible(true);
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        } catch (final IOException x) {
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
        final int view_row = SBLightsControls.this.lights.getSelectedRow();
        assert view_row != -1;
        final int model_row =
          SBLightsControls.this.lights.convertRowIndexToModel(view_row);
        SBLight light;

        try {
          light = SBLightsControls.this.lights_model.getLightAt(model_row);
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
        assert light != null;

        try {
          final LightEditDialog dialog =
            new LightEditDialog(log, controller, light.getID(), light
              .getDescription());
          dialog.pack();
          dialog.setVisible(true);
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        } catch (final IOException x) {
          x.printStackTrace();
        }
      }
    });

    this.remove = new JButton("Remove...");
    this.remove.setEnabled(false);
    this.remove.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final int view_row = SBLightsControls.this.lights.getSelectedRow();
          assert view_row != -1;
          final int model_row =
            SBLightsControls.this.lights.convertRowIndexToModel(view_row);
          SBLight light;

          light = SBLightsControls.this.lights_model.getLightAt(model_row);
          assert light != null;
          controller.sceneLightRemove(light.getID());

          SBLightsControls.this.lights_model.refreshLights();
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
      }
    });

    this.lights.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          valueChanged(
            final @Nonnull ListSelectionEvent e)
        {
          if (SBLightsControls.this.lights.getSelectedRow() == -1) {
            SBLightsControls.this.edit.setEnabled(false);
            SBLightsControls.this.remove.setEnabled(false);
          } else {
            SBLightsControls.this.edit.setEnabled(true);
            SBLightsControls.this.remove.setEnabled(true);
          }
        }
      });

    controller.sceneChangeListenerAdd(this);
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
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
        SBLightsControls.this.lights_model.refreshLights();
      }
    });
  }

  @Override public String toString()
  {
    return "[SBLightsPanel]";
  }
}
