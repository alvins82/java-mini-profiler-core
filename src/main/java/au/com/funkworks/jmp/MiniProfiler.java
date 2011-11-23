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

import java.io.Closeable;
import java.io.Serializable;
import java.util.*;

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
	/**
	 * Contains information about a profiling step
	 */
	protected static class Profile implements Serializable {
		private static final long serialVersionUID = 6373761607106996570L;

		/**
		 * The id of the step. This is used in the UI to uniquely identify steps
		 * in-order.
		 */
		private int id;

		/**
		 * The depth of the step in the profiling tree. This is used in the UI
		 * to determine indentation.
		 */
		private int depth;

		/** The name of the step. */
		private String name;

		/** When the step started (nanoseconds) */
		private long start;

		/** How long the step was (in nanoseconds) */
		private long duration;

		/**
		 * How far from the start of profiling did this step start (in
		 * nanoseconds)
		 */
		private long offset;

		/** The child steps of this step */
		private List<Profile> children = new ArrayList<Profile>();

		public Profile(int id, String name) {
			this.id = id;
			this.name = name;
		}

		/**
		 * The id of the step. This is used in the UI to uniquely identify steps
		 * in-order.
		 * 
		 * @return The id of the step.
		 */
		public long getId() {
			return id;
		}

		/**
		 * Get the depth of the step in the profiling tree. This is used in the
		 * UI to determine indentation.
		 * 
		 * @return The depth.
		 */
		public int getDepth() {
			return depth;
		}

		/**
		 * Set the depth of the step in the profiling tree.
		 * 
		 * @param depth
		 *            The depth.
		 */
		public void setDepth(int depth) {
			this.depth = depth;
		}

		/**
		 * Get the name of the step.
		 * 
		 * @return The name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Get when the step started (nanoseconds).
		 * 
		 * @return When the step started.
		 */
		public long getStart() {
			return start;
		}

		/**
		 * Set when the step started (nanoseconds).
		 * 
		 * @param start
		 *            When the step started.
		 */
		public void setStart(long start) {
			this.start = start;
		}

		/**
		 * Set how long the step took (nanoseconds).
		 * 
		 * @param duration
		 *            The duration.
		 */
		public void setDuration(long duration) {
			this.duration = duration;
		}

		/**
		 * Get how long the step tool (nanoseconds).
		 * 
		 * @return The duration.
		 */
		public long getDuration() {
			return duration;
		}

		/**
		 * Set the step's offset from the start of profiling (nanoseconds).
		 * 
		 * @param offset
		 *            The offset.
		 */
		public void setOffset(long offset) {
			this.offset = offset;
		}

		/**
		 * Get the step's offset from the start of profling (nanoseconds).
		 * 
		 * @return The offset.
		 */
		public long getOffset() {
			return offset;
		}

		/**
		 * Calculate the duration of this step, minus the duration of all the
		 * child steps.
		 * <p>
		 * In effect, returns the time spent <i>only</i> in this step.
		 * 
		 * @return The time spent just in this step (none of the children)
		 */
		public long getSelf() {
			long result = duration;
			for (Profile p : children) {
				result -= p.duration;
			}
			return result;
		}

		/**
		 * Get the child steps of this step.
		 * 
		 * @return The child steps.
		 */
		public List<Profile> getChildren() {
			return children;
		}

		/**
		 * Add a child step to this step.
		 * 
		 * @param child
		 *            The child to add.
		 */
		public void addChild(Profile child) {
			children.add(child);
		}
	}

	/**
	 * The root of the profiling data.
	 * <p>
	 * This is what's stored in the {@code ThreadLocal}.
	 */
	private static class Root implements Serializable {
		private static final long serialVersionUID = -7244418353632893875L;

		/** Counter used for generating ids */
		private int count = 0;

		/** The root step of the profile */
		private Profile root = new Profile(count++, "Request");

		/**
		 * The stack of steps (the top of the stack will always be the current
		 * step)
		 */
		private Stack<Profile> stack = new Stack<Profile>();

		/**
		 * Create the root of the profile - records the start time.
		 */
		public Root() {
			root.setStart(System.nanoTime());
			stack.push(root);
		}

		/**
		 * Generate the id for the next step.
		 * 
		 * @return The next id.
		 */
		public int nextId() {
			return count++;
		}

		/**
		 * Add a profile step to the stack.
		 * 
		 * @param d
		 *            The profile step to add
		 */
		public void pushData(Profile d) {
			long now = System.nanoTime();
			d.setDepth(stack.size());
			d.setStart(now);
			d.setOffset(now - root.getStart());
			stack.peek().addChild(d);
			stack.push(d);
		}

		/**
		 * Remove the top profile step from the stack.
		 * 
		 * @return The top profile step.
		 */
		public Profile popData() {
			long now = System.nanoTime();
			Profile d = stack.pop();
			d.setDuration(now - d.getStart());
			return d;
		}
	}

	/**
	 * Used to control the starting and stopping of profiling steps.
	 * <p>
	 * Implements {@code Closeable}, so can theoretically be used in a Java 7
	 * {@code try-with-resources} statement for less code.
	 */
	public static class Step implements Closeable {
		private Root root;

		/**
		 * Create a step object.
		 * 
		 * @param root
		 *            The profile root.
		 * @param data
		 *            The current step data.
		 */
		public Step(Root root, Profile data) {
			this.root = root;
			if (root != null) {
				root.pushData(data);
			}
		}

		/**
		 * Stop the profiling step.
		 */
		public void close() {
			if (root != null) {
				root.popData();
			}
		}
	}

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
		Root root = PROFILER_STEPS.get();
		if (root != null) {
			Profile data = new Profile(root.nextId(), stepName);
			return new Step(root, data);
		} else {
			return new Step(null, null);
		}
	}
}
