package bhapps.utilitools.location.kotlin

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class LocationsManager {

    /*

        //val locationManager : LocationsManager()
        locationManager.getLastKnownPosition(
        activity,
        onLastLocationFound = { location ->
            // handle location data
        },
        onNoLocationFound = {
            // handle no location data
        })

        locationManager.startLocationUpdates(
        activity,
        onLocationChange = { location ->
            // handle location data
        },
        config = LocationsHelper().LocationConfig())

        locationManager.stopLocationUpdates()

     */


    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var onLastLocationFound: ((Location) -> Unit)? = null
    private var onNoLocationFound: (() -> Unit)? = null
    private var onLocationChange: ((Location) -> Unit)? = null
    private var locationRequest: LocationRequest? = null
    private var lastTrackedLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    private var permissions_list_to_check = arrayOf(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )


    /**
     * get last known location
     *
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownPosition(activity: Activity, onLastLocationFound: ((Location) -> Unit)?, onNoLocationFound: (() -> Unit)?) {

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        }

        this.onLastLocationFound = onLastLocationFound
        this.onNoLocationFound = onNoLocationFound

        getLastLocation()

    }

    fun startLocationUpdates(activity: Activity, config: LocationsHelper.LocationConfig, onLocationChange: (Location) -> Unit) {

        this.onLocationChange = onLocationChange

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        }

        locationRequest = LocationRequest().apply {
            interval = config.interval
            fastestInterval = config.fastestInterval
            priority = config.priority
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            requestLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }
    }


    /**
     * execute last location request
     *
     */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient
            ?.lastLocation
            ?.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLastLocationFound?.invoke(location)
                } else {
                    onNoLocationFound?.invoke()
                }
            } ?: onNoLocationFound?.invoke()
    }

    /**
     * execute location change updates
     *
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (location.latitude != lastTrackedLocation?.latitude
                        || location.longitude != lastTrackedLocation?.longitude
                        || location.accuracy != lastTrackedLocation?.accuracy) {
                        onLocationChange?.invoke(location)
                        lastTrackedLocation = location
                    }
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)

    }

}