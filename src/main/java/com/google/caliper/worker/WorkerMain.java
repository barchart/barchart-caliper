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

import static com.google.inject.Stage.PRODUCTION;

import com.google.caliper.Benchmark;
import com.google.caliper.bridge.BridgeModule;
import com.google.caliper.bridge.WorkerSpec;
import com.google.caliper.json.GsonModule;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.lang.reflect.Method;

/**
 * This class is invoked as a subprocess by the Caliper runner parent process; it re-stages
 * the benchmark and hands it off to the instrument's worker.
 */
public final class WorkerMain {
  private WorkerMain() {}

  public static void main(String[] args) throws Exception {
    Injector gsonInjector = Guice.createInjector(PRODUCTION, new GsonModule());
    WorkerSpec request =
        gsonInjector.getInstance(Gson.class).fromJson(args[0], WorkerSpec.class);

    Injector workerInjector = gsonInjector.createChildInjector(new WorkerModule(request),
        new BridgeModule());

    Worker worker = workerInjector.getInstance(Worker.class);
    Benchmark benchmark = workerInjector.getInstance(Benchmark.class);
    WorkerEventLog log = workerInjector.getInstance(WorkerEventLog.class);

    log.notifyWorkerStarted();

    try {
      runSetUp(benchmark);

      worker.measure(benchmark, request.benchmarkSpec.methodName(), request.workerOptions, log);
    } catch (Exception e) {
      log.notifyFailure(e);
    } finally {
      System.out.flush(); // ?
      runTearDown(benchmark);
    }
  }

  private static void runSetUp(Benchmark benchmark) throws Exception {
    runBenchmarkMethod(benchmark, "setUp");
  }

  private static void runTearDown(Benchmark benchmark) throws Exception {
    runBenchmarkMethod(benchmark, "tearDown");
  }

  private static void runBenchmarkMethod(Benchmark benchmark, String methodName) throws Exception {
    // benchmark.setUp() or .tearDown() -- but they're protected
    Method method = Benchmark.class.getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(benchmark);
  }
}
