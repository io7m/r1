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
import net.java.dev.designgridlayout.IHideable;
import net.java.dev.designgridlayout.IRowCreator;
import net.java.dev.designgridlayout.RowGroup;

public class SelectorExampleExtended0
{
  static final class BikeControls implements IHideable
  {
    private final RowGroup group;
    private int            hides;
    private int            shows;

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

    @Override public void forceShow()
    {
      this.hides = 0;
      this.shows = 1;
      this.group.forceShow();
    }

    @Override public void hide()
    {
      this.hides++;
      this.shows--;
      this.group.hide();
      this.showState();
    }

    @Override public void show()
    {
      this.shows++;
      this.hides--;
      this.group.show();
      this.showState();
    }

    @SuppressWarnings("boxing") private void showState()
    {
      System.out.printf("Bike: shows %d hides %d\n", this.shows, this.hides);
    }
  }

  static final class CarControls implements IHideable
  {
    private final RowGroup group;
    private int            hides;
    private int            shows;

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

    @Override public void forceShow()
    {
      this.hides = 0;
      this.shows = 1;
      this.group.forceShow();
    }

    @Override public void hide()
    {
      this.hides++;
      this.shows--;
      this.group.hide();
      this.showState();
    }

    @Override public void show()
    {
      this.shows++;
      this.hides--;
      this.group.show();
      this.showState();
    }

    @SuppressWarnings("boxing") private void showState()
    {
      System.out.printf("Car: shows %d hides %d\n", this.shows, this.hides);
    }
  }

  static final class PlaneControls implements IHideable
  {
    private final RowGroup group;
    private int            hides;
    private int            shows;

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

    @Override public void forceShow()
    {
      this.hides = 0;
      this.shows = 0;
      this.group.forceShow();
    }

    @Override public void hide()
    {
      this.hides++;
      this.shows--;
      this.group.hide();
      this.showState();
    }

    @Override public void show()
    {
      this.shows++;
      this.hides--;
      this.group.show();
      this.showState();
    }

    @SuppressWarnings("boxing") private void showState()
    {
      System.out.printf("Plane: shows %d hides %d\n", this.shows, this.hides);
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
        new SelectorExampleExtended0(layout);
        frame.setPreferredSize(new Dimension(640, 480));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      }
    });
  }

  private final BikeControls  bike_controls;
  private final JCheckBox     enabled;
  private final PlaneControls plane_controls;
  private Type                selected_last;
  private final TypeSelector  selector;
  private final CarControls   car_controls;

  public SelectorExampleExtended0(
    final DesignGridLayout layout)
  {
    this.bike_controls = new BikeControls();
    this.plane_controls = new PlaneControls();
    this.car_controls = new CarControls();

    this.enabled = new JCheckBox("Enabled");
    this.enabled.setSelected(false);
    this.selector = new TypeSelector();
    this.selector.setEnabled(false);
    this.selected_last = null;

    this.enabled.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final ActionEvent e)
      {
        if (SelectorExampleExtended0.this.enabled.isSelected()) {
          SelectorExampleExtended0.this.selector.setEnabled(true);
          SelectorExampleExtended0.this.selector
            .setSelectedItem(SelectorExampleExtended0.this.selector
              .getSelectedItem());
        } else {
          SelectorExampleExtended0.this.selector.setEnabled(false);
          SelectorExampleExtended0.this.bike_controls.hide();
          SelectorExampleExtended0.this.plane_controls.hide();
          SelectorExampleExtended0.this.car_controls.hide();
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
          (Type) SelectorExampleExtended0.this.selector.getSelectedItem();
        System.out.println("Selected: " + selected);

        switch (selected) {
          case BIKE:
          {
            SelectorExampleExtended0.this.hidePrevious();
            SelectorExampleExtended0.this.bike_controls.show();
            break;
          }
          case CAR:
          {
            SelectorExampleExtended0.this.hidePrevious();
            SelectorExampleExtended0.this.car_controls.show();
            break;
          }
          case PLANE:
          {
            SelectorExampleExtended0.this.hidePrevious();
            SelectorExampleExtended0.this.plane_controls.show();
            break;
          }
        }

        SelectorExampleExtended0.this.selected_last = selected;
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

  protected void hidePrevious()
  {
    if (this.selected_last != null) {
      switch (this.selected_last) {
        case BIKE:
        {
          this.bike_controls.hide();
          break;
        }
        case CAR:
        {
          this.car_controls.hide();
          break;
        }
        case PLANE:
        {
          this.plane_controls.hide();
          break;
        }
      }
    }
  }
}
