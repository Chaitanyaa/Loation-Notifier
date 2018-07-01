package com.example.saichaitanya.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends Activity implements LocationListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    AutoCompleteTextView atvPlaces;
    PlacesTask placesTask;
    ParserTask parserTask;
    Spinner staticSpinner;
    GoogleApiClient mGoogleApiClient;
    GoogleMap map;
    Location loc;
    public static LatLng start, end;
    public static String placeId;
    public static String distance;
    String duration;
    public static int selec;
    TextView dist,tvs,dur;
    public static LocationManager locationManager;
    AlarmManager alarmManager;
    Intent alertIntent;
    Button noti,stop,inmap,offline;
    int time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        noti=(Button)findViewById(R.id.butnot);noti.setEnabled(false);
        stop=(Button)findViewById(R.id.stop);stop.setEnabled(false);
        inmap=(Button)findViewById(R.id.button);inmap.setEnabled(false);
        offline=(Button)findViewById(R.id.off);
        offline.setEnabled(false);
        staticSpinner = (Spinner) findViewById(R.id.spinner);
        staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selec=position;Log.v("item", ""+position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.prenot,
                        android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        staticSpinner.setAdapter(staticAdapter);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        dist = (TextView) findViewById(R.id.dist);
        dur=(TextView)findViewById(R.id.dur);
        tvs=(TextView)findViewById(R.id.tvs);
        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        atvPlaces.setThreshold(1);

        atvPlaces.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                if(checkInternetConnection()) {
                    placesTask.execute(s.toString());
                }
                else{
                    nonet();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkInternetConnection()) {
            mGoogleApiClient.connect();
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {

                    PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                    String content = "";
                    if (placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty(placeLikelihood.getPlace().getName()))
                        content = "" + placeLikelihood.getPlace().getName() + "\n";
                    Log.d("Check:", content);
                    tvs.setText(content);
                    likelyPlaces.release();
                }
            });
        }
        else{
            nonet();
        }
    }


    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    public boolean checkInternetConnection() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() &&    conMgr.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            System.out.println("Internet Connection Not Present");
            return false;
        }
    }
    public void nonet(){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG);
        toast.show();
    }
    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            //Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void showDistance(View view) {
        if(checkInternetConnection()) {
            findPlaceById(placeId);
            inmap.setEnabled(true);
            noti.setEnabled(true);
            offline.setEnabled(true);
        }
        else{
            nonet();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        start=new LatLng(location.getLatitude(),location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    public void setAlarm(View view) {
        if(checkInternetConnection()) {

            // Define a time value of 5 seconds
            //Long alertTime = new GregorianCalendar().getTimeInMillis() + 5 * 1000;

            // Define our intention of executing AlertReceiver
            alertIntent = new Intent(this, AlertReceiver.class);

            // Allows you to schedule for your application to do something at a later date
            // even if it is in he background or isn't active
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Log.d("Hello", "almanag");
            // set() schedules an alarm to trigger
            // Trigger for alertIntent to fire in 5 seconds
            // FLAG_UPDATE_CURRENT : Update the Intent if active
            //alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
            //       PendingIntent.getBroadcast(this, 1, alertIntent,
            //               PendingIntent.FLAG_UPDATE_CURRENT));
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 3 * 60 * 1000, PendingIntent.getBroadcast(this, 1, alertIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT));
            Log.d("Hello", "repeat");
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Notification Set", Toast.LENGTH_SHORT);
            toast.show();
            stop.setEnabled(true);
            offline.setEnabled(false);
        }
        else{
            nonet();
        }
    }

    public void stopClic(View view) {
        if (alarmManager != null)
            alarmManager.cancel(PendingIntent.getBroadcast(this, 1, alertIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT));System.exit(0);
    }

    public void inMap(View view) {
        if(checkInternetConnection()) {
            Intent i = new Intent(this, MapFrag.class);
            startActivity(i);
        }
        else{
            nonet();
        }
    }

    public void offlineAlert(View view) {
        Long alertTime = new GregorianCalendar().getTimeInMillis() + 10000;

        // Define our intention of executing AlertReceiver
        alertIntent = new Intent(this, OfflineAlertReciever.class);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.d("Hello","almanag");
        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
               PendingIntent.getBroadcast(this, 1, alertIntent,
                       PendingIntent.FLAG_UPDATE_CURRENT));
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, "Notification Set", Toast.LENGTH_SHORT);
        toast.show();
        noti.setEnabled(false);
        stop.setEnabled(true);


    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {

            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyCFGvOeMTe7h9ukQYAzMIDBmDs52SnuPb4";

            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + types + "&" + sensor + "&" + key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

            try {
                // Fetching the data from we service
                data = downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            atvPlaces.setAdapter(adapter);


        }
    }

    private void findPlaceById(String id) {
        if (TextUtils.isEmpty(id) || mGoogleApiClient == null || !mGoogleApiClient.isConnected())
            return;

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, id).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess()) {
                    Place place = places.get(0);
                    end = place.getLatLng();
                    //if(end!=null&&start!=null) {
                        String url = getDirectionsUrl(start, end);
                        DownloadTask downloadTask = new DownloadTask();
                        downloadTask.execute(url);
                    //}
                    //else{
                    //    findPlaceById(placeId);
                    //}

                }

                //Release the PlaceBuffer to prevent a memory leak
                places.release();
            }
        });
    }

    public void calculateDuration(String s){
        if(!s.contains("h")){
            //Should code for Hour's' and min's' case.

        }
        else{
            if(s.contains("s")){
                time=Integer.parseInt(s.substring(0,s.length()-5));
            }
            else{
                time=Integer.parseInt(s.substring(0,s.length()-4));
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//=======================================================================================================================

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String loadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Excep downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = loadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParseTask parserTask = new ParseTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            distance="";
            duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }
            dist.setText("Distance:" + distance);
            dur.setText("Duration:" + duration);
            calculateDuration(duration);
            Log.d("Dura",duration.substring(0,duration.length()-4));
        }
    }
}
