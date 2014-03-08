package it.sephiroth.android.library.subtlerater.demo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import it.sephiroth.android.library.subtlerater.RetryPolicy;
import it.sephiroth.android.library.subtlerater.SubtleRater;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, SubtleRater.ShowListener, CompoundButton.OnCheckedChangeListener {

	private static final String LOG_TAG = "MainActivity";

	public static final long ONE_SECOND = 1000;
	public static final long ONE_MINUTE = ONE_SECOND * 60;
	public static final long ONE_HOUR = ONE_MINUTE * 60;
	public static final long ONE_DAY = ONE_HOUR * 24;
	public static final long ONE_WEEK = ONE_DAY * 7;

	SubtleRater mRater;

	CheckBox checkbox1, checkbox2;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );


		findViewById( R.id.show_button ).setOnClickListener( this );
		findViewById( R.id.hide_button ).setOnClickListener( this );
		findViewById( R.id.force_show_button ).setOnClickListener( this );

		checkbox1 = (CheckBox) findViewById( R.id.auto_hide );
		checkbox2 = (CheckBox) findViewById( R.id.custom_view );

		checkbox1.setOnCheckedChangeListener( this );
		checkbox2.setOnCheckedChangeListener( this );

		SubtleRater.Builder builder = createNew( checkbox2.isChecked() );
		mRater = builder.build();
	}

	private SubtleRater.Builder createNew( boolean custom ) {
		SubtleRater.Builder builder = new SubtleRater.Builder( this );

		if( custom ) {
			// use a custom view, custom animations..
			builder.withContent( R.layout.custom_content );
			builder.inAnimation( android.R.anim.fade_in );
			builder.outAnimation( android.R.anim.fade_out );
			builder.withParent( (ViewGroup) findViewById( R.id.layout01 ) );

		}

		builder.withListener( this );
		builder.withDelay( 500 );
		builder.hideAfter( checkbox2.isChecked() ? 3000 : 0 );
		builder.withPolicy( new RetryPolicy.Builder( RetryPolicy.Policy.INCREMENTAL ).launchCount( 2 ).retryDelay( ONE_MINUTE ).build() );
		return builder;
	}

	@Override
	protected void onDestroy() {
		mRater.destroy();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		int id = item.getItemId();
		if( id == R.id.action_reset_count ) {
			mRater.resetCount();
			return true;
		}
		else if( id == R.id.action_reset_all ) {
			mRater.reset();
			return true;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onClick( final View view ) {
		final int id = view.getId();

		switch( id ) {
			case R.id.show_button:
				mRater.show();
				break;

			case R.id.hide_button:
				mRater.hide();
				break;

			case R.id.force_show_button:
				mRater.forceShow();
				break;
		}
	}

	@Override
	public boolean onRateClicked() {
		Log.i( LOG_TAG, "onRateClicked" );
		return false;
	}

	@Override
	public void onRateShown() {
		Log.i( LOG_TAG, "onRateShown" );

		findViewById( R.id.hide_button ).setEnabled( true );
		findViewById( R.id.show_button ).setEnabled( false );
		findViewById( R.id.force_show_button ).setEnabled( false );

		checkbox2.setEnabled( false );
		checkbox1.setEnabled( false );
	}

	@Override
	public void onRateClosed() {
		Log.i( LOG_TAG, "onRateClosed" );

		findViewById( R.id.hide_button ).setEnabled( false );
		findViewById( R.id.show_button ).setEnabled( true );
		findViewById( R.id.force_show_button ).setEnabled( true );

		checkbox2.setEnabled( true );
		checkbox1.setEnabled( true );
	}

	@Override
	public void onCheckedChanged( final CompoundButton compoundButton, final boolean checked ) {

		final int id = compoundButton.getId();

		if( id == R.id.auto_hide ) {
			mRater.setHideAfter( checked ? 3000 : 0 );
		}
		else if( id == R.id.custom_view ) {
			mRater.destroy();
			mRater = createNew( checked ).build();
		}

	}
}
