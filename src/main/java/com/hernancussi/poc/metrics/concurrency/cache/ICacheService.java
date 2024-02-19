package com.hernancussi.poc.metrics.concurrency.cache;

public interface ICacheService<V> {

  default V get(String key) {
    return null;
  };

  default boolean set(String key, V value) {
    return false;
  }

  default String getName() {
    return "NONE";
  };

  default String getNamespace() { return null; };

  default void removeAll() {};

  default Long getKeyCount() { return null; };

}
