SubtleRater
===========

Discreet AppRater for Android

Inspired by the [Discreen App Rate][1] library, but with more customizations.

# Installation
Using gradle, just add this to your dependencies:

	compile 'it.sephiroth.android.library.subtlerater:library:+@aar'


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
	
If auto hide is set ( using hideAfter ), then the view will be automatically hidden after a certain amount of time, otherwise you should call manually:

	mRater.hide();
	
In order to understand the available policies which can be used with the **withPolicy** method:

The RetryPolicy builder has the following methods:

* **RetryPolicy( Policy )**: is the constructor and the required policy enum has the following fields: 

	* **NONE**: After the view has been shown to the user, it will never appear again.
	* **INCREMENTAL**: Will retry each time the initial count has been triggered (2, 4, 6, 8...)
	* **EXPONENTIAL**: Will retry exponentially to be less intrusive (2, 4, 8, 16...)

* **launchCount( long count )**: After how many invocations the user will be notified
* **retryDelay( long ms )**: To be used with INCREMENTAL or EXPONENTIAL. If set, once the SubtleRater is hidden without user interaction ( **hideAfter** is > 0 or **hide** is called manually ), a subsequent invocation of the **show** method will first check if enough time has passed before start checking the launchCount again. Use this to be less intrusive to your users.

# Customization

The rater View/Animation can be customized using the Builder's methods withContent, inAnimation, outAnimation. Also the parent ViewGroup can be set ( otherwise the activity root will be used ).

For a more detailed example, see the demo application included.


# Sample Video
[![Video](https://i.ytimg.com/vi/i-po9n-kiL4/3.jpg?1394313141348)](http://www.youtube.com/watch?v=i-po9n-kiL4&feature=youtu.be)



[1]: https://github.com/PomepuyN/discreet-app-rate
