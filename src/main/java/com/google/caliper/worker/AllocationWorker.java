/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.caliper.worker;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.caliper.Benchmark;
import com.google.caliper.model.Measurement;
import com.google.caliper.model.Value;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;

/**
 * The {@link Worker} for the {@code AllocationInstrument}.  This class invokes the benchmark method
 * a few times, with varying numbers of reps, and computes the number of object allocations and the
 * total size of those allocations.
 */
public final class AllocationWorker implements Worker {
  private static final int MAX_BASELINE_REPS = 5;
  private static final int MAX_REPS_ABOVE_BASELINE = 100;

  private final Random random;

  private int allocationCount;
  private long allocationSize;
  private boolean recordAllocations = false;

  @Inject AllocationWorker(Random random) {
    this.random = random;
    AllocationRecorder.addSampler(
        new Sampler() {
          @Override
          public void sampleAllocation(int arrayCount, String desc, Object newObj, long size) {
            if (recordAllocations) {
              allocationCount++;
              allocationSize += size;
            }
          }
        });
  }

  @Override public synchronized void measure(Benchmark benchmark, String methodName,
      Map<String, String> options, WorkerEventLog log) throws Exception {
    // do one initial measurement and throw away its results
    log.notifyWarmupPhaseStarting();
    measureAllocations(benchmark, methodName, 1);

    log.notifyMeasurementPhaseStarting();
    while (true) {
      log.notifyMeasurementStarting();
      // [1, 5]
      int baselineReps = random.nextInt(MAX_BASELINE_REPS) + 1;
      AllocationStats baseline = measureAllocations(benchmark, methodName, baselineReps);
      // (baseline, baseline + MAX_REPS_ABOVE_BASELINE]
      int measurementReps = baselineReps + random.nextInt(MAX_REPS_ABOVE_BASELINE) + 1;
      AllocationStats measurement = measureAllocations(benchmark, methodName, measurementReps);
      try {
        AllocationStats diff = diffMeasurements(baseline, measurement);
        log.notifyMeasurementEnding(ImmutableList.of(
            new Measurement.Builder()
                .value(Value.create(diff.allocationCount, ""))
                .description("objects")
                .weight(diff.reps)
                .build(),
            new Measurement.Builder()
                .value(Value.create(diff.allocationSize, "B"))
                .weight(diff.reps)
                .description("bytes")
                .build()));
      } catch (IllegalStateException e) {
        // log the failure, but just keep trying to measure
        log.notifyMeasurementFailure(e);
      }
    }
  }

  /**
   * Computes and returns the difference between these two measurements. The {@code initial}
   * measurement must have a lower weight (fewer reps) than the {@code second} measurement.
   */
  private AllocationStats diffMeasurements(AllocationStats baseline, AllocationStats measurement) {
    try {
      return new AllocationStats(measurement.allocationCount - baseline.allocationCount,
          measurement.allocationSize - baseline.allocationSize,
          measurement.reps - baseline.reps);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException(String.format(
          "The difference between the baseline (%s) and the measurement (%s) is invalid",
              baseline, measurement), e);
    }
  }

  private synchronized AllocationStats measureAllocations(
      Benchmark benchmark, String methodName, int reps) throws Exception {
    Method method = benchmark.getClass().getDeclaredMethod("time" + methodName, int.class);
    clearAccumulatedStats();
    // do the Integer boxing and the creation of the Object[] outside of the record block, so that
    // our internal allocations aren't counted in the benchmark's allocations.
    Object[] args = {reps};
    recordAllocations = true;
    method.invoke(benchmark, args);
    recordAllocations = false;

    return new AllocationStats(allocationCount, allocationSize, reps);
  }

  private synchronized void clearAccumulatedStats() {
    recordAllocations = false;
    allocationCount = 0;
    allocationSize = 0;
  }

  static class AllocationStats {
    final int allocationCount;
    final long allocationSize;
    final int reps;

    AllocationStats(int allocationCount, long allocationSize, int reps) {
      checkArgument(allocationCount >= 0, "allocationCount (%s) was negative", allocationCount);
      this.allocationCount = allocationCount;
      checkArgument(allocationSize >= 0, "allocationSize (%s) was negative", allocationSize);
      this.allocationSize = allocationSize;
      checkArgument(reps >= 0, "reps (%s) was negative", reps);
      this.reps = reps;
    }

    @Override public String toString() {
      return Objects.toStringHelper(this)
          .add("allocationCount", allocationCount)
          .add("allocationSize", allocationSize)
          .add("reps", reps)
          .toString();
    }
  }
}
