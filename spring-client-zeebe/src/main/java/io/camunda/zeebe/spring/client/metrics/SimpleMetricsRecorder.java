package io.camunda.zeebe.spring.client.metrics;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Super simple class to record metrics in memory.
 * Typically used for test cases
 */
public class SimpleMetricsRecorder implements MetricsRecorder {

  public HashMap<String, AtomicLong> counters = new HashMap<>();

  public HashMap<String, Long> timers = new HashMap<>();

  @Override
  public void increase(String metricName, String action, String type) {
    String key = key(metricName, action, type);
    if (!counters.containsKey(key)) {
      counters.put(key, new AtomicLong(1));
    } else {
      counters.get(key).incrementAndGet();
    }
  }

  @Override
  public void executeWithTimer(String metricName, Runnable methodToExecute) {
    long startTime = System.currentTimeMillis();
    methodToExecute.run();
    timers.put(metricName, System.currentTimeMillis() - startTime);
  }

  private String key(String metricName, String action, String type) {
    String key = metricName + "#" + action + "#" + type;
    return key;
  }

  public long getCount(String metricName, String action, String type) {
    if (!counters.containsKey(key(metricName, action, type))) {
      return 0;
    }
    return counters.get(key(metricName, action, type)).get();
  }

  public long getTimer(String metricName) {
    if (!timers.containsKey(metricName)) {
      return 0;
    }
    return counters.get(metricName).get();
  }
}