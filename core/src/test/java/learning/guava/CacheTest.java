package learning.guava;

import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheTest {

	private static final String FOO = "foo";
	private static int EXPIRATION_TIME_IN_MILLISECONDS = 1000;
	private static Long KEY = 10L;

	@Test
	public void temporizedCacheShouldDeleteEntriesAfterTimeout() throws Exception {
		// given
		final Cache<Long, String> cache = CacheBuilder.newBuilder() //
				.expireAfterWrite(EXPIRATION_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS) //
				.build();
		cache.put(KEY, FOO);
		Thread.sleep(EXPIRATION_TIME_IN_MILLISECONDS + 200);

		// when
		final String value = cache.getIfPresent(KEY);

		// then
		assertNull(value);
	}

	@Test
	public void shouldReturnNoElementAfterInvalidation() throws Exception {
		// given
		final Cache<Long, String> cache = CacheBuilder.newBuilder() //
				.expireAfterWrite(EXPIRATION_TIME_IN_MILLISECONDS * 3, TimeUnit.MILLISECONDS) //
				.build();
		cache.put(KEY, FOO);

		// when
		cache.invalidate(KEY);
		final String value = cache.getIfPresent(KEY);

		// then
		assertNull(value);
	}

}
