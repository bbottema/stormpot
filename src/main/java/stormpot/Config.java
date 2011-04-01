package stormpot;

import java.util.concurrent.TimeUnit;

public class Config<T extends Poolable> {

  private int size = 10;
  private boolean sane = true;
  private long ttl = 10;
  private TimeUnit ttlUnit = TimeUnit.MINUTES;
  private Allocator allocator;

  public synchronized Config<T> setSize(int size) {
    if (sane && size < 1) {
      throw new IllegalArgumentException(
          "size must be at least 1 but was " + size);
    }
    this.size = size;
    return this;
  }

  public synchronized int getSize() {
    return size;
  }

  synchronized Config<T> goInsane() {
    sane = false;
    return this;
  }
  
  public synchronized Config<T> setTTL(long ttl, TimeUnit unit) {
    if (sane && unit == null) {
      throw new IllegalArgumentException("unit cannot be null");
    }
    this.ttl = ttl;
    this.ttlUnit = unit;
    return this;
  }

  public synchronized long getTTL() {
    return ttl;
  }

  public synchronized TimeUnit getTTLUnit() {
    return ttlUnit;
  }

  public synchronized <X extends Poolable> Config<X> setAllocator(
      Allocator<X> allocator) {
    this.allocator = allocator;
    return (Config<X>) this;
  }

  public synchronized Allocator<T> getAllocator() {
    return allocator;
  }

  public synchronized void setFieldsOn(Config config) {
    if (!sane) {
      config.goInsane();
    }
    config.setAllocator(allocator);
    config.setSize(size);
    config.setTTL(ttl, ttlUnit);
  }
}
