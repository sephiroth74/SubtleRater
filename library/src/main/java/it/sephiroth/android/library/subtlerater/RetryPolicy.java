package it.sephiroth.android.library.subtlerater;

/**
 * Created by alessandro on 08/03/14.
 */
public class RetryPolicy {

	public static enum Policy {
		/**
		 * Will retry each time the initial count has been triggered
		 * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 9th, ... times
		 */
		INCREMENTAL,

		/**
		 * Will retry exponentially to be less intrusive
		 * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 12th, ... times
		 */
		EXPONENTIAL,

		/**
		 * Will never retry
		 */
		NONE
	}

	public static class Builder {

		private final Policy policy;
		private long count;
		private long retryDelay;

		/**
		 * Creates a new Builder with the given Policy
		 *
		 * @param policy
		 */
		public Builder( Policy policy ) {
			this.policy = policy;
		}

		/**
		 * After how many invocations of the {@link it.sephiroth.android.library.subtlerater.SubtleRater} user
		 * will be notified
		 *
		 * @param count The number of invocations before notification is seen
		 * @return
		 */
		public Builder launchCount( long count ) {
			this.count = count;
			return this;
		}

		/**
		 * This method works in conjunction with the {@link it.sephiroth.android.library.subtlerater.SubtleRater.Builder#hideAfter(long)}, or when
		 * the {@link SubtleRater#hide()} is called manually. <br />
		 * Basically this works like a "reming me later" and if the given ms value is > 0 then the user will be notified again
		 * only after a certain amount of time. <br />
		 * Cannot be used if the policy is set to NONE
		 *
		 * @param ms The amount of time to wait until the next notification
		 * @return
		 */
		public Builder retryDelay( long ms ) {
			this.retryDelay = ms;
			return this;
		}

		public RetryPolicy build() throws IllegalArgumentException {
			if( count < 1 ) throw new IllegalArgumentException( "launchCount must be > 1" );
			if( policy == Policy.NONE && retryDelay > 0 ) throw new IllegalArgumentException( "retryDelay cannot be > 0 if the policy is NONE" );
			if( retryDelay < 0 ) throw new IllegalArgumentException( "retryDelay cannot be < 0" );

			RetryPolicy instance = new RetryPolicy();
			instance.mCount = count;
			instance.mPolicy = policy;
			instance.mRetryDelay = retryDelay;
			return instance;
		}
	}

	private Policy mPolicy;
	private long mCount;
	private long mRetryDelay;

	private RetryPolicy() {
	}

	public Policy getPolicy() {
		return mPolicy;
	}

	public long getCount() {
		return mCount;
	}

	public long getRetryDelay() {
		return mRetryDelay;
	}
}
