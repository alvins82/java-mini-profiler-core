package au.com.funkworks.jmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a profiling step
 */
public class Profile implements Serializable {
	
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
	
	/**
	 * Used to collate date of same kind
	 */
	private String tag;

	/** The child steps of this step */
	private List<Profile> children = new ArrayList<Profile>();

	public Profile(int id, String name, String tag) {
		this.id = id;
		this.name = name;
		this.tag = tag;
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

	public String getTag() {
		return tag;
	}

}
