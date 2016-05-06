package com.creative.carlocator;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.carlocator.appdata.AppConstant;
import com.creative.carlocator.appdata.AppController;
import com.creative.carlocator.map.BaseDemoActivity;
import com.creative.carlocator.utils.GPSTracker;
import com.creative.carlocator.utils.GpsEnableTool;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseDemoActivity {

    GPSTracker gps;
    GpsEnableTool gpsEnableTool;

    Button btn_save, btn_show;

    ArrayList<String> latList, lngList;

    private ProgressDialog progressDialog;

    @Override
    protected void startDemo() {

        getMap().setMyLocationEnabled(true);
        getMap().setTrafficEnabled(true);
        getMap().getUiSettings().setMyLocationButtonEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(true);

        init();

        zoomCameraToCurrentPosition();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                latList.add(String.valueOf(gps.getLatitude()));
                lngList.add(String.valueOf(gps.getLongitude()));

                AppController.getInstance().getPrefManger().setLatList(latList);
                AppController.getInstance().getPrefManger().setLngList(lngList);

            }
        });

        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLoopToDrawThePath();
            }
        });

    }

    private void performLoopToDrawThePath() {

        double start_lat = gps.getLatitude();
        double start_lng = gps.getLongitude();


        for (int i = 0; i < latList.size(); i++) {

           // Log.d("DEBUG_lat",latList.get(i));
           // Log.d("DEBUG_lng",lngList.get(i));

            double end_lat = Double.parseDouble(latList.get(i));
            double end_lng = Double.parseDouble(lngList.get(i));

            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(end_lat, end_lng)).title("park " + i);
            getMap().addMarker(markerOptions);


          sendRequestToServer(AppConstant.DirectionApiUrl(start_lat, start_lng, end_lat, end_lng));


           start_lat = end_lat;
          start_lng = end_lng;

        }
    }


    private void init() {
        gpsEnableTool = new GpsEnableTool(this);
        gpsEnableTool.enableGPs();

        gps = new GPSTracker(this);

        latList = AppController.getInstance().getPrefManger().getLatList();

        lngList = AppController.getInstance().getPrefManger().getLngList();

        btn_save = (Button) findViewById(R.id.btn_save);
        btn_show = (Button) findViewById(R.id.btn_show);
    }

    private void zoomCameraToCurrentPosition() {
        if (gps.canGetLocation()) {
            CameraPosition cameraPosition = new CameraPosition.Builder().
                    target(new LatLng(gps.getLatitude(), gps.getLongitude())).
                    tilt(0).
                    zoom(15).
                    bearing(0).
                    build();

            getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


    public void sendRequestToServer(String url) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        } else if (!progressDialog.isShowing()) {
            progressDialog.show();
        }


        StringRequest req = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        if (response != null) {
                            drawPath(response);
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                        }


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss();


            }
        });
        // req.setRetryPolicy(new DefaultRetryPolicy(Constants.MY_SOCKET_TIMEOUT_MS,
        //        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(req);
    }

    public void drawPath(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = getMap().addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );
        } catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gps.stopUsingGPS();
        gps.stopSelf();
    }
}
