package com.creative.carlocator.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;

import com.creative.carlocator.appdata.AppConstant;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by jubayer on 21-Apr-16.
 */
public class GpsEnableTool {

    private static Context context;


    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    private static boolean app_comeFrom_background = false;

    public GpsEnableTool(Context context) {
        this.context = context;
    }

    public void enableGPs() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest);
                        builder.setAlwaysShow(true);
                        PendingResult<LocationSettingsResult> result =
                                LocationServices.SettingsApi.checkLocationSettings(
                                        mGoogleApiClient,
                                        builder.build()
                                );

                        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                            @Override
                            public void onResult(LocationSettingsResult locationSettingsResult) {


                                final Status status = locationSettingsResult.getStatus();
                                switch (status.getStatusCode()) {
                                    case LocationSettingsStatusCodes.SUCCESS:

                                        // NO need to show the dialog;

                                        break;

                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        //  Location settings are not satisfied. Show the user a dialog

                                        try {
                                            // Show the dialog by calling startResolutionForResult(), and check the result
                                            // in onActivityResult().

                                            status.startResolutionForResult((Activity) context, AppConstant.REQUEST_CHECK_SETTINGS);

                                        } catch (IntentSender.SendIntentException e) {

                                            //failed to show
                                        }
                                        break;

                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        // Location settings are unavailable so not possible to show any dialog now
                                        break;
                                }

                            }
                        });

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                }).build();
        mGoogleApiClient.connect();

    }
}
