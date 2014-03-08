/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package it.sephiroth.android.library.subtlerater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Created by alessandro on 08/03/14.
 */
public class SubtleRater {

	private static final String LOG_TAG = "SubtleRater";

	/** name of the preferences file */
	private static final String PREFS_NAME = "apprater";

	private static final String KEY_COUNT = "launch_count";
	private static final String KEY_CLICKED = "dontshowagain";
	private static final String KEY_NEXT_TIME = "timenextcheck";

	private static final int MSG_SHOW = 1;
	private static final int MSG_HIDE = 2;

	public static interface ShowListener {
		/**
		 * User clicked the "rate it" {@link android.widget.TextView},<br />
		 * Return true if you want to manage the behavior, false for the default one ( gplay will be opened )
		 *
		 * @return false for the default behavior, true to manually manage it
		 */
		boolean onRateClicked();

		void onRateShown();

		void onRateClosed();
	}

	public static final class Builder {

		private final Activity activity;
		private long delay = 0;
		private long hideAfter = 0;
		private ShowListener listener;
		private int in_animation = R.anim.subtlerater_in_animation;
		private int out_animation = R.anim.subtlerater_out_animation;
		private int contentResId = R.layout.subtlerater_content;
		private CharSequence text;
		private RetryPolicy policy;
		private ViewGroup parent;

		public Builder( Activity activity ) {
			this.activity = activity;
		}

		public Builder withDelay( long delay ) {
			this.delay = delay;
			return this;
		}

		public Builder hideAfter( long ms ) {
			this.hideAfter = ms;
			return this;
		}

		public Builder withListener( ShowListener listener ) {
			this.listener = listener;
			return this;
		}

		public Builder inAnimation( int animation ) {
			this.in_animation = animation;
			return this;
		}

		public Builder outAnimation( int animation ) {
			this.out_animation = animation;
			return this;
		}

		public Builder withContent( int contentResId ) {
			this.contentResId = contentResId;
			return this;
		}

		public Builder withText( int resId ) {
			return withText( activity.getString( resId ) );
		}

		public Builder withText( CharSequence text ) {
			this.text = text;
			return this;
		}

		public Builder withPolicy( RetryPolicy policy ) {
			this.policy = policy;
			return this;
		}

		public Builder withParent( ViewGroup parent ) {
			this.parent = parent;
			return this;
		}

		public SubtleRater build() {
			if( null == activity ) throw new IllegalArgumentException( "activity cannot be null" );
			if( delay < 0 ) throw new IllegalArgumentException( "delay cannot be < 0" );
			if( hideAfter < 0 ) throw new IllegalArgumentException( "hideAfter cannot be < 0" );
			if( null == policy ) throw new IllegalArgumentException( "policy cannot be null" );

			SubtleRater instance = new SubtleRater( activity );
			instance.mDelay = delay;
			instance.mHideAfter = hideAfter;
			instance.mListener = listener;
			instance.mInAnimation = in_animation;
			instance.mOutAnimation = out_animation;
			instance.mContentId = contentResId;

			if( null == text ) {
				ApplicationInfo info = activity.getApplicationInfo();
				CharSequence label = info.loadLabel( activity.getPackageManager() );
				text = activity.getString( R.string.subtlerater_text, label );
			}

			instance.mText = text;
			instance.mPolicy = policy;
			instance.mParent = parent;
			return instance;
		}
	}

	private Activity mActivity;
	private long mDelay;
	private long mHideAfter;
	private ShowListener mListener;
	private int mInAnimation;
	private int mOutAnimation;
	private int mContentId;
	private CharSequence mText;
	private RetryPolicy mPolicy;
	private ViewGroup mParent;
	private View mView;

	private final SharedPreferences preferences;
	private final SharedPreferences.Editor editor;

	private final Handler handler = new Handler( new Handler.Callback() {
		@Override
		public boolean handleMessage( final Message message ) {
			switch( message.what ) {
				case MSG_HIDE:
					hide();
					return true;

				case MSG_SHOW:
					displayView();
					return true;
			}
			return false;
		}
	} );

