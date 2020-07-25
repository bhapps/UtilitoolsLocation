package bhapps.utilitools.location.kotlin

import com.google.android.gms.location.LocationRequest

public class LocationsHelper {

    data class LocationConfig(val interval: Long = 10000, val fastestInterval: Long = 5000L, val priority: Int = LocationRequest.PRIORITY_HIGH_ACCURACY)

}