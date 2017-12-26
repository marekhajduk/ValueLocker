package com.codewise.lock;

import static java.util.concurrent.TimeUnit.*;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ReentryLockWeakReferenceRemovedTest {
	private ReentryLocker locker = new ReentryLocker();

	@Test
	public void unusedLockObjectRemovedFromServicetest() throws InterruptedException {

		IntStream.rangeClosed(1, 10).forEach(x -> {
			locker.lock(new Object()).release();
		});

		Assertions.assertThat(locker.lockerSize()).isEqualTo(10);

		IntStream.rangeClosed(1, 3).forEach(x -> {
			System.gc();
			try {
				MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
			}
		});
		Assertions.assertThat(locker.lockerSize()).isEqualTo(0);
	}
}
