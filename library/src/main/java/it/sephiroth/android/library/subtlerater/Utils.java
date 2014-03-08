package it.sephiroth.android.library.subtlerater;

/**
 * Created by alessandro on 08/03/14.
 */
public class Utils {
	public static boolean isPowerOfTwo( long x ) {
		return ( x & ( x - 1 ) ) == 0;
	}
}
