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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ProgramAttribute;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.ProgramUniform;
import com.io7m.jlog.Log;
import com.io7m.parasol.PGLSLMetaXML;
import com.io7m.parasol.PGLSLMetaXML.Output;

final class SBShadersPanel extends JPanel
{
  private static class InputsTable extends JTable
  {
    private static final long               serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final @Nonnull InputsTableModel model;

    public InputsTable(
      final InputsTableModel model)
    {
      super(model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = model;
    }
  }

  private static class InputsTableModel extends AbstractTableModel
  {
    private static final long                           serialVersionUID;
    private final @Nonnull String[]                     column_names;
    private final @Nonnull ArrayList<ArrayList<String>> data;
    private final @Nonnull Log                          log;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public InputsTableModel(
      final @Nonnull Log log)
    {
      this.log = new Log(log, "shader-uniform-table");
      this.column_names = new String[] { "Name", "Type" };
      this.data = new ArrayList<ArrayList<String>>();
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

    void showShader(
      final @Nonnull SBShader shader)
    {
      this.data.clear();

      final ProgramReference p = shader.getProgram();
      for (final Entry<String, ProgramAttribute> e : p
        .getAttributes()
        .entrySet()) {
        final String n = e.getKey();
        final ProgramAttribute a = e.getValue();
        final ArrayList<String> row = new ArrayList<String>();
        row.add(n);
        row.add(Integer.toString(a.getLocation()));
        row.add(a.getType().getName());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static class OutputsTable extends JTable
  {
    private static final long                serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final @Nonnull OutputsTableModel model;

    public OutputsTable(
      final OutputsTableModel model)
    {
      super(model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = model;
    }
  }

  private static class OutputsTableModel extends AbstractTableModel
  {
    private static final long                           serialVersionUID;
    private final @Nonnull String[]                     column_names;
    private final @Nonnull ArrayList<ArrayList<String>> data;
    private final @Nonnull Log                          log;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public OutputsTableModel(
      final @Nonnull Log log)
    {
      this.log = new Log(log, "shader-uniform-table");
      this.column_names = new String[] { "Name", "Index", "Type" };
      this.data = new ArrayList<ArrayList<String>>();
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

    @SuppressWarnings("boxing") void showShader(
      final @Nonnull SBShader shader)
    {
      this.data.clear();

      final PGLSLMetaXML m = shader.getMeta();
      for (final Entry<Integer, Output> e : m.getOutputs().entrySet()) {
        final Output o = e.getValue();
        final ArrayList<String> row = new ArrayList<String>();
        row.add(o.getName());
        row.add(Integer.toString(o.getIndex()));
        row.add(o.getType());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static class UniformsTable extends JTable
  {
    private static final long                 serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final @Nonnull UniformsTableModel model;

    public UniformsTable(
      final UniformsTableModel model)
    {
      super(model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = model;
    }
  }

  private static class UniformsTableModel extends AbstractTableModel
  {
    private static final long                           serialVersionUID;
    private final @Nonnull String[]                     column_names;
    private final @Nonnull ArrayList<ArrayList<String>> data;
    private final @Nonnull Log                          log;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public UniformsTableModel(
      final @Nonnull Log log)
    {
      this.log = new Log(log, "shader-uniform-table");
      this.column_names = new String[] { "Name", "Location", "Type" };
      this.data = new ArrayList<ArrayList<String>>();
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

    void showShader(
      final @Nonnull SBShader shader)
    {
      this.data.clear();

      final ProgramReference p = shader.getProgram();
      for (final Entry<String, ProgramUniform> e : p.getUniforms().entrySet()) {
        final String n = e.getKey();
        final ProgramUniform u = e.getValue();
        final ArrayList<String> row = new ArrayList<String>();
        row.add(n);
        row.add(Integer.toString(u.getLocation()));
        row.add(u.getType().getName());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static final long                  serialVersionUID;

  protected static final @Nonnull FileFilter META_XML_FILTER;
  static {
    serialVersionUID = -941448169051827275L;

    META_XML_FILTER = new FileFilter() {
      @Override public boolean accept(
        final File f)
      {
        return (f.isDirectory() || f.getName().equals("meta.xml"));
      }

      @Override public String getDescription()
      {
        return "All shaders (\"meta.xml\")";
      }
    };
  }
  private final @Nonnull JComboBox<String>   selector;

  private final @Nonnull JButton             open;
  private final @Nonnull Log                 jlog;
  private @Nonnull Map<String, SBShader>     shaders;

  private final @Nonnull UniformsTableModel  uniforms_model;
  private final @Nonnull UniformsTable       uniforms_table;
  private final @Nonnull JScrollPane         uniforms_scroller;
  private final @Nonnull InputsTableModel    inputs_model;

  private final @Nonnull InputsTable         inputs_table;

  private final @Nonnull JScrollPane         inputs_scroller;

  private final @Nonnull OutputsTableModel   outputs_model;

  private final @Nonnull OutputsTable        outputs_table;

  private final @Nonnull JScrollPane         outputs_scroller;

  private final @Nonnull JFrame              window;

  public SBShadersPanel(
    final @Nonnull JFrame window,
    final @Nonnull SBSceneControllerShaders controller,
    final @Nonnull Log log)
  {
    this.window = window;
    this.jlog = new Log(log, "Shaders");
    this.shaders = controller.shadersGet();

    this.selector = new JComboBox<String>();
    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final ActionEvent e)
      {
        SBShadersPanel.this.selectorShowSelected();
      }
    });

    this.open = new JButton("Open...");
    this.open.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(SBShadersPanel.META_XML_FILTER);

        final int r = chooser.showOpenDialog(SBShadersPanel.this);

        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final File file = chooser.getSelectedFile();

            final SwingWorker<SBShader, Void> worker =
              new SwingWorker<SBShader, Void>() {
                @Override protected @Nonnull SBShader doInBackground()
                  throws Exception
                {
                  try {
                    return controller.shaderLoad(file).get();
                  } catch (final ConstraintError x) {
                    throw new IOException(x);
                  }
                }

                @SuppressWarnings("synthetic-access") @Override protected
                  void
                  done()
                {
                  try {
                    this.get();

                    SBShadersPanel.this.shaders = controller.shadersGet();
                    SBShadersPanel.this.shadersUpdated();

                  } catch (final InterruptedException x) {
                    SBErrorBox.showError(
                      SBShadersPanel.this.jlog,
                      "Error loading shader",
                      x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showError(
                      SBShadersPanel.this.jlog,
                      "Error loading shader",
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

    this.uniforms_model = new UniformsTableModel(this.jlog);
    this.uniforms_table = new UniformsTable(this.uniforms_model);
    this.uniforms_scroller = new JScrollPane(this.uniforms_table);
    this.uniforms_scroller.setPreferredSize(new Dimension(this
      .getPreferredSize().width, 100));

    this.inputs_model = new InputsTableModel(this.jlog);
    this.inputs_table = new InputsTable(this.inputs_model);
    this.inputs_scroller = new JScrollPane(this.inputs_table);
    this.inputs_scroller.setPreferredSize(new Dimension(this
      .getPreferredSize().width, 100));

    this.outputs_model = new OutputsTableModel(this.jlog);
    this.outputs_table = new OutputsTable(this.outputs_model);
    this.outputs_scroller = new JScrollPane(this.outputs_table);
    this.outputs_scroller.setPreferredSize(new Dimension(this
      .getPreferredSize().width, 100));

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(this.selector, 3).add(this.open);
    dg.row().grid().add(new JLabel("Uniforms"));
    dg.row().grid().add(this.uniforms_scroller);
    dg.row().grid().add(new JLabel("Inputs"));
    dg.row().grid().add(this.inputs_scroller);
    dg.row().grid().add(new JLabel("Outputs"));
    dg.row().grid().add(this.outputs_scroller);

    this.shadersUpdated();
  }

  public @CheckForNull SBShader getSelectedShader()
  {
    final String sname = (String) this.selector.getSelectedItem();
    if (sname != null) {
      return this.shaders.get(sname);
    }

    return null;
  }

  private void selectorRefresh(
    final @Nonnull JComboBox<String> box)
  {
    box.removeAllItems();
    for (final Entry<String, SBShader> e : this.shaders.entrySet()) {
      box.addItem(e.getKey());
    }
    this.window.pack();
  }

  protected void selectorShowSelected()
  {
    final SBShader shader = this.getSelectedShader();
    if (shader != null) {
      this.uniforms_model.showShader(shader);
      this.inputs_model.showShader(shader);
      this.outputs_model.showShader(shader);
    }
  }

  protected void shadersUpdated()
  {
    this.selectorRefresh(this.selector);
    this.selectorShowSelected();
  }

  @Override public String toString()
  {
    return "[SBShadersPanel]";
  }
}
