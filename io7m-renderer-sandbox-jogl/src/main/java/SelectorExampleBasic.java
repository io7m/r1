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

public class SelectorExampleBasic
{
  static final class BikeControls
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

    public RowGroup getGroup()
    {
      return this.group;
    }
  }

  static final class CarControls
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

    public RowGroup getGroup()
    {
      return this.group;
    }
  }

  static final class PlaneControls
  {
    private final RowGroup group;

    public PlaneControls()
    {
      this.group = new RowGroup();
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
    }

    public RowGroup getGroup()
    {
      return this.group;
    }
  }

  enum Type
  {
    BIKE("Bike"),
    CAR("Car"),
    PLANE("Plane");

    private final String name;

    private Type(
      final String name)
    {
      this.name = name;
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
        new SelectorExampleBasic(layout);
        frame.setPreferredSize(new Dimension(640, 480));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      }
    });
  }

  final BikeControls  bike_controls;
  final CarControls   car_controls;
  final JCheckBox     enabled;
  final PlaneControls plane_controls;
  final TypeSelector  selector;

  public SelectorExampleBasic(
    final DesignGridLayout layout)
  {
    this.car_controls = new CarControls();
    this.plane_controls = new PlaneControls();
    this.bike_controls = new BikeControls();

    this.enabled = new JCheckBox("Enabled");
    this.enabled.setSelected(false);
    this.selector = new TypeSelector();
    this.selector.setEnabled(false);

    this.enabled.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final ActionEvent e)
      {
        if (SelectorExampleBasic.this.enabled.isSelected()) {
          SelectorExampleBasic.this.selector.setEnabled(true);
          SelectorExampleBasic.this.selector
            .setSelectedItem(SelectorExampleBasic.this.selector
              .getSelectedItem());
        } else {
          SelectorExampleBasic.this.selector.setEnabled(false);
          SelectorExampleBasic.this.bike_controls.getGroup().hide();
          SelectorExampleBasic.this.car_controls.getGroup().hide();
          SelectorExampleBasic.this.plane_controls.getGroup().hide();
        }
      }
    });

    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final ActionEvent e)
      {
        final Type selected =
          (Type) SelectorExampleBasic.this.selector.getSelectedItem();
        System.out.println("Selected: " + selected);

        SelectorExampleBasic.this.bike_controls.getGroup().forceShow();
        SelectorExampleBasic.this.car_controls.getGroup().forceShow();
        SelectorExampleBasic.this.plane_controls.getGroup().forceShow();

        switch (selected) {
          case BIKE:
          {
            SelectorExampleBasic.this.bike_controls.getGroup().forceShow();
            SelectorExampleBasic.this.car_controls.getGroup().hide();
            SelectorExampleBasic.this.plane_controls.getGroup().hide();
            break;
          }
          case CAR:
          {
            SelectorExampleBasic.this.bike_controls.getGroup().hide();
            SelectorExampleBasic.this.car_controls.getGroup().forceShow();
            SelectorExampleBasic.this.plane_controls.getGroup().hide();
            break;
          }
          case PLANE:
          {
            SelectorExampleBasic.this.bike_controls.getGroup().hide();
            SelectorExampleBasic.this.car_controls.getGroup().hide();
            SelectorExampleBasic.this.plane_controls.getGroup().forceShow();
            break;
          }
        }
      }
    });

    layout.row().left().add(this.enabled).add(new JSeparator()).fill();
    layout.row().grid(new JLabel("Type")).add(this.selector);
    layout.emptyRow();
    this.bike_controls.addToLayout(layout.row());
    this.car_controls.addToLayout(layout.row());
    this.plane_controls.addToLayout(layout.row());

    this.bike_controls.getGroup().hide();
    this.car_controls.getGroup().hide();
    this.plane_controls.getGroup().hide();
  }
}
