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
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.Log;

final class SBModelsPanel extends JPanel implements SBSceneChangeListener
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }

  protected static void enableOrDisableSelect(
    final @Nonnull JTree t,
    final @Nonnull JButton button)
  {
    final DefaultMutableTreeNode node =
      (DefaultMutableTreeNode) t.getLastSelectedPathComponent();

    button.setEnabled(false);
    if (node != null) {
      if (node.isRoot() == false) {
        if (node.isLeaf()) {
          button.setEnabled(true);
        }
      }
    }
  }

  private final @Nonnull Log                       log;
  protected final @Nonnull JTree                   tree;
  protected final @Nonnull DefaultMutableTreeNode  tree_root;
  protected @Nonnull Map<String, SBModel>          models;
  protected final @Nonnull SBSceneControllerModels controller;

  SBModelsPanel(
    final @Nonnull JFrame window,
    final @Nonnull SBSceneControllerModels controller,
    final @Nonnull JTextField model,
    final @Nonnull JTextField model_object,
    final @Nonnull Log log)
  {
    this.log = new Log(log, "meshes");
    this.controller = controller;
    this.models = controller.sceneModelsGet();

    final JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        SBWindowUtilities.closeWindow(window);
      }
    });

    final JButton select = new JButton("Select");
    select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final DefaultMutableTreeNode leaf =
          (DefaultMutableTreeNode) SBModelsPanel.this.tree
            .getLastSelectedPathComponent();
        assert leaf.getParent() != null;

        final DefaultMutableTreeNode model_node =
          (DefaultMutableTreeNode) leaf.getParent();
        assert model_node != null;
        assert model_node.getUserObject() instanceof String;

        model.setText((String) model_node.getUserObject());
        model_object.setText((String) leaf.getUserObject());

        SBWindowUtilities.closeWindow(window);
      }
    });

    this.tree_root = new DefaultMutableTreeNode("Meshes");
    this.tree = new JTree(this.tree_root);
    this.tree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override public void valueChanged(
        final @Nonnull TreeSelectionEvent e)
      {
        SBModelsPanel.enableOrDisableSelect(SBModelsPanel.this.tree, select);
      }
    });

    this.modelsRefresh(this.models);
    SBModelsPanel.enableOrDisableSelect(this.tree, select);

    final JScrollPane scroller = new JScrollPane(this.tree);

    final JButton load = new JButton("Load...");
    load.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);

        final int r = chooser.showOpenDialog(SBModelsPanel.this);
        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final File file = chooser.getSelectedFile();

            final SwingWorker<SBModel, Void> worker =
              new SwingWorker<SBModel, Void>() {
                @Override protected SBModel doInBackground()
                  throws Exception
                {
                  return controller.sceneModelLoad(file).get();
                }

                @Override protected void done()
                {
                  try {
                    this.get();

                    SBModelsPanel.this.models = controller.sceneModelsGet();
                    SBModelsPanel.this
                      .modelsRefresh(SBModelsPanel.this.models);
                    SBModelsPanel.this.tree.repaint();
                    SBModelsPanel.enableOrDisableSelect(
                      SBModelsPanel.this.tree,
                      select);

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

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(scroller);
    dg.row().grid().add(load).add(cancel).add(select);

    controller.changeListenerAdd(this);
  }

  protected @CheckForNull DefaultMutableTreeNode findTreeNode(
    final @Nonnull String name)
  {
    @SuppressWarnings("unchecked") final Enumeration<DefaultMutableTreeNode> e =
      this.tree_root.children();

    while (e.hasMoreElements()) {
      final DefaultMutableTreeNode node = e.nextElement();
      final String model = (String) node.getUserObject();
      if (model.equals(name)) {
        return node;
      }
    }

    return null;
  }

  protected void modelsRefresh(
    final @Nonnull Map<String, SBModel> new_models)
  {
    this.tree_root.removeAllChildren();

    for (final Entry<String, SBModel> e : new_models.entrySet()) {
      final DefaultMutableTreeNode category =
        new DefaultMutableTreeNode(e.getKey());

      for (final String name : e.getValue().getMeshes().keySet()) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        category.add(node);
      }

      this.tree_root.add(category);
    }

    for (int row = 0; row < this.tree.getRowCount(); ++row) {
      this.tree.expandRow(row);
    }
  }

  @Override public void sceneChanged()
  {
    SwingUtilities.invokeLater(new Runnable() {

      @Override public void run()
      {
        SBModelsPanel.this.models =
          SBModelsPanel.this.controller.sceneModelsGet();
        SBModelsPanel.this.modelsRefresh(SBModelsPanel.this.models);
      }
    });
  }

  @Override public String toString()
  {
    return "[SBMeshesPanel]";
  }
}
