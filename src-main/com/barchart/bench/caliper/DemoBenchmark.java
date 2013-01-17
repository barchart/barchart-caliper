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

package com.barchart.bench.caliper;

import com.google.caliper.Param;
import com.google.caliper.api.Benchmark;
import com.google.caliper.api.SkipThisScenarioException;
import com.google.caliper.api.VmOptions;
import com.google.caliper.runner.CaliperMain;
import com.google.caliper.util.ShortDuration;

import java.math.BigDecimal;

@VmOptions("-server")
public class DemoBenchmark extends Benchmark {

	@Param({ "abc", "def" })
	String string;
	
	@Param({ "1", "2" })
	int number;

	@Param({ "1 ms", "2 m" })
	ShortDuration duration;

	DemoBenchmark() {
		System.out.println("I should not do this.");
	}

	@Override
	protected void setUp() throws Exception {
		System.out.println("Hey, I'm setting up the joint.");
		if (string.equals("abc") && number == 1) {
			throw new SkipThisScenarioException();
		}
	}

	public int timeMain(int reps) {
		int dummy = 0;
		for (int i = 0; i < reps; i++) {
			dummy += i;
		}
		return dummy;
	}

	@Override
	protected void tearDown() throws Exception {
		System.out.println("Hey, I'm tearing up the joint.");
	}

	public static void main(String[] a) {
		String[] args = new String[]{"--verbose"}; 
		CaliperMain.main(DemoBenchmark.class, args);
	}
	
}