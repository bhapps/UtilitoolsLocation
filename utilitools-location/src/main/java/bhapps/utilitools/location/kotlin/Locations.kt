package bhapps.utilitools.location.kotlin

import android.app.Activity
import android.location.Location

interface Locations {

    fun getLastKnownPosition(activity: Activity, onLastLocationFound: ((Location) -> Unit)?, onNoLocationFound: (() -> Unit)?)

    fun startLocationUpdates(activity: Activity, config: Locations, onLocationChange: (Location) -> Unit)

    fun stopLocationUpdates()

}