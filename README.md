SubtleRater
===========

Discreet AppRater for Android

Inspired by the [Discreen App Rate][1] library, but with more customizations.

# Installation
Using gradle, just add this to your dependencies:

	compile 'it.sephiroth.android.library:subtlerater:+'


# Usage
Usage is quite simple. This is a common usage

    SubtleRater.Builder builder = new SubtleRater.Builder( this );
    builder.withListener( this );
	builder.withDelay( 500 );
	builder.hideAfter( 3000 );
	builder.withPolicy( 
		new RetryPolicy.Builder( RetryPolicy.Policy.INCREMENTAL )
			.launchCount( 2 )
			.retryDelay( ONE_WEEK ).build()
	);
	
	SubtleRater mRater = builder.build();
	mRater.show();

The rater View/Animation can be customized using the Builder's methods withContent, inAnimation, outAnimation. Also the parent ViewGroup can be set ( otherwise the activity root will be used ).


# Sample Video
[![Video](https://i.ytimg.com/vi/i-po9n-kiL4/3.jpg?1394313141348)](http://www.youtube.com/watch?v=i-po9n-kiL4&feature=youtu.be)



[1]: https://github.com/PomepuyN/discreet-app-rate
