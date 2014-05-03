package utils.async;

public class Timeout {

	private final long endTime;

	public Timeout(final long duration) {
		this.endTime = System.currentTimeMillis() + duration;
	}

	public boolean hasTimedOut() {
		return timeRemaining() <= 0;
	}

	public void waitOn(final Object lock) throws InterruptedException {
		final long waitTime = timeRemaining();
		if (waitTime > 0) {
			lock.wait(waitTime);
		}
	}

	private long timeRemaining() {
		return endTime - System.currentTimeMillis();
	}

}
