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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformTexture;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

final class SBObjectsPanel extends JPanel implements SBSceneChangeListener
{
  private static class AlbedoSettings implements
    MaterialPanel<SBMaterialAlbedoDescription>
  {
    protected final @Nonnull JTextField     r;
    protected final @Nonnull JTextField     g;
    protected final @Nonnull JTextField     b;
    protected final @Nonnull JTextField     a;
    protected final @Nonnull JButton        colour_select;
    protected final @Nonnull JTextField     texture;
    protected final @Nonnull JButton        texture_select;
    protected final @Nonnull SBFloatHSlider texture_mix;

    AlbedoSettings(
      final @Nonnull SBSceneControllerTextures controller,
      final @Nonnull JPanel owner,
      final @Nonnull Log log)
      throws ConstraintError
    {
      this.r = new JTextField("1.0");
      this.g = new JTextField("1.0");
      this.b = new JTextField("1.0");
      this.a = new JTextField("1.0");
      this.colour_select = new JButton("Select...");
      this.colour_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final Color c =
            JColorChooser.showDialog(owner, "Select colour...", Color.WHITE);
          if (c != null) {
            final float[] rgb = c.getRGBColorComponents(null);
            AlbedoSettings.this.r.setText(Float.toString(rgb[0]));
            AlbedoSettings.this.g.setText(Float.toString(rgb[1]));
            AlbedoSettings.this.b.setText(Float.toString(rgb[2]));
          }
        }
      });

      this.texture = new JTextField();
      this.texture.setEditable(false);
      this.texture_select = new JButton("Select...");
      this.texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTextures2DWindow twindow =
            new SBTextures2DWindow(
              controller,
              AlbedoSettings.this.texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });

      this.texture_mix = new SBFloatHSlider("Mix", 0.0f, 1.0f);
    }

    @Override public void mpLayout(
      final @Nonnull DesignGridLayout dg)
    {
      dg
        .row()
        .grid()
        .add(new JLabel("Colour"))
        .add(this.r)
        .add(this.g)
        .add(this.b)
        .add(this.a)
        .add(this.colour_select);

      dg
        .row()
        .grid()
        .add(new JLabel("Texture"))
        .add(this.texture, 4)
        .add(this.texture_select);

      dg
        .row()
        .grid()
        .add(this.texture_mix.getLabel())
        .add(this.texture_mix.getSlider(), 4)
        .add(this.texture_mix.getField());
    }

    @Override public void mpLoadFrom(
      final @Nonnull SBInstanceDescription i)
    {
      final SBMaterialAlbedoDescription mat_d = i.getMaterial().getAlbedo();

      this.r.setText(Float.toString(mat_d.getColour().x));
      this.g.setText(Float.toString(mat_d.getColour().y));
      this.b.setText(Float.toString(mat_d.getColour().z));
      this.a.setText(Float.toString(mat_d.getColour().w));

      final PathVirtual t = mat_d.getTexture();
      if (t != null) {
        this.texture.setText(t.toString());
      }

      this.texture_mix.setCurrent(mat_d.getMix());
    }

    @Override public @Nonnull SBMaterialAlbedoDescription mpSave()
      throws SBExceptionInputError,
        ConstraintError
    {
      final RVectorI4F<RSpaceRGBA> albedo_colour =
        new RVectorI4F<RSpaceRGBA>(
          SBTextFieldUtilities.getFieldFloatOrError(this.r),
          SBTextFieldUtilities.getFieldFloatOrError(this.g),
          SBTextFieldUtilities.getFieldFloatOrError(this.b),
          SBTextFieldUtilities.getFieldFloatOrError(this.a));

      final String tt = this.texture.getText();

      final PathVirtual albedo_texture_value =
        (tt.equals("")) ? null : PathVirtual.ofString(tt);

      final SBMaterialAlbedoDescription albedo =
        new SBMaterialAlbedoDescription(
          albedo_colour,
          this.texture_mix.getCurrent(),
          albedo_texture_value);

      return albedo;
    }
  }

  private static class AlphaSettings implements
    MaterialPanel<SBMaterialAlphaDescription>
  {
    protected final @Nonnull JCheckBox      translucent;
    protected final @Nonnull SBFloatHSlider opacity;

    public AlphaSettings()
      throws ConstraintError
    {
      this.opacity = new SBFloatHSlider("Opacity", 0.0f, 1.0f);
      this.translucent = new JCheckBox();
    }

    @Override public void mpLayout(
      final DesignGridLayout dg)
    {
      dg
        .row()
        .grid()
        .add(this.opacity.getLabel())
        .add(this.opacity.getSlider(), 3)
        .add(this.opacity.getField());

      dg.emptyRow();

      dg.row().grid().add(new JLabel("Translucent")).add(this.translucent);
    }

    @Override public void mpLoadFrom(
      final SBInstanceDescription i)
    {
      final SBMaterialAlphaDescription mat_a = i.getMaterial().getAlpha();
      this.translucent.setSelected(mat_a.isTranslucent());
      this.opacity.setCurrent(mat_a.getOpacity());
    }

    @Override public SBMaterialAlphaDescription mpSave()
      throws SBExceptionInputError,
        ConstraintError
    {
      return new SBMaterialAlphaDescription(
        this.translucent.isSelected(),
        this.opacity.getCurrent());
    }

  }

  private static class EmissiveSettings implements
    MaterialPanel<SBMaterialEmissiveDescription>
  {
    protected final @Nonnull JTextField     texture;
    protected final @Nonnull JButton        texture_select;
    protected final @Nonnull SBFloatHSlider level;

    public EmissiveSettings(
      final @Nonnull SBSceneControllerTextures controller,
      final @Nonnull Log log)
      throws ConstraintError
    {
      this.texture = new JTextField();
      this.texture.setEditable(false);
      this.texture_select = new JButton("Select...");
      this.texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTextures2DWindow twindow =
            new SBTextures2DWindow(
              controller,
              EmissiveSettings.this.texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });

      this.level = new SBFloatHSlider("Emission", 0.0f, 1.0f);
    }

    @Override public void mpLayout(
      final DesignGridLayout dg)
    {
      dg
        .row()
        .grid()
        .add(new JLabel("Texture"))
        .add(this.texture, 3)
        .add(this.texture_select);

      dg
        .row()
        .grid()
        .add(this.level.getLabel())
        .add(this.level.getSlider(), 3)
        .add(this.level.getField());
    }

    @Override public void mpLoadFrom(
      final SBInstanceDescription i)
    {
      final SBMaterialEmissiveDescription mat_m =
        i.getMaterial().getEmissive();
      this.level.setCurrent(mat_m.getEmission());
      final PathVirtual tt = mat_m.getTexture();
      this.texture.setText(tt == null ? "" : tt.toString());
    }

    @Override public SBMaterialEmissiveDescription mpSave()
      throws SBExceptionInputError,
        ConstraintError
    {
      final String tt = this.texture.getText();
      final PathVirtual path =
        (tt.equals("")) ? null : PathVirtual.ofString(tt);

      return new SBMaterialEmissiveDescription(this.level.getCurrent(), path);
    }

  }

  private static class EnvironmentSettings implements
    MaterialPanel<SBMaterialEnvironmentDescription>
  {
    protected final @Nonnull JTextField     texture;
    protected final @Nonnull JButton        texture_select;
    protected final @Nonnull SBFloatHSlider mix;
    protected final @Nonnull SBFloatHSlider reflection_mix;
    protected final @Nonnull SBFloatHSlider refraction_index;
    protected final @Nonnull JCheckBox      spec_map;

    public EnvironmentSettings(
      final @Nonnull SBSceneControllerTextures controller,
      final @Nonnull Log log)
      throws ConstraintError
    {
      this.texture = new JTextField();
      this.texture.setEditable(false);
      this.texture_select = new JButton("Select...");
      this.texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTexturesCubeWindow twindow =
            new SBTexturesCubeWindow(
              controller,
              EnvironmentSettings.this.texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });

      this.mix = new SBFloatHSlider("Mix", 0.0f, 1.0f);
      this.reflection_mix = new SBFloatHSlider("Reflection mix", 0.0f, 1.0f);
      this.refraction_index =
        new SBFloatHSlider("Refraction index", 0.0f, 10.0f);
      this.spec_map = new JCheckBox();
    }

    @Override public void mpLayout(
      final @Nonnull DesignGridLayout dg)
    {
      dg
        .row()
        .grid()
        .add(new JLabel("Texture"))
        .add(this.texture, 3)
        .add(this.texture_select);

      dg
        .row()
        .grid()
        .add(this.mix.getLabel())
        .add(this.mix.getSlider(), 3)
        .add(this.mix.getField());

      dg
        .row()
        .grid()
        .add(this.reflection_mix.getLabel())
        .add(this.reflection_mix.getSlider(), 3)
        .add(this.reflection_mix.getField());

      dg
        .row()
        .grid()
        .add(this.refraction_index.getLabel())
        .add(this.refraction_index.getSlider(), 3)
        .add(this.refraction_index.getField());

      dg.emptyRow();

      dg
        .row()
        .grid()
        .add(new JLabel("Mix from specular map"))
        .add(this.spec_map, 4);
    }

    @Override public void mpLoadFrom(
      final @Nonnull SBInstanceDescription i)
    {
      final SBMaterialEnvironmentDescription mat_e =
        i.getMaterial().getEnvironment();

      final PathVirtual tt = mat_e.getTexture();
      this.texture.setText(tt == null ? "" : tt.toString());
      this.mix.setCurrent(mat_e.getMix());
      this.reflection_mix.setCurrent(mat_e.getReflectionMix());
      this.refraction_index.setCurrent(mat_e.getRefractionIndex());
      this.spec_map.setSelected(mat_e.getMixFromSpecularMap());
    }

    @Override public @Nonnull SBMaterialEnvironmentDescription mpSave()
      throws SBExceptionInputError,
        ConstraintError
    {
      final String tt = this.texture.getText();
      final PathVirtual environment_texture_value =
        (tt.equals("")) ? null : PathVirtual.ofString(tt);

      final SBMaterialEnvironmentDescription environment =
        new SBMaterialEnvironmentDescription(
          environment_texture_value,
          this.mix.getCurrent(),
          this.reflection_mix.getCurrent(),
          this.refraction_index.getCurrent(),
          this.spec_map.isSelected());

      return environment;
    }
  }

  private static class GeneralSettings implements
    MaterialPanel<RMatrixI3x3F<RTransformTexture>>
  {
    protected final @Nonnull SBMatrix3x3Fields<RTransformTexture> matrix;

    GeneralSettings()
    {
      this.matrix = new SBMatrix3x3Fields<RTransformTexture>();
    }

    @Override public void mpLayout(
      final DesignGridLayout d)
    {
      d.row().grid().add(new JLabel("UV matrix"));

      for (int r = 0; r < 3; ++r) {
        d
          .row()
          .grid()
          .add(this.matrix.getRowColumnField(r, 0))
          .add(this.matrix.getRowColumnField(r, 1))
          .add(this.matrix.getRowColumnField(r, 2));
      }
    }

    @Override public void mpLoadFrom(
      final SBInstanceDescription i)
    {
      this.matrix.setMatrix(i.getUVMatrix());
    }

    @Override public RMatrixI3x3F<RTransformTexture> mpSave()
      throws SBExceptionInputError,
        ConstraintError
    {
      return this.matrix.getMatrix3x3f();
    }
  }

  interface MaterialPanel<T>
  {
    public void mpLayout(
      final @Nonnull DesignGridLayout d);

    public void mpLoadFrom(
      final @Nonnull SBInstanceDescription i);

    public @Nonnull T mpSave()
      throws SBExceptionInputError,
        ConstraintError;
  }

  private static class NormalSettings implements
    MaterialPanel<SBMaterialNormalDescription>
  {
    protected final @Nonnull JTextField texture;
    protected final @Nonnull JButton    texture_select;

    public NormalSettings(
      final @Nonnull SBSceneControllerTextures controller,
      final @Nonnull Log log)
    {
      this.texture = new JTextField();
      this.texture.setEditable(false);
      this.texture_select = new JButton("Select...");
      this.texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTextures2DWindow twindow =
            new SBTextures2DWindow(
              controller,
              NormalSettings.this.texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });
    }

    @Override public void mpLayout(
      final @Nonnull DesignGridLayout dg)
    {
      dg
        .row()
        .grid()
        .add(new JLabel("Texture"))
        .add(this.texture, 3)
        .add(this.texture_select);
    }

    @Override public void mpLoadFrom(
      final @Nonnull SBInstanceDescription i)
    {
      final SBMaterialNormalDescription mat_n = i.getMaterial().getNormal();
      final PathVirtual tt = mat_n.getTexture();
      this.texture.setText(tt == null ? "" : tt.toString());
    }

    @Override public @Nonnull SBMaterialNormalDescription mpSave()
      throws SBExceptionInputError,
        ConstraintError
    {
      final String tt = this.texture.getText();
      final PathVirtual texture_normal =
        (tt.equals("")) ? null : PathVirtual.ofString(tt);

      return new SBMaterialNormalDescription(texture_normal);
    }

  }

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
      throws IOException,
        ConstraintError
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
      throws IOException,
        ConstraintError
    {
      this.panel =
        new ObjectEditDialogPanel(this, controller, initial_desc, data, log);
      this.setTitle("Edit object...");
      this.getContentPane().add(this.panel);
    }
  }

  private static class ObjectEditDialogPanel extends JPanel
  {
    private static final long                                     serialVersionUID;

    static {
      serialVersionUID = -3467271842953066384L;
    }

    protected final @Nonnull AlbedoSettings                       albedo_settings;
    protected final @Nonnull AlphaSettings                        alpha_settings;
    protected final @Nonnull EmissiveSettings                     emissive_settings;
    protected final @Nonnull EnvironmentSettings                  environment_settings;
    protected final @Nonnull NormalSettings                       normal_settings;
    protected final @Nonnull SpecularSettings                     specular_settings;

    protected @Nonnull Map<PathVirtual, SBMesh>                   meshes;
    protected final @Nonnull JComboBox<PathVirtual>               mesh_selector;
    protected final @Nonnull JButton                              mesh_load;

    protected final @Nonnull JTextField                           position_x;
    protected final @Nonnull JTextField                           position_y;
    protected final @Nonnull JTextField                           position_z;

    protected final @Nonnull JTextField                           scale_x;
    protected final @Nonnull JTextField                           scale_y;
    protected final @Nonnull JTextField                           scale_z;

    protected final @Nonnull JTextField                           orientation_x;
    protected final @Nonnull JTextField                           orientation_y;
    protected final @Nonnull JTextField                           orientation_z;

    protected final @Nonnull SBMatrix3x3Fields<RTransformTexture> matrix_uv;

    protected final @Nonnull JLabel                               error_icon;
    protected final @Nonnull JLabel                               error_text;

    private final @Nonnull ObjectsTableModel                      objects_table_model;
    private final @Nonnull GeneralSettings                        general_settings;
    protected final static @Nonnull FileFilter                    MESH_FILE_FILTER;

    static {
      MESH_FILE_FILTER = new FileFilter() {
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
    }

    public <C extends SBSceneControllerMeshes & SBSceneControllerInstances & SBSceneControllerTextures> ObjectEditDialogPanel(
      final @Nonnull ObjectEditDialog window,
      final @Nonnull C controller,
      final @CheckForNull SBInstanceDescription initial_desc,
      final @Nonnull ObjectsTableModel objects_table_model,
      final @Nonnull Log log)
      throws IOException,
        ConstraintError
    {
      this.objects_table_model = objects_table_model;

      this.albedo_settings = new AlbedoSettings(controller, this, log);
      this.alpha_settings = new AlphaSettings();
      this.environment_settings = new EnvironmentSettings(controller, log);
      this.emissive_settings = new EmissiveSettings(controller, log);
      this.normal_settings = new NormalSettings(controller, log);
      this.specular_settings = new SpecularSettings(controller, log);
      this.general_settings = new GeneralSettings();

      this.error_text = new JLabel("Some informative error text");
      this.error_icon = SBIcons.makeErrorIcon();
      this.error_text.setVisible(false);
      this.error_icon.setVisible(false);

      this.position_x = new JTextField("0.0");
      this.position_y = new JTextField("0.0");
      this.position_z = new JTextField("0.0");

      this.scale_x = new JTextField("1.0");
      this.scale_y = new JTextField("1.0");
      this.scale_z = new JTextField("1.0");

      this.orientation_x = new JTextField("0.0");
      this.orientation_y = new JTextField("0.0");
      this.orientation_z = new JTextField("0.0");

      this.matrix_uv = new SBMatrix3x3Fields<RTransformTexture>();

      this.mesh_selector = new JComboBox<PathVirtual>();
      this.meshesRefresh(controller);

      this.mesh_load = new JButton("Open...");
      this.mesh_load.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final JFileChooser chooser = new JFileChooser();
          chooser.setMultiSelectionEnabled(false);
          chooser.setFileFilter(ObjectEditDialogPanel.MESH_FILE_FILTER);

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

                      ObjectEditDialogPanel.this.meshesRefresh(controller);

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
          } catch (final ConstraintError x) {
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
          } catch (final ConstraintError x) {
            ObjectEditDialogPanel.this.setError(x.getMessage());
          }
        }
      });

      final JTabbedPane tabs = new JTabbedPane();

      {
        final JPanel p = new JPanel();
        final DesignGridLayout d = new DesignGridLayout(p);
        this.general_settings.mpLayout(d);
        tabs.add("General", p);
      }

      {
        final JPanel p = new JPanel();
        final DesignGridLayout d = new DesignGridLayout(p);
        this.albedo_settings.mpLayout(d);
        tabs.add("Albedo", p);
      }

      {
        final JPanel p = new JPanel();
        final DesignGridLayout d = new DesignGridLayout(p);
        this.alpha_settings.mpLayout(d);
        tabs.add("Alpha", p);
      }

      {
        final JPanel p = new JPanel();
        final DesignGridLayout d = new DesignGridLayout(p);
        this.emissive_settings.mpLayout(d);
        tabs.add("Emission", p);
      }

      {
        final JPanel p = new JPanel();
        final DesignGridLayout d = new DesignGridLayout(p);
        this.environment_settings.mpLayout(d);
        tabs.add("Environment", p);
      }

      {
        final JPanel p = new JPanel();
        final DesignGridLayout d = new DesignGridLayout(p);
        this.normal_settings.mpLayout(d);
        tabs.add("Normal", p);
      }

      {
        final JPanel p = new JPanel();
        final DesignGridLayout d = new DesignGridLayout(p);
        this.specular_settings.mpLayout(d);
        tabs.add("Specularity", p);
      }

      {
        final DesignGridLayout d = new DesignGridLayout(this);

        if (initial_desc != null) {
          final JTextField id_field =
            new JTextField(initial_desc.getID().toString());
          id_field.setEditable(false);
          d.row().grid().add(new JLabel("ID")).add(id_field, 3);
        }

        d
          .row()
          .grid()
          .add(new JLabel("Mesh"))
          .add(this.mesh_selector, 3)
          .add(this.mesh_load);

        d.emptyRow();

        d
          .row()
          .grid()
          .add(new JLabel("Position"))
          .add(this.position_x)
          .add(this.position_y)
          .add(this.position_z);

        d
          .row()
          .grid()
          .add(new JLabel("Scale"))
          .add(this.scale_x)
          .add(this.scale_y)
          .add(this.scale_z);

        d
          .row()
          .grid()
          .add(new JLabel("Orientation"))
          .add(this.orientation_x)
          .add(this.orientation_y)
          .add(this.orientation_z);

        d.emptyRow();
        d.row().left().add(new JSeparator()).fill();
        d.emptyRow();

        d.row().grid().add(tabs);
        d.row().grid().add(apply).add(cancel).add(finish);
        d.emptyRow();
        d.row().left().add(this.error_icon, this.error_text).fill();
      }

      if (initial_desc != null) {
        this.loadObject(initial_desc);
      }
    }

    private void loadObject(
      final @Nonnull SBInstanceDescription initial_desc)
    {
      final RVectorReadable3F<RSpaceWorld> pos = initial_desc.getPosition();
      final VectorI3F scale = initial_desc.getScale();
      final RVectorReadable3F<SBDegrees> ori = initial_desc.getOrientation();

      {
        final int count = this.mesh_selector.getItemCount();
        for (int index = 0; index < count; ++index) {
          final PathVirtual item = this.mesh_selector.getItemAt(index);
          if (item.equals(initial_desc.getMesh())) {
            this.mesh_selector.setSelectedIndex(index);
            break;
          }
        }
      }

      this.position_x.setText(Float.toString(pos.getXF()));
      this.position_y.setText(Float.toString(pos.getYF()));
      this.position_z.setText(Float.toString(pos.getZF()));

      this.scale_x.setText(Float.toString(scale.getXF()));
      this.scale_y.setText(Float.toString(scale.getYF()));
      this.scale_z.setText(Float.toString(scale.getZF()));

      this.orientation_x.setText(Float.toString(ori.getXF()));
      this.orientation_y.setText(Float.toString(ori.getYF()));
      this.orientation_z.setText(Float.toString(ori.getZF()));

      this.albedo_settings.mpLoadFrom(initial_desc);
      this.alpha_settings.mpLoadFrom(initial_desc);
      this.environment_settings.mpLoadFrom(initial_desc);
      this.emissive_settings.mpLoadFrom(initial_desc);
      this.normal_settings.mpLoadFrom(initial_desc);
      this.specular_settings.mpLoadFrom(initial_desc);
    }

    protected void meshesRefresh(
      final @Nonnull SBSceneControllerMeshes controller)
    {
      ObjectEditDialogPanel.this.meshes = controller.sceneMeshesGet();

      this.mesh_selector.removeAllItems();
      for (final PathVirtual name : this.meshes.keySet()) {
        this.mesh_selector.addItem(name);
      }
    }

    protected void saveObject(
      final @Nonnull SBSceneControllerInstances controller,
      final @CheckForNull SBInstanceDescription initial)
      throws SBExceptionInputError,
        ConstraintError
    {
      final Integer id =
        (initial == null) ? controller.sceneInstanceFreshID() : initial
          .getID();

      final RVectorI3F<RSpaceWorld> position =
        new RVectorI3F<RSpaceWorld>(
          SBTextFieldUtilities.getFieldFloatOrError(this.position_x),
          SBTextFieldUtilities.getFieldFloatOrError(this.position_y),
          SBTextFieldUtilities.getFieldFloatOrError(this.position_z));

      final VectorI3F scale =
        new VectorI3F(
          SBTextFieldUtilities.getFieldFloatOrError(this.scale_x),
          SBTextFieldUtilities.getFieldFloatOrError(this.scale_y),
          SBTextFieldUtilities.getFieldFloatOrError(this.scale_z));

      final RVectorI3F<SBDegrees> orientation =
        new RVectorI3F<SBDegrees>(
          SBTextFieldUtilities.getFieldFloatOrError(this.orientation_x),
          SBTextFieldUtilities.getFieldFloatOrError(this.orientation_y),
          SBTextFieldUtilities.getFieldFloatOrError(this.orientation_z));

      final SBMaterialAlphaDescription alpha = this.alpha_settings.mpSave();
      final SBMaterialAlbedoDescription albedo =
        this.albedo_settings.mpSave();
      final SBMaterialEnvironmentDescription environment =
        this.environment_settings.mpSave();
      final SBMaterialNormalDescription normal =
        this.normal_settings.mpSave();
      final SBMaterialSpecularDescription specular =
        this.specular_settings.mpSave();
      final SBMaterialEmissiveDescription emissive =
        this.emissive_settings.mpSave();
      final RMatrixI3x3F<RTransformTexture> material_uv_matrix =
        this.general_settings.mpSave();

      final RMatrixI3x3F<RTransformTexture> instance_uv_matrix =
        material_uv_matrix;

      final SBMaterialDescription material =
        new SBMaterialDescription(
          alpha,
          albedo,
          emissive,
          specular,
          environment,
          normal,
          material_uv_matrix);

      final PathVirtual mesh_name =
        (PathVirtual) this.mesh_selector.getSelectedItem();
      if (mesh_name == null) {
        throw new SBExceptionInputError("Mesh is unset");
      }

      final SBInstanceDescription d =
        new SBInstanceDescription(
          id,
          position,
          scale,
          orientation,
          instance_uv_matrix,
          mesh_name,
          material);

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

    protected void refreshObjects()
    {
      this.data.clear();
      final Collection<SBInstance> objects =
        this.controller.sceneInstancesGetAll();

      for (final SBInstance o : objects) {
        final ArrayList<String> row = new ArrayList<String>();
        row.add(o.getID().toString());
        row.add(o.getMesh().toString());
        this.data.add(row);
      }

      this.fireTableDataChanged();
    }
  }

  private static class SpecularSettings implements
    MaterialPanel<SBMaterialSpecularDescription>
  {
    protected final @Nonnull JTextField     texture;
    protected final @Nonnull JButton        texture_select;
    protected final @Nonnull SBFloatHSlider intensity;
    protected final @Nonnull SBFloatHSlider exponent;

    public SpecularSettings(
      final @Nonnull SBSceneControllerTextures controller,
      final @Nonnull Log log)
      throws ConstraintError
    {
      this.texture = new JTextField();
      this.texture.setEditable(false);
      this.texture_select = new JButton("Select...");
      this.texture_select.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SBTextures2DWindow twindow =
            new SBTextures2DWindow(
              controller,
              SpecularSettings.this.texture,
              log);
          twindow.pack();
          twindow.setVisible(true);
        }
      });

      this.exponent = new SBFloatHSlider("Exponent", 1.0f, 128.0f);
      this.intensity = new SBFloatHSlider("Intensity", 0.0f, 1.0f);
    }

    @Override public void mpLayout(
      final DesignGridLayout dg)
    {
      dg
        .row()
        .grid()
        .add(new JLabel("Texture"))
        .add(this.texture, 3)
        .add(this.texture_select);

      dg
        .row()
        .grid()
        .add(this.intensity.getLabel())
        .add(this.intensity.getSlider(), 3)
        .add(this.intensity.getField());

      dg
        .row()
        .grid()
        .add(this.exponent.getLabel())
        .add(this.exponent.getSlider(), 3)
        .add(this.exponent.getField());
    }

    @Override public void mpLoadFrom(
      final SBInstanceDescription i)
    {
      final SBMaterialSpecularDescription mat_s =
        i.getMaterial().getSpecular();

      final PathVirtual tt = mat_s.getTexture();
      this.texture.setText(tt == null ? "" : tt.toString());
      this.exponent.setCurrent(mat_s.getExponent());
      this.intensity.setCurrent(mat_s.getIntensity());
    }

    @Override public SBMaterialSpecularDescription mpSave()
      throws SBExceptionInputError,
        ConstraintError
    {
      final String tt = this.texture.getText();
      final PathVirtual specular_texture_value =
        (tt.equals("")) ? null : PathVirtual.ofString(tt);

      final SBMaterialSpecularDescription specular =
        new SBMaterialSpecularDescription(
          specular_texture_value,
          this.intensity.getCurrent(),
          this.exponent.getCurrent());

      return specular;
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
        } catch (final ConstraintError x) {
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
        } catch (final ConstraintError x) {
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
        try {
          final int view_row = SBObjectsPanel.this.objects.getSelectedRow();
          assert view_row != -1;
          final int model_row =
            SBObjectsPanel.this.objects.convertRowIndexToModel(view_row);
          final SBInstance object =
            SBObjectsPanel.this.objects_model.getInstanceAt(model_row);
          assert object != null;

          controller.sceneInstanceRemove(object.getID());
          SBObjectsPanel.this.objects_model.refreshObjects();
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
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

    controller.sceneChangeListenerAdd(this);
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
