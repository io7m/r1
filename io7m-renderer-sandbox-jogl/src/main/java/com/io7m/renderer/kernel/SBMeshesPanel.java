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
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.functional.Pair;
import com.io7m.jlog.Log;

final class SBMeshesPanel extends JPanel
{
  private static final long           serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }

  private final @Nonnull ObjectsPanel objects;

  private final static class ObjectsPanel extends JPanel
  {
    private static final long                       serialVersionUID;

    static {
      serialVersionUID = 1042996532266058580L;
    }

    protected final @Nonnull JTree                  tree;
    protected final @Nonnull DefaultMutableTreeNode tree_root;
    protected final @Nonnull Log                    log_meshes;

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

    ObjectsPanel(
      final @Nonnull SBSceneControllerMeshes controller,
      final @Nonnull Log log)
    {
      this.log_meshes = new Log(log, "meshes");

      this.setBorder(BorderFactory.createTitledBorder("Meshes"));

      this.tree_root = new DefaultMutableTreeNode("Meshes");
      this.tree = new JTree(this.tree_root);
      this.tree.getSelectionModel().setSelectionMode(
        TreeSelectionModel.SINGLE_TREE_SELECTION);
      final JScrollPane scroller = new JScrollPane(this.tree);

      final JButton load = new JButton("Load...");
      load.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final JFileChooser chooser = new JFileChooser();
          chooser.setMultiSelectionEnabled(false);

          final int r = chooser.showOpenDialog(ObjectsPanel.this);
          switch (r) {
            case JFileChooser.APPROVE_OPTION:
            {
              final File file = chooser.getSelectedFile();

              final SwingWorker<Pair<File, SortedSet<String>>, Void> worker =
                new SwingWorker<Pair<File, SortedSet<String>>, Void>() {
                  @Override protected
                    Pair<File, SortedSet<String>>
                    doInBackground()
                      throws Exception
                  {
                    return controller.meshLoad(file).get();
                  }

                  @Override protected void done()
                  {
                    try {
                      final Pair<File, SortedSet<String>> v = this.get();

                      final DefaultMutableTreeNode original =
                        ObjectsPanel.this.findTreeNode(file.toString());

                      final DefaultMutableTreeNode category =
                        (original == null) ? new DefaultMutableTreeNode(file
                          .toString()) : original;

                      category.removeAllChildren();
                      for (final String object_name : v.second) {
                        log.debug("Loaded object " + object_name);
                        category.add(new DefaultMutableTreeNode(object_name));
                      }
                      if (category != original) {
                        ObjectsPanel.this.tree_root.add(category);
                      }

                      ObjectsPanel.this.tree.repaint();

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
      dg.row().grid().add(load);
    }
  }

  private final @Nonnull Log log;

  public SBMeshesPanel(
    final @Nonnull SBSceneControllerMeshes controller,
    final @Nonnull Log log)
  {
    this.log = new Log(log, "meshes");

    this.setPreferredSize(new Dimension(640, 480));

    this.objects = new ObjectsPanel(controller, this.log);

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(this.objects);
  }
}
