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
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;
import net.java.dev.designgridlayout.Tag;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceWorld;
import com.io7m.renderer.types.RTransformTexture;

public final class SBInstanceControls implements
  SBControlsDataType<SBInstance>
{
  private static final class MaterialSelectDialog extends JFrame
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = -6013889187391591941L;
    }

    public <C extends SBSceneControllerMaterials & SBSceneControllerTextures> MaterialSelectDialog(
      final @Nonnull JTextField material_id,
      final @Nonnull JTextField material_name,
      final @Nonnull C controller,
      final @Nonnull Log log)
    {
      final Container content = this.getContentPane();
      final JPanel inner = new JPanel();
      final JScrollPane pane = new JScrollPane(inner);
      content.add(pane);

      final DesignGridLayout layout = new DesignGridLayout(inner);
      final SBMaterialsControls controls =
        new SBMaterialsControls(controller, log);
      controls.controlsAddToLayout(layout);

      final JTable materials = controls.getMaterialTable();

      /**
       * The OK button will be enabled/disabled based on whether a material is
       * selected or not.
       */

      final JButton ok = new JButton("OK");
      ok.setEnabled(false);
      ok.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            final SBMaterial m = controls.getSelectedMaterial();
            material_id.setText(m.materialGetID().toString());
            material_name.setText(m.materialGetName());
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException(x);
          }
        }
      });

      materials.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {
          @Override public void valueChanged(
            final @Nonnull ListSelectionEvent e)
          {
            if (materials.getSelectedRow() == -1) {
              ok.setEnabled(false);
            } else {
              ok.setEnabled(true);
            }
          }
        });

      final JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          SBWindowUtilities.closeWindow(MaterialSelectDialog.this);
        }
      });

      layout.row().bar().add(cancel, Tag.CANCEL).add(ok, Tag.OK);
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
            final SBInstanceControls controls =
              SBInstanceControls.newControls(
                this,
                log,
                controller,
                Integer.valueOf(0));
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

  public static @Nonnull
    <C extends SBSceneControllerTextures & SBSceneControllerMaterials & SBSceneControllerMeshes>
    SBInstanceControls
    newControls(
      final @Nonnull JFrame parent,
      final @Nonnull Log log,
      final @Nonnull C controller,
      final @Nonnull Integer id)
  {
    return new SBInstanceControls(parent, log, controller, id);
  }

  private final @Nonnull SBMatrix3x3Controls<RTransformTexture> matrix_uv;
  private final @Nonnull SBOrientationInput                     orientation;
  private final @Nonnull SBVector3FInput<RSpaceWorld>           position;
  private final @Nonnull SBVector3FInput<RSpaceObject>          scale;
  private final @Nonnull JTextField                             material_id;
  private final @Nonnull JTextField                             material_name;
  private final @Nonnull JButton                                material_select;
  private final @Nonnull RowGroup                               group;
  private final @Nonnull JComboBox<PathVirtual>                 mesh_selector;
  private @Nonnull Map<PathVirtual, SBMesh>                     meshes;
  private final @Nonnull JButton                                mesh_load;
  private final @Nonnull JTextField                             id_field;
  private final @Nonnull JCheckBox                              lit;
  private final @Nonnull Integer                                id;

  protected final static @Nonnull FileFilter                    MESH_RXML_FILE_FILTER;
  protected final static @Nonnull FileFilter                    MESH_RMB_FILE_FILTER;

  static {
    MESH_RXML_FILE_FILTER = new FileFilter() {
      @Override public boolean accept(
        final File f)
      {
        return f.isDirectory() || f.toString().endsWith(".rmx");
      }

      @Override public String getDescription()
      {
        return "RXML mesh files (*.rmx)";
      }
    };

    MESH_RMB_FILE_FILTER = new FileFilter() {
      @Override public boolean accept(
        final File f)
      {
        return f.isDirectory() || f.toString().endsWith(".rmb");
      }

      @Override public String getDescription()
      {
        return "RB mesh files (*.rmb)";
      }
    };
  }

  public <C extends SBSceneControllerTextures & SBSceneControllerMaterials & SBSceneControllerMeshes> SBInstanceControls(
    final @Nonnull JFrame parent,
    final @Nonnull Log log,
    final @Nonnull C controller,
    final @Nonnull Integer id)
  {
    this.id = id;
    this.id_field = new JTextField(id.toString());
    this.id_field.setEditable(false);

    this.position = SBVector3FInput.newInput("Position");
    this.scale = SBVector3FInput.newInput("Scale");
    this.orientation = SBOrientationInput.newInput();
    this.matrix_uv = new SBMatrix3x3Controls<RTransformTexture>("UV matrix");
    this.lit = new JCheckBox();
    this.lit.setSelected(true);
    this.group = new RowGroup();

    this.material_id = new JTextField("");
    this.material_id.setEditable(false);
    this.material_name = new JTextField("");
    this.material_name.setEditable(false);

    this.material_select = new JButton("Select...");
    this.material_select.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nonnull ActionEvent e)
      {
        final MaterialSelectDialog dialog =
          new MaterialSelectDialog(
            SBInstanceControls.this.material_id,
            SBInstanceControls.this.material_name,
            controller,
            log);
        dialog.pack();
        dialog.setVisible(true);
      }
    });

    this.mesh_selector = new JComboBox<PathVirtual>();
    this.meshesRefresh(controller);

    this.mesh_load = new JButton("Open...");
    this.mesh_load.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser
          .addChoosableFileFilter(SBInstanceControls.MESH_RXML_FILE_FILTER);
        chooser
          .addChoosableFileFilter(SBInstanceControls.MESH_RMB_FILE_FILTER);

        final int r = chooser.showOpenDialog(parent);
        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final File file = chooser.getSelectedFile();

            final SwingWorker<SBMesh, Void> worker =
              new SwingWorker<SBMesh, Void>() {
                @Override protected SBMesh doInBackground()
                  throws Exception
                {
                  try {
                    return controller.sceneMeshLoad(file).get();
                  } catch (final ConstraintError x) {
                    throw new IOException(x);
                  }
                }

                @Override protected void done()
                {
                  try {
                    this.get();

                    SBInstanceControls.this.meshesRefresh(controller);

                  } catch (final InterruptedException x) {
                    SBErrorBox.showErrorWithTitleLater(
                      log,
                      "Interrupted operation",
                      x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showErrorWithTitleLater(
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
  }

  protected void meshesRefresh(
    final @Nonnull SBSceneControllerMeshes controller)
  {
    this.meshes = controller.sceneMeshesGet();

    this.mesh_selector.removeAllItems();
    for (final PathVirtual name : this.meshes.keySet()) {
      this.mesh_selector.addItem(name);
    }
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    layout.row().group(this.group).grid(new JLabel("ID")).add(this.id_field);

    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Mesh"))
      .add(this.mesh_selector, 3)
      .add(this.mesh_load);

    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Material"))
      .add(this.material_id)
      .add(this.material_name, 5)
      .add(this.material_select);

    layout.row().group(this.group).grid(new JLabel("Lit")).add(this.lit);
    layout.emptyRow();

    this.position.controlsAddToLayout(layout);
    this.scale.controlsAddToLayout(layout);
    this.orientation.controlsAddToLayout(layout);
    this.matrix_uv.controlsAddToLayout(layout);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
    this.position.controlsHide();
    this.scale.controlsHide();
    this.orientation.controlsHide();
    this.matrix_uv.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
    this.position.controlsShow();
    this.scale.controlsShow();
    this.orientation.controlsShow();
    this.matrix_uv.controlsShow();
  }

  @Override public void controlsLoadFrom(
    final SBInstance t)
  {
    this.position.setVector(t.getPosition());
    this.scale.setVector(t.getScale());
    this.orientation.setOrientation(t.getOrientation());
    this.matrix_uv.controlsLoadFrom(t.getUVMatrix());
    this.mesh_selector.setSelectedItem(t.getMesh());
    this.material_id.setText(t.getMaterial().toString());
    this.lit.setSelected(t.isLit());
  }

  @SuppressWarnings("boxing") @Override public SBInstance controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    return new SBInstance(
      this.id,
      this.position.getVector(),
      this.scale.getVector(),
      this.orientation.getOrientation(),
      this.matrix_uv.controlsSave(),
      (PathVirtual) this.mesh_selector.getSelectedItem(),
      SBTextFieldUtilities.getFieldIntegerOrError(this.material_id),
      this.lit.isSelected());
  }
}
