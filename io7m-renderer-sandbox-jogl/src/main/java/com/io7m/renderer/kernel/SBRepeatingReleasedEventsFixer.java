package com.io7m.renderer.kernel;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Timer;

/**
 * This {@link AWTEventListener} tries to work around a 12 yo bug in the Linux
 * KeyEvent handling for keyboard repeat. Linux apparently implements
 * repeating keypresses by repeating both the {@link KeyEvent#KEY_PRESSED} and
 * {@link KeyEvent#KEY_RELEASED}, while on Windows, one only gets repeating
 * PRESSES, and then a final RELEASE when the key is released. The Windows way
 * is obviously much more useful, as one then can easily distinguish between a
 * user holding a key pressed, and a user hammering away on the key.
 * 
 * This class is an {@link AWTEventListener} that should be installed as the
 * application's first ever {@link AWTEventListener} using the following code,
 * but it is simpler to invoke {@link #install() install(new instance)}:
 * 
 * Toolkit.getDefaultToolkit().addAWTEventListener(new
 * {@link SBRepeatingReleasedEventsFixer}, AWTEvent.KEY_EVENT_MASK);
 * 
 * Remember to remove it and any other installed {@link AWTEventListener} if
 * your application have some "reboot" functionality that can potentially
 * install it again - or else you'll end up with multiple instances, which
 * isn't too hot.
 * 
 * Notice: Read up on the {@link Reposted} interface if you have other
 * AWTEventListeners that resends KeyEvents (as this one does) - or else we'll
 * get the event back.
 * 
 * Mode of operation
 * 
 * The class makes use of the fact that the subsequent PRESSED event comes
 * right after the RELEASED event - one thus have a sequence like this:
 * 
 * PRESSED -wait between key repeats- RELEASED PRESSED -wait between key
 * repeats- RELEASED PRESSED etc.
 * 
 * A timer is started when receiving a RELEASED event, and if a PRESSED comes
 * soon afterwards, the RELEASED is dropped (consumed) - while if the timer
 * times out, the event is reposted and thus becomes the final, wanted
 * RELEASED that denotes that the key actually was released.
 * 
 * Inspired by http://www.arco.in-berlin.de/keyevent.html
 * 
 * @author Endre Stølsvik
 */

public class SBRepeatingReleasedEventsFixer implements AWTEventListener
{
  final Map<Integer, ReleasedAction> _map =
                                            new HashMap<Integer, ReleasedAction>();

  public void install()
  {
    Toolkit.getDefaultToolkit().addAWTEventListener(
      this,
      AWTEvent.KEY_EVENT_MASK);
  }

  public void remove()
  {
    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
  }

  @Override public void eventDispatched(
    final AWTEvent event)
  {
    assert event instanceof KeyEvent : "Shall only listen to KeyEvents, so no other events shall come here";
    assert SBRepeatingReleasedEventsFixer.assertEDT(); // REMEMBER THAT THIS IS
                                                     // SINGLE THREADED, so no
                                                     // need for synch.

    // ?: Is this one of our synthetic RELEASED events?
    if (event instanceof Reposted) {
      // -> Yes, so we shalln't process it again.
      return;
    }

    // ?: KEY_TYPED event? (We're only interested in KEY_PRESSED and
    // KEY_RELEASED).
    if (event.getID() == KeyEvent.KEY_TYPED) {
      // -> Yes, TYPED, don't process.
      return;
    }

    final KeyEvent keyEvent = (KeyEvent) event;

    // ?: Is this already consumed?
    // (Note how events are passed on to all AWTEventListeners even though a
    // previous one consumed it)
    if (keyEvent.isConsumed()) {
      return;
    }

    // ?: Is this RELEASED? (the problem we're trying to fix!)
    if (keyEvent.getID() == KeyEvent.KEY_RELEASED) {
      // -> Yes, so stick in wait
      /*
       * Really just wait until "immediately", as the point is that the
       * subsequent PRESSED shall already have been posted on the event queue,
       * and shall thus be the direct next event no matter which events are
       * posted afterwards. The code with the ReleasedAction handles if the
       * Timer thread actually fires the action due to lags, by cancelling the
       * action itself upon the PRESSED.
       */
      final Timer timer = new Timer(2, null);
      final ReleasedAction action = new ReleasedAction(keyEvent, timer);
      timer.addActionListener(action);
      timer.start();

      this._map.put(Integer.valueOf(keyEvent.getKeyCode()), action);

      // Consume the original
      keyEvent.consume();
    } else if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {
      // Remember that this is single threaded (EDT), so we can't have races.
      final ReleasedAction action =
        this._map.remove(Integer.valueOf(keyEvent.getKeyCode()));
      // ?: Do we have a corresponding RELEASED waiting?
      if (action != null) {
        // -> Yes, so dump it
        action.cancel();
      }
      // System.out.println("PRESSED: [" + keyEvent + "]");
    } else {
      throw new AssertionError("All IDs should be covered.");
    }
  }

