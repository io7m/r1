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

package com.io7m.renderer.kernel.sandbox;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

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

import com.io7m.jcanephora.ProgramAttributeType;
import com.io7m.jcanephora.ProgramType;
import com.io7m.jcanephora.ProgramUniformType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.xml.FragmentOutput;
import com.io7m.jparasol.xml.PGLSLMetaXML;
import com.io7m.renderer.kernel.KProgram;

final class SBShadersPanel extends JPanel
{
  private static class InputsTable extends JTable
  {
    private static final long      serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final InputsTableModel model;

    public InputsTable(
      final InputsTableModel in_model)
    {
      super(in_model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = in_model;
    }
  }

  private static class InputsTableModel extends AbstractTableModel
  {
    private static final long                  serialVersionUID;
    private final String[]                     column_names;
    private final ArrayList<ArrayList<String>> data;
    private final LogUsableType                log;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public InputsTableModel(
      final LogUsableType in_log)
    {
      this.log = in_log.with("shader-uniform-table");
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
      final SBShader shader)
    {
      this.data.clear();

      final KProgram kp = shader.getProgram();
      final ProgramType p = kp.getProgram();
      for (final Entry<String, ProgramAttributeType> e : p
        .programGetAttributes()
        .entrySet()) {
        final String n = e.getKey();
        final ProgramAttributeType a = e.getValue();
        final ArrayList<String> row = new ArrayList<String>();
        row.add(n);
        row.add(Integer.toString(a.attributeGetLocation()));
        row.add(a.attributeGetType().getName());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static class OutputsTable extends JTable
  {
    private static final long       serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final OutputsTableModel model;

    public OutputsTable(
      final OutputsTableModel in_model)
    {
      super(in_model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = in_model;
    }
  }

  private static class OutputsTableModel extends AbstractTableModel
  {
    private static final long                  serialVersionUID;
    private final String[]                     column_names;
    private final ArrayList<ArrayList<String>> data;
    private final LogUsableType                log;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public OutputsTableModel(
      final LogUsableType in_log)
    {
      this.log = in_log.with("shader-uniform-table");
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
      final SBShader shader)
    {
      this.data.clear();

      final PGLSLMetaXML m = shader.getMeta();
      for (final Entry<Integer, FragmentOutput> e : m
        .getDeclaredFragmentOutputs()
        .entrySet()) {
        final FragmentOutput o = e.getValue();
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
    private static final long        serialVersionUID;

    static {
      serialVersionUID = -3198912965434164747L;
    }

    private final UniformsTableModel model;

    public UniformsTable(
      final UniformsTableModel in_model)
    {
      super(in_model);

      this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      this.setAutoCreateRowSorter(true);
      this.setAutoCreateColumnsFromModel(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.setFillsViewportHeight(true);

      this.model = in_model;
    }
  }

  private static class UniformsTableModel extends AbstractTableModel
  {
    private static final long                  serialVersionUID;
    private final String[]                     column_names;
    private final ArrayList<ArrayList<String>> data;
    private final LogUsableType                log;

    static {
      serialVersionUID = 6499860137289000995L;
    }

    public UniformsTableModel(
      final LogUsableType in_log)
    {
      this.log = in_log.with("shader-uniform-table");
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
      final SBShader shader)
    {
      this.data.clear();

      final KProgram kp = shader.getProgram();
      final ProgramType p = kp.getProgram();
      for (final Entry<String, ProgramUniformType> e : p
        .programGetUniforms()
        .entrySet()) {
        final String n = e.getKey();
        final ProgramUniformType u = e.getValue();
        final ArrayList<String> row = new ArrayList<String>();
        row.add(n);
        row.add(Integer.toString(u.uniformGetLocation()));
        row.add(u.uniformGetType().getName());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static final long         serialVersionUID;

  protected static final FileFilter META_XML_FILTER;
  static {
    serialVersionUID = -941448169051827275L;

    META_XML_FILTER = new FileFilter() {
      @Override public boolean accept(
        final @Nullable File f)
      {
        assert f != null;
        return (f.isDirectory() || f.getName().equals("meta.xml"));
      }

      @Override public String getDescription()
      {
        return "All shaders (\"meta.xml\")";
      }
    };
  }

  private final JComboBox<String>   selector;
  private final JButton             open;
  private final LogUsableType       jlog;
  private Map<String, SBShader>     shaders;
  private final UniformsTableModel  uniforms_model;
  private final UniformsTable       uniforms_table;
  private final JScrollPane         uniforms_scroller;
  private final InputsTableModel    inputs_model;
  private final InputsTable         inputs_table;
  private final JScrollPane         inputs_scroller;
  private final OutputsTableModel   outputs_model;
  private final OutputsTable        outputs_table;
  private final JScrollPane         outputs_scroller;
  private final JFrame              window;

  public SBShadersPanel(
    final JFrame in_window,
    final SBSceneControllerShaders controller,
    final LogUsableType log)
  {
    this.window = in_window;
    this.jlog = log.with("ForwardShaders");
    this.shaders = controller.shadersGet();

    this.selector = new JComboBox<String>();
    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        SBShadersPanel.this.selectorShowSelected();
      }
    });

    this.open = new JButton("Open...");
    this.open.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
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
                @Override protected SBShader doInBackground()
                  throws Exception
                {
                  return controller.shaderLoad(file).get();
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
                    SBErrorBox.showErrorWithTitleLater(
                      SBShadersPanel.this.jlog,
                      "Error loading shader",
                      x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showErrorWithTitleLater(
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

  public @Nullable SBShader getSelectedShader()
  {
    final String sname = (String) this.selector.getSelectedItem();
    if (sname != null) {
      return this.shaders.get(sname);
    }

    return null;
  }

  private void selectorRefresh(
    final JComboBox<String> box)
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
