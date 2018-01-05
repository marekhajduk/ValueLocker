package com.codewise.lock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReentryLockerTest {
	@Parameter(0)
	public Object counter;
	@Parameter(1)
	public Object hashEqualsSupport;
	@Parameter(2)
	public Object lockFairness;
    private ReentryLocker reentryLocker;
    private int resource = 0;

    @Before
    public void setUp() {
        reentryLocker = new ReentryLocker((Boolean) lockFairness, (Boolean) hashEqualsSupport);
    }

	@Parameters(name = "{index}-THREAD NUM: {0} - HASH/EQUALS Support: {1}, FAIRNESS: {2}")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { Integer.valueOf(2000), false, false },
				{ Integer.valueOf(2000), true, false },
				{ Integer.valueOf(2000), false, true },
				{ Integer.valueOf(2000), true, true } };
		return Arrays.asList(data);
	}
    
    @Test 
    public void shouldSynchronizeAccess() throws InterruptedException {
        // given
        CountDownLatch latch = new CountDownLatch(1);
        List<Thread> threads = Stream.iterate(0, i -> i + 1)
                .limit((int)counter)
                .map(i -> new Thread(() -> {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int j = 0; j < (int)counter; j++) {
                        Mutex section = reentryLocker.lock(new String("a"));
                        resource = resource +1;
                        section.release();
                    }
                }))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);

        // when
        latch.countDown();

        // then
        for (Thread thread : threads) {
            thread.join();
        }
        
        Assertions.assertThat(resource).isEqualTo((int)counter * (int)counter);
    }
}