  /**
   * The ActionListener that posts the RELEASED {@link RepostedKeyEvent} if
   * the {@link Timer} times out (and hence the repeat-action was over).
   */

  private class ReleasedAction implements ActionListener
  {

    private final KeyEvent _originalKeyEvent;
    private Timer          _timer;

    ReleasedAction(
      final KeyEvent originalReleased,
      final Timer timer)
    {
      this._timer = timer;
      this._originalKeyEvent = originalReleased;
    }

    void cancel()
    {
      assert SBRepeatingReleasedEventsFixer.assertEDT();
      this._timer.stop();
      this._timer = null;
      SBRepeatingReleasedEventsFixer.this._map.remove(Integer
        .valueOf(this._originalKeyEvent.getKeyCode()));
    }

    @Override public void actionPerformed(
      final ActionEvent e)
    {
      assert SBRepeatingReleasedEventsFixer.assertEDT();
      // ?: Are we already cancelled?
      // (Judging by Timer and TimerQueue code, we can theoretically be raced
      // to
      // be posted onto EDT by TimerQueue,
      // due to some lag, unfair scheduling)
      if (this._timer == null) {
        // -> Yes, so don't post the new RELEASED event.
        return;
      }
      // Stop Timer and clean.
      this.cancel();
      // Creating new KeyEvent (we've consumed the original).
      final KeyEvent newEvent =
        new RepostedKeyEvent(
          (Component) this._originalKeyEvent.getSource(),
          this._originalKeyEvent.getID(),
          this._originalKeyEvent.getWhen(),
          this._originalKeyEvent.getModifiers(),
          this._originalKeyEvent.getKeyCode(),
          this._originalKeyEvent.getKeyChar(),
          this._originalKeyEvent.getKeyLocation());
      // Posting to EventQueue.
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(newEvent);
      // System.out.println("Posted synthetic RELEASED [" + newEvent + "].");
    }
  }

  /**
   * Marker interface that denotes that the {@link KeyEvent} in question is
   * reposted from some {@link AWTEventListener}, including this. It denotes
   * that the event shall not be "hack processed" by this class again. (The
   * problem is that it is not possible to state
   * "inject this event from this point in the pipeline" - one have to inject
   * it to the event queue directly, thus it will come through this
   * {@link AWTEventListener} too.
   */

  public interface Reposted
  {
    // marker
  }

  /**
   * Dead simple extension of {@link KeyEvent} that implements
   * {@link Reposted}.
   */

  public static class RepostedKeyEvent extends KeyEvent implements Reposted
  {
    private static final long serialVersionUID = 1L;

    public RepostedKeyEvent(
      final Component source,
      final int id,
      final long when,
      final int modifiers,
      final int keyCode,
      final char keyChar,
      final int keyLocation)
    {
      super(source, id, when, modifiers, keyCode, keyChar, keyLocation);
    }
  }

  static boolean assertEDT()
  {
    if (!EventQueue.isDispatchThread()) {
      throw new AssertionError("Not EDT, but ["
        + Thread.currentThread()
        + "].");
    }
    return true;
  }
}
