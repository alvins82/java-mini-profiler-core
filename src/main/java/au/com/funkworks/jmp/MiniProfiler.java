/**
 * Copyright (C) 2011 by Jim Riecken
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package au.com.funkworks.jmp;


/**
 * Simple step instrumentation that can be used to profile Java code in the
 * context of the {@link MiniProfilerFilter}.
 * <p>
 * To start a step, use the {@link #step(String)} method. To finish a step, call
 * the {@code close} method on the {@link Step} object returned.
 * <p>
 * Typically you will do this in a {@code try/finally} block like:
 * 
 * <pre>
 * Step s = MiniProfiler.step(&quot;My Step&quot;);
 * try {
 * 	// Do Stuff
 * } finally {
 * 	s.close();
 * }
 * </pre>
 * 
 * Steps can be nested (e.g. starting a step while inside another step)
 * <p>
 * Profiling data is stored in a {@link ThreadLocal}.
 */
public class MiniProfiler {

	/** Thread local that contains the profiling data for the current thread */
	private static final ThreadLocal<Root> PROFILER_STEPS = new ThreadLocal<Root>();

	/**
	 * Start the profiler.
	 */
	protected static void start() {
		PROFILER_STEPS.set(new Root());
	}

	/**
	 * Stop the profiler.
	 * <p>
	 * This should be called in a {@code finally} block to avoid leaving data on
	 * the thread.
	 * 
	 * @return The profiling data.
	 */
	protected static Profile stop() {
		Root result = PROFILER_STEPS.get();
		PROFILER_STEPS.remove();
		return result != null ? result.popData() : null;
	}

	/**
	 * Start a profiling step.
	 * 
	 * @param stepName
	 *            The name of the step.
	 * @return A {@code Step} object whose {@link Step#close()} method should be
	 *         called to finish the step.
	 */
	public static Step step(String stepName) {
		return step(stepName, null);
	}
	
	public static Step step(String stepName, String tag) {
		Root root = PROFILER_STEPS.get();
		if (root != null) {
			Profile data = new Profile(root.nextId(), stepName, tag);
			return new Step(root, data);
		} else {
			return new Step(null, null);
		}
	}
}