	private SubtleRater( Activity activity ) {
		this.mActivity = activity;
		this.preferences = activity.getSharedPreferences( PREFS_NAME, 0 );
		this.editor = preferences.edit();
	}

	public void hide() {
		Log.i( LOG_TAG, "hide" );

		if( mPolicy.getRetryDelay() > 0 ) {
			setNextCheckTime( System.currentTimeMillis() + mPolicy.getRetryDelay() );
		}
		close();
	}

	/**
	 * Change the auto hide policy.<br />
	 * If <b>ms</b> is &gt; 0 the view will auto hide itself after a delay. Set to <b>0</b> to deny auto hide.
	 *
	 * @param ms time in milliseconds
	 * @see it.sephiroth.android.library.subtlerater.SubtleRater.Builder#hideAfter(long)
	 */
	public void setHideAfter( final long ms ) {
		this.mHideAfter = ms;
	}

	public void show() {
		Log.i( LOG_TAG, "show" );

		final boolean clicked = preferences.getBoolean( KEY_CLICKED, false );
		if( clicked ) {
			Log.w( LOG_TAG, "never show again" );
			return;
		}

		// current launch count
		long count = incrementCountAndGet();

		// current time
		final long now = System.currentTimeMillis();

		// next check time
		final long next_check = getNextCheckTime();


		Log.d( LOG_TAG, "count:      " + count + " of " + mPolicy.getCount() );
		Log.d( LOG_TAG, "now:        " + now );
		Log.d( LOG_TAG, "next_check: " + next_check );

		switch( mPolicy.getPolicy() ) {
			case NONE:
				if( count == mPolicy.getCount() ) {
					// ok, show the rater
					open();
				}
				break;

			case INCREMENTAL:
				if( next_check >= now ) {
					Log.w( LOG_TAG, "wait more time... " + ( next_check - now ) );
					resetCount();
				}
				else {
					if( count % mPolicy.getCount() == 0 ) {
						open();
					}
				}
				break;

			case EXPONENTIAL:
				if( next_check >= now ) {
					Log.w( LOG_TAG, "wait more time... " + ( next_check - now ) );
					resetCount();
				}
				else {
					if( count % mPolicy.getCount() == 0 && Utils.isPowerOfTwo( count / mPolicy.getCount() ) ) {
						open();
					}
				}
				break;
		}
	}

	/**
	 * Resets the launch count
	 */
	public void resetCount() {
		Log.i( LOG_TAG, "resetCount" );
		editor.putLong( KEY_COUNT, 0 );
		editor.commit();
	}

	/**
	 * Reset all the preference stored in the {@link it.sephiroth.android.library.subtlerater.SubtleRater}
	 */
	public void reset() {
		Log.i( LOG_TAG, "reset" );
		editor.clear();
		editor.commit();
	}

	public void forceShow() {
		Log.i( LOG_TAG, "forceShow" );
		open();
	}

	public void destroy() {
		Log.i( LOG_TAG, "destroy" );
		removeAllMessages();
		mListener = null;
		mActivity = null;
		removeFromParent();
	}

	private void removeAllMessages() {
		Log.i( LOG_TAG, "removeAllMessages" );
		handler.removeMessages( MSG_HIDE );
		handler.removeMessages( MSG_SHOW );
	}

	private void dontShowAgain() {
		editor.putBoolean( KEY_CLICKED, true );
		editor.commit();
	}

	private long incrementCountAndGet() {
		long count;

		try {
			count = preferences.getLong( KEY_COUNT, 0 );
		} catch( ClassCastException e ) {
			count = 0;
		}

		editor.putLong( KEY_COUNT, count + 1 );
		editor.commit();
		return count + 1;
	}

	private long getNextCheckTime() {
		try {
			return preferences.getLong( KEY_NEXT_TIME, 0 );
		} catch( ClassCastException e ) {
			return 0;
		}
	}

