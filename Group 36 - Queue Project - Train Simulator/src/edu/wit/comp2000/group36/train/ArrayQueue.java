package edu.wit.comp2000.group36.train;

import com.pearson.carrano.EmptyQueueException;
import com.pearson.carrano.QueueInterface;

public final class ArrayQueue<T> implements QueueInterface<T> {
	private T[] queue;
	private int frontIndex;
	private int backIndex;
	private boolean initialized = false;

	public ArrayQueue() {
		this(3);
	}

	@SuppressWarnings("unchecked")
	public ArrayQueue(int initialCapacity) {
		checkCapacity(initialCapacity);

		Object[] tempQueue = new Object[initialCapacity + 1];
		queue = (T[]) tempQueue;
		frontIndex = 0;
		backIndex = initialCapacity;
		initialized = true;
	}

	public void enqueue(T newEntry) {
		checkInitialization();
		ensureCapacity();
		backIndex = ((backIndex + 1) % queue.length);
		queue[backIndex] = newEntry;
	}

	public T getFront() {
		checkInitialization();
		if(isEmpty()) {
			throw new EmptyQueueException();
		}
		return queue[frontIndex];
	}

	public T dequeue() {
		checkInitialization();
		if(isEmpty()) {
			throw new EmptyQueueException();
		}

		T front = queue[frontIndex];
		queue[frontIndex] = null;
		frontIndex = ((frontIndex + 1) % queue.length);
		return front;
	}

	public boolean isEmpty() {
		return frontIndex == (backIndex + 1) % queue.length;
	}

	public void clear() {
		checkInitialization();
		if(!isEmpty()) {
			for(int index = frontIndex; index != backIndex; index = (index + 1) % queue.length) {
				queue[index] = null;
			}

			queue[backIndex] = null;
		}

		frontIndex = 0;
		backIndex = (queue.length - 1);
	}

	private void checkInitialization() {
		if(!initialized) {
			throw new SecurityException("ArrayQueue object is not initialized properly.");
		}
	}

	private void checkCapacity(int capacity) {
		if(capacity > 10000) {
			throw new IllegalStateException("Attempt to create a queue whose capacity exceeds allowed maximum.");
		}
	}

	@SuppressWarnings("unchecked")
	private void ensureCapacity() {
		if(frontIndex == (backIndex + 2) % queue.length) {
//			System.out.println("Doubling Array Size");
			Object[] oldQueue = queue;
			int oldSize = oldQueue.length;
			int newSize = 2 * oldSize;
			checkCapacity(newSize - 1);

			Object[] tempQueue = new Object[newSize];
			queue = (T[]) tempQueue;

			for(int index = 0; index < oldSize - 1; index++) {
				queue[index] = (T) oldQueue[frontIndex];
//				System.out.println("queue[" + index + "] = " + oldQueue[frontIndex]);
				frontIndex = ((frontIndex + 1) % oldSize);
			}

			frontIndex = 0;
			backIndex = (oldSize - 2);
//			System.out.println("End ensureCapacity(): newSize = " + newSize);
		}
	}
}
