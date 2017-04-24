package com.aionemu.gameserver.taskmanager.parallel;

import java.util.Collection;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Rolandas
 * @modified Neon
 */
public final class ForEach<E> extends CountedCompleter<E> {

	private static final Logger log = LoggerFactory.getLogger(ForEach.class);
	private static final long serialVersionUID = 7902148320917998146L;

	/**
	 * Creates a task that executes an operation for each element in the collection asynchronously (after invocation). Utilizes Fork/Join framework to
	 * speed up processing, by using a divide/conquer algorithm
	 * 
	 * @param list
	 *          - element list to loop
	 * @param operation
	 *          - operation to perform on each element
	 */
	public static <E> ForkJoinTask<E> newTask(Collection<E> list, Consumer<E> operation) {
		if (list.size() > 0) {
			@SuppressWarnings("unchecked")
			E[] objects = list.toArray((E[]) new Object[list.size()]);
			return newTask(operation, objects);
		}
		return null;
	}

	/**
	 * See {@link #newTask(Collection, Consumer) newTask(Collection&lt;E&gt; list, Consumer&lt;E&gt; operation)}
	 */
	@SuppressWarnings("unchecked")
	@SuppressFBWarnings(value = "NP_NULL_PARAM_DEREF_NONVIRTUAL",
		justification = "FindBugs somehow thinks CountedCompleter would't accept a null rootTask")
	public static <E> ForkJoinTask<E> newTask(Consumer<E> operation, E... list) {
		if (list.length > 0) {
			return new ForEach<>(null, operation, 0, list.length, list);
		}
		return null;
	}

	final E[] list;
	final Consumer<E> operation;
	final int lo, hi;

	private ForEach(CountedCompleter<E> rootTask, Consumer<E> operation, int lo, int hi, E[] list) {
		super(rootTask);
		this.list = list;
		this.operation = operation;
		this.lo = lo;
		this.hi = hi;
	}

	@Override
	public void compute() {
		int l = lo, h = hi;
		while (h - l >= 2) {
			int mid = (l + h) >>> 1;
			addToPendingCount(1);
			new ForEach<>(this, operation, mid, h, list).fork(); // right child
			h = mid;
		}
		if (h > l) {
			try {
				operation.accept(list[l]);
			} catch (Throwable ex) {
				// we want to complete without an exception re-thrown
				// otherwise, should call completeExceptionally(ex);
				onExceptionalCompletion(ex, this);
			}
		}
		propagateCompletion();

	}

	@Override
	public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
		log.warn("", ex);
		// returning false would result in infinite wait when calling join();
		return true;
	}

}
