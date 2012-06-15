package stormpot.benchmark;

import java.util.concurrent.locks.LockSupport;

/**
 * The Clock improves upon {@link System#currentTimeMillis()} in that it can be
 * stopped and manually ticked forward. This is useful for testability.
 * 
 * It also turns out that a simple volatile read is significantly faster than
 * calling {@link System#currentTimeMillis()}, but the improvement in reading
 * performance comes at a cost of accuracy. Of what I've observed, the clock
 * can sometimes be off by some 10-odd milliseconds - sometimes more. It varies.
 * @author cvh
 */
public final class Clock {

  private static volatile long now;
  private static final Ticker ticker = new Ticker();
  private static final boolean precise = usePreciseTime();
  
  private Clock() { /* static utility class */ }
  
  private static boolean usePreciseTime() {
    return Boolean.valueOf(System.getProperty("clock.precise", "false"));
  }

  /**
   * Advance the clock to the current time.
   * @return The new current time, in milliseconds since the epoch.
   */
  public static long tick() {
    return now = System.currentTimeMillis();
  }

  /**
   * Advance the clock by exactly one millisecond, regardless of what the
   * actual time may be.
   * Note that this method is racy, and is technically only safe to call by
   * a single thread at a time, and when the automatic ticking of the clock
   * has been turned off. This method is only really useful for unit testing of
   * time dependent code.
   */
  public static void inc() {
    now++;
  }
  
  /**
   * Get the value of the last tick.
   * @return The "current" time recorded by the last tick.
   */
  public static long currentTimeMillis() {
    if (precise) {
      return System.currentTimeMillis();
    }
    return now;
  }
  
  /**
   * Start automatic ticking. This will tick the clock, and update its current
   * time value, about every millisecond. Note that this does not guarantee
   * that the clock will get a distinctively new time value every
   * millisecond because the underlying time source may not be that accurate.
   * The clock can be stopped again at any time with the {@link #stop()}
   * method. Starting an already started clock has no effect.
   */
  public static void start() {
    tick();
    ticker.stopFlag = false;
    if (ticker.getState() == Thread.State.NEW) {
      ticker.start();
    } else {
      LockSupport.unpark(ticker);
    }
  }
  
  /**
   * Stop automatic ticking. It can be resumed again with the {@link #start()}
   * method. Stopping an already stopped clock has no effect.
   */
  public static void stop() {
    ticker.stopFlag = true;
  }
  
  private static final class Ticker extends Thread {
    public volatile boolean stopFlag;
    
    public Ticker() {
      super("Clock-Thread");
      setDaemon(true);
    }

    @Override
    public void run() {
      for (;;) {
        if (stopFlag) {
          LockSupport.park();
        }
        tick();
//        LockSupport.parkNanos(1000);
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {}
      }
    }
  }
}