	private void setNextCheckTime( long ms ) {
		editor.putLong( KEY_NEXT_TIME, ms );
		editor.commit();
	}

	private void open() {
		Log.i( LOG_TAG, "open" );

		if( ! valid() ) return;

		removeAllMessages();

		final View view = getOrCreateView();
		view.clearAnimation();

		if( mDelay > 0 ) {
			handler.sendEmptyMessageDelayed( MSG_SHOW, mDelay );
		}
		else {
			displayView();
		}
	}

	private void displayView() {
		Log.i( LOG_TAG, "displayView" );

		if( ! valid() || null == mView ) return;

		ViewGroup.LayoutParams params = mView.getLayoutParams();
		if( null == params ) {
			Log.d( LOG_TAG, "generating LayoutParams" );
			params = new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
		}

		if( mParent != null ) {
			mParent.addView( mView, params );
			Log.w( LOG_TAG, "parent childCount: " + mParent.getChildCount() );
		}
		else {
			mActivity.addContentView( mView, params );
		}

		Animation animation = AnimationUtils.loadAnimation( mActivity, mInAnimation );
		animation.setAnimationListener( new Animation.AnimationListener() {
			@Override
			public void onAnimationStart( final Animation animation ) {

			}

			@Override
			public void onAnimationEnd( final Animation animation ) {
				Log.i( LOG_TAG, "onAnimationEnd" );
				if( null != mListener ) {
					mListener.onRateShown();
				}

				if( mHideAfter > 0 ) {
					handler.sendEmptyMessageDelayed( MSG_HIDE, mHideAfter );
				}
				mView.requestFocus();
			}

			@Override
			public void onAnimationRepeat( final Animation animation ) {

			}
		} );

		mView.startAnimation( animation );
	}

	private View getOrCreateView() {
		if( null == mView ) {
			final LayoutInflater inflater = LayoutInflater.from( mActivity );
			if( null != mParent ) {
				mView = inflater.inflate( mContentId, mParent, false );
			}
			else {
				mView = inflater.inflate( mContentId, null );
			}

			View close = mView.findViewById( android.R.id.closeButton );
			TextView textView = (TextView) mView.findViewById( android.R.id.text1 );

			if( null != mText ) {
				textView.setText( mText );
			}

			close.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( final View view ) {
					dontShowAgain();
					close();
				}
			} );

			textView.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick( final View view ) {
					if( null != mListener ) {
						if( ! mListener.onRateClicked() ) {
							handleRateClick();
						}
					}
					else {
						handleRateClick();
					}
					dontShowAgain();
					close();
				}
			} );

		}
		return mView;
	}

	private void handleRateClick() {
		if( ! valid() ) return;
		mActivity.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=" + mActivity.getPackageName() ) ) );
	}

	private void close() {
		Log.i( LOG_TAG, "close" );

		if( ! valid() || null == mView ) return;

		removeAllMessages();

		final Animation animation = AnimationUtils.loadAnimation( mActivity, mOutAnimation );
		animation.setAnimationListener( new Animation.AnimationListener() {
			@Override
			public void onAnimationStart( final Animation animation ) {
			}

			@Override
			public void onAnimationEnd( final Animation animation ) {
				Log.i( LOG_TAG, "out: onAnimationEnd" );

				if( null != mListener ) {
					mListener.onRateClosed();
				}
				removeFromParent();
			}

			@Override
			public void onAnimationRepeat( final Animation animation ) {
			}
		} );

		mView.clearAnimation();
		mView.startAnimation( animation );
		mView.clearFocus();
	}

	private void removeFromParent() {
		Log.i( LOG_TAG, "removeFromParent" );

		if( null != mView ) {
			ViewParent parent = mView.getParent();
			if( null != parent && parent instanceof ViewGroup ) {
				( (ViewGroup) parent ).removeView( mView );
				Log.w( LOG_TAG, "parent childCount: " + ( (ViewGroup) parent ).getChildCount() );
				mView = null;
			}
		}
	}

	private boolean valid() {
		return null != mActivity;
	}
}
