package au.com.funkworks.jmp;

import java.io.Closeable;

/**
 * Used to control the starting and stopping of profiling steps.
 * <p>
 * Implements {@code Closeable}, so can theoretically be used in a Java 7
 * {@code try-with-resources} statement for less code.
 */
public class Step implements Closeable {
	
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
