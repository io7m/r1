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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;
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

    public <C extends SBSceneControllerTextures & SBSceneControllerLights> LightEditDialog(
      final @Nonnull C controller,
      final @Nonnull LightsTableModel light_table_model,
      final @Nonnull Log log)
      throws IOException,
        ConstraintError
    {
      this.panel =
        new LightEditDialogPanel(
          this,
          controller,
          null,
          light_table_model,
          log);
      this.setTitle("Create light...");
      this.getContentPane().add(this.panel);
    }

    public <C extends SBSceneControllerTextures & SBSceneControllerLights> LightEditDialog(
      final @Nonnull C controller,
      final @Nonnull SBLight light,
      final @Nonnull LightsTableModel light_table_model,
      final @Nonnull Log log)
      throws IOException,
        ConstraintError
    {
      this.panel =
        new LightEditDialogPanel(
          this,
          controller,
          light,
          light_table_model,
          log);
      this.setTitle("Edit light...");
      this.getContentPane().add(this.panel);
    }
  }

  private static class LightEditDialogPanel extends JPanel
  {
    private static final long                      serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    private final @Nonnull JLabel                  error_text;
    private final @Nonnull JLabel                  error_icon;
    private final @Nonnull SBSceneControllerLights controller;
    protected final @Nonnull LightsTableModel      light_table_model;
    private final @Nonnull SBLightControls         controls;

    public <C extends SBSceneControllerTextures & SBSceneControllerLights> LightEditDialogPanel(
      final @Nonnull LightEditDialog window,
      final @Nonnull C controller,
      final @CheckForNull SBLight light,
      final @Nonnull LightsTableModel light_table_model,
      final @Nonnull Log log)
      throws IOException,
        ConstraintError
    {
      this.controller = controller;
      this.light_table_model = light_table_model;
      this.controls = SBLightControls.newControls(window, controller, log);

      this.error_text = new JLabel("Some informative error text");
      this.error_icon = SBIcons.makeErrorIcon();
      this.error_text.setVisible(false);
      this.error_icon.setVisible(false);

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
            LightEditDialogPanel.this.saveLight();
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
            LightEditDialogPanel.this.saveLight();
            SBWindowUtilities.closeWindow(window);
          } catch (final SBExceptionInputError x) {
            LightEditDialogPanel.this.setError(x.getMessage());
          } catch (final ConstraintError x) {
            LightEditDialogPanel.this.setError(x.getMessage());
          }
        }
      });

      final DesignGridLayout layout = new DesignGridLayout(this);

      this.controls.addToLayout(layout);
      layout.emptyRow();
      layout.row().grid().add(apply).add(cancel).add(finish);
      layout.emptyRow();
      layout.row().grid(this.error_icon).add(this.error_text);

      if (light != null) {
        this.controls.setDescription(light.getDescription());
      }
    }

    protected void saveLight()
      throws SBExceptionInputError,
        ConstraintError
    {
      final SBLightDescription ld = this.controls.getDescription();
      this.controller.sceneLightAddByDescription(ld);
      this.light_table_model.refreshLights();
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

  public <C extends SBSceneControllerTextures & SBSceneControllerLights> SBLightsPanel(
    final @Nonnull C controller,
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
            new LightEditDialog(
              controller,
              SBLightsPanel.this.lights_model,
              log);
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
        SBLight light;

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
              SBLightsPanel.this.lights_model,
              log);
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
          SBLight light;

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
