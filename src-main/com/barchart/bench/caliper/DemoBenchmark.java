package com.barchart.bench.caliper;

import com.google.caliper.Param;
import com.google.caliper.api.Benchmark;
import com.google.caliper.api.SkipThisScenarioException;
import com.google.caliper.api.VmOptions;
import com.google.caliper.runner.CaliperMain;
import com.google.caliper.util.CommandLineParser.Option;
import com.google.caliper.util.ShortDuration;

@VmOptions("-server")
public class DemoBenchmark extends Benchmark {

	@Param({ "abc", "def" })
	String string;

	@Param({ "1", "2" })
	int number;

	@Param
	Foo foo;

	@Param({ "1ns", "2 minutes" })
	ShortDuration duration;

	enum Foo {
		FOO, BAR
	}

	DemoBenchmark() {
		 System.out.println("I like doing this.");
	}

	@Override
	protected void setUp() throws Exception {
		System.out.println("Hey, I'm setting up the joint.");
		if (string.equals("abc") && number == 1) {
			throw new SkipThisScenarioException();
		}
	}

	public int timeMain(final int reps) {
		System.out.println("reps=" + reps);
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

	public static void main(final String[] args) {
		CaliperMain.main(DemoBenchmark.class, new String[] { //
			"--output", "./target/caliper-results/"//
				//
				});
	}

}
