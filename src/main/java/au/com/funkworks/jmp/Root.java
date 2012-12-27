package au.com.funkworks.jmp;

import java.io.Serializable;
import java.util.Stack;

/**
 * The root of the profiling data.
 * <p>
 * This is what's stored in the {@code ThreadLocal}.
 */
public class Root implements Serializable {
	
	private static final long serialVersionUID = -7244418353632893875L;

	/** Counter used for generating ids */
	private int count = 0;

	/** The root step of the profile */
	private Profile root = new Profile(count++, "Request", null);

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
