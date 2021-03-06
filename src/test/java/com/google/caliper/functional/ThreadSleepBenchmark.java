/**
 * Copyright (C) 2009 Google Inc.
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

package com.google.caliper.functional;

import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;

/**
 * If everything is working properly, this should report runtime very close to
 * 1ms.
 */
public class ThreadSleepBenchmark extends Benchmark {

  public void timeSleep(int reps) {
    try {
      Thread.sleep(reps);
    } catch (InterruptedException ignored) {
    }
  }

  public static void main(String[] args) throws Exception {
    CaliperMain.main(ThreadSleepBenchmark.class, args);
  }
}
