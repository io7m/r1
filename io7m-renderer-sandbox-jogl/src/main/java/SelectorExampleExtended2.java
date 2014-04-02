import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;
import net.java.dev.designgridlayout.RowGroup;

public class SelectorExampleExtended2
{
  interface Hideable
  {
    void show();

    void hide();
  }

  static final class BikeControls implements Hideable
  {
    private final RowGroup group;

    public BikeControls()
    {
      this.group = new RowGroup();
    }

    public void addToLayout(
      final IRowCreator row)
    {
      row
        .group(this.group)
        .grid(new JLabel("Brake tension"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
      row
        .group(this.group)
        .grid(new JLabel("Spoke tension"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
      row
        .group(this.group)
        .grid(new JLabel("Gears"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
    }

    @Override public void show()
    {
      this.group.forceShow();
    }

    @Override public void hide()
    {
      this.group.hide();
    }
  }

  static final class CarControls implements Hideable
  {
    private final RowGroup group;

    public CarControls()
    {
      this.group = new RowGroup();
    }

    public void addToLayout(
      final IRowCreator row)
    {
      row
        .group(this.group)
        .grid(new JLabel("Horsepower"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
      row
        .group(this.group)
        .grid(new JLabel("Shininess"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
    }

    @Override public void show()
    {
      this.group.forceShow();
    }

    @Override public void hide()
    {
      this.group.hide();
    }
  }

  static final class PlaneControls implements Hideable
  {
    static final class ExtraSelector extends JComboBox<ExtraType>
    {
      private static final long serialVersionUID;

      static {
        serialVersionUID = 319658390826132910L;
      }

      public ExtraSelector()
      {
        for (final ExtraType type : ExtraType.values()) {
          this.addItem(type);
        }

        this.setSelectedItem(ExtraType.EXTRA_FUEL);
      }
    }

    static enum ExtraType
    {
      EXTRA_FUEL,
      EXTRA_LUGGAGE
    }

    private final ExtraSelector extra_selector;
    private final RowGroup      fuel_group;
    private final RowGroup      group;
    private final RowGroup      luggage_group;

    public PlaneControls()
    {
      this.group = new RowGroup();
      this.luggage_group = new RowGroup();
      this.fuel_group = new RowGroup();
      this.extra_selector = new ExtraSelector();

      this.extra_selector.addActionListener(new ActionListener() {
        @SuppressWarnings("synthetic-access") @Override public
          void
          actionPerformed(
            final ActionEvent e)
        {
          PlaneControls.this.makeSelectedVisible();
        }
      });

      this.luggage_group.hide();
      this.fuel_group.hide();
    }

    public void addToLayout(
      final IRowCreator row)
    {
      row
        .group(this.group)
        .grid(new JLabel("Engines"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
      row
        .group(this.group)
        .grid(new JLabel("Wing span"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
      row
        .group(this.group)
        .grid(new JLabel("Fuel capacity"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
      row
        .group(this.group)
        .grid(new JLabel("Luggage capacity"))
        .add(new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50));
      row
        .group(this.group)
        .grid(new JLabel("Extras"))
        .add(this.extra_selector);

      final JSlider luggage =
        new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
      row
        .group(this.luggage_group)
        .grid(new JLabel("Extra luggage"))
        .add(luggage);

      final JSlider fuel = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
      row.group(this.fuel_group).grid(new JLabel("Extra fuel")).add(fuel);
    }

    @Override public void hide()
    {
      this.group.hide();
      this.fuel_group.hide();
      this.luggage_group.hide();
    }

    private void makeSelectedVisible()
    {
      final ExtraType selected =
        (ExtraType) PlaneControls.this.extra_selector.getSelectedItem();

      System.out.println("Selected: " + selected);
      switch (selected) {
        case EXTRA_FUEL:
        {
          this.luggage_group.hide();
          this.fuel_group.forceShow();
          break;
        }
        case EXTRA_LUGGAGE:
        {
          this.luggage_group.forceShow();
          this.fuel_group.hide();
          break;
        }
      }
    }

    @Override public void show()
    {
      this.group.forceShow();
      this.makeSelectedVisible();
    }
  }

  static enum Type
  {
    BIKE("Bike"),
    CAR("Car"),
    PLANE("Plane");

    private final String name;

    private Type(
      final String in_name)
    {
      this.name = in_name;
    }

    @Override public String toString()
    {
      return this.name;
    }
  }

  static final class TypeSelector extends JComboBox<Type>
  {
    private static final long serialVersionUID;

    static {
      serialVersionUID = 319658390826132910L;
    }

    public TypeSelector()
    {
      for (final Type type : Type.values()) {
        this.addItem(type);
      }

      this.setSelectedItem(Type.BIKE);
    }
  }

  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        final JFrame frame = new JFrame("Example");
        final Container panel = frame.getContentPane();
        final DesignGridLayout layout = new DesignGridLayout(panel);
        new SelectorExampleExtended2(layout);
        frame.setPreferredSize(new Dimension(640, 480));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      }
    });
  }

  private final BikeControls  bike_controls;
  private final CarControls   car_controls;
  private final JCheckBox     enabled;
  private final PlaneControls plane_controls;
  private final TypeSelector  selector;

  public SelectorExampleExtended2(
    final DesignGridLayout layout)
  {
    this.bike_controls = new BikeControls();
    this.plane_controls = new PlaneControls();
    this.car_controls = new CarControls();

    this.enabled = new JCheckBox("Enabled");
    this.enabled.setSelected(false);
    this.selector = new TypeSelector();
    this.selector.setEnabled(false);

    this.enabled.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final ActionEvent e)
      {
        if (SelectorExampleExtended2.this.enabled.isSelected()) {
          SelectorExampleExtended2.this.selector.setEnabled(true);
          SelectorExampleExtended2.this.selector
            .setSelectedItem(SelectorExampleExtended2.this.selector
              .getSelectedItem());
        } else {
          SelectorExampleExtended2.this.selector.setEnabled(false);
          SelectorExampleExtended2.this.bike_controls.hide();
          SelectorExampleExtended2.this.plane_controls.hide();
          SelectorExampleExtended2.this.car_controls.hide();
        }
      }
    });

    this.selector.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final ActionEvent e)
      {
        final Type selected =
          (Type) SelectorExampleExtended2.this.selector.getSelectedItem();
        System.out.println("Selected: " + selected);

        switch (selected) {
          case BIKE:
          {
            SelectorExampleExtended2.this.car_controls.hide();
            SelectorExampleExtended2.this.plane_controls.hide();
            SelectorExampleExtended2.this.bike_controls.show();
            break;
          }
          case CAR:
          {
            SelectorExampleExtended2.this.car_controls.show();
            SelectorExampleExtended2.this.plane_controls.hide();
            SelectorExampleExtended2.this.bike_controls.hide();
            break;
          }
          case PLANE:
          {
            SelectorExampleExtended2.this.car_controls.hide();
            SelectorExampleExtended2.this.plane_controls.show();
            SelectorExampleExtended2.this.bike_controls.hide();
            break;
          }
        }
      }
    });

    layout.row().left().add(this.enabled).add(new JSeparator()).fill();
    layout.row().grid(new JLabel("Type")).add(this.selector);
    layout.emptyRow();

    this.bike_controls.addToLayout(layout.row());
    this.plane_controls.addToLayout(layout.row());
    this.car_controls.addToLayout(layout.row());

    this.bike_controls.hide();
    this.car_controls.hide();
    this.plane_controls.hide();
  }
}
