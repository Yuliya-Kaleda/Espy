package com.example.c4q_ac35.espy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.c4q_ac35.espy.foursquare.FourSquareAPI;
import com.example.c4q_ac35.espy.foursquare.ResponseAPI;
import com.example.c4q_ac35.espy.foursquare.Venue;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;


public class HomeSearchActivity extends Fragment
        implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnStreetViewPanoramaReadyCallback
{
    private String title;
    private int page;
    private VenueAdapter adapter;
    public static final String BASE_API = "https://api.foursquare.com/v2";
    public static final String TAG = "HomeSearchActivity";
    FourSquareAPI servicesFourSquare = null;
    public Venue[] venuee = null;
    private boolean resultsFound = false;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static final long MIN_LOCATION_TIME = 1 * 1000;

    private static final String LOG_TAG = "HomeSearchActivity";
    private LocationManager locationManager;
    private GoogleApiClient mGoogleApiClient;
    private AutoCompleteTextView mAutocompleteTextView;
    GoogleMap map;
    Typeface ndad;
    //Context mContext;

    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(40.498425, -74.250219), new LatLng(40.792266, -73.776434));
    private static final int GOOGLE_API_CLIENT_ID = 1;
    private PlacesAdapter mPlaceArrayAdapter;


    public static HomeSearchActivity newInstance(int page, String title) {
        HomeSearchActivity homeSearchActivity = new HomeSearchActivity();
        Bundle args = new Bundle();
        args.putInt("homePage", page);
        args.putString("home", title);
        homeSearchActivity.setArguments(args);
        return homeSearchActivity;
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        mContext = activity;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        page = getArguments().getInt("homePage", 0);
        title = getArguments().getString("home");

        RestAdapter mRestAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog(TAG))
                .setEndpoint(BASE_API).build();

        servicesFourSquare = mRestAdapter.create(FourSquareAPI.class);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), GOOGLE_API_CLIENT_ID, this)
               // .addConnectionCallbacks(this)
                .build();


    }

    @Override
    public void onResume() {
        super.onResume();


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        Log.d(TAG, "provider: " + provider);

        android.location.Location location = getLocation(locationManager);

        Log.d(TAG, "Location: " + location);


        if (location != null) {
            Log.d(TAG, "date: " + (System.currentTimeMillis() - location.getTime()));
            updateLocation(location);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search_list, container, false);




        mAutocompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.et_autocomplete_places);

        mAutocompleteTextView.setThreshold(3);

        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceArrayAdapter = new PlacesAdapter(view.getContext(), android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_MOUNTAIN_VIEW, null);

        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);
//        mAutocompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                boolean handled = false;
//                LatLng latLng;
//                if ( actionId == EditorInfo.IME_ACTION_GO){
//                    latLng = getLocationFromAddress(mAutocompleteTextView.getText().toString());
//                    setViewToLocation(latLng);
//                    handled = true;
//                }
//
//                return handled;
//            }
//        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.listView);

        return view;
    }

    ///////// from sufei /////////

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(getActivity());
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return p1;
    }

    private void setViewToLocation(LatLng latLng) {
        if (map != null) {
            // Sets initial view to current location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        }
    }


    /////////////// from sufei ////////

    @Override
    public void onLocationChanged(android.location.Location location) {

        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
            Log.d(TAG, "OnLocationChange" + location);
            Log.d(TAG, "provider: " + location.getProvider());
            updateLocation(location);

            if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
                locationManager.removeUpdates(this);

            }
        }

    }

    private void updateLocation(Location location) {
        final String ll = String.format("%g,%g", location.getLatitude(), location.getLongitude());
        servicesFourSquare.getFeed(ll, new FourSquareCallback());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "OnStatusChanged" + status + ", " + provider);

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "OnProviderEnabled" + provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "OnProviderDisabled" + provider);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(getActivity(),
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {

        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {

        streetViewPanorama.setPosition(new LatLng(-33.87365, 151.20689));

    }

    class FourSquareCallback implements Callback<ResponseAPI> {

        @Override
        public void success(final ResponseAPI responseAPI, Response response) {

            venuee = new Venue[responseAPI.getResponse().getVenues().size()];

            resultsFound = true;
            if (adapter == null) {
                List<Venue> venueList = responseAPI.getResponse().getVenues();

                venuee = venueList.toArray(new Venue[venueList.size()]);
                adapter = new VenueAdapter(getActivity(), venuee);
                mRecyclerView.setAdapter(adapter);
                mRecyclerView.setLayoutManager((new LinearLayoutManager(getActivity())));

            }

            Log.d(TAG, "Success");
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d(TAG, "Failure");

        }
    }

    public android.location.Location getLocation(LocationManager mLocationManager) {

        android.location.Location location = null;

        // getting GPS status
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            // First get location from Network Provider
            if (isNetworkEnabled) {
                Log.d("Network", "Network");
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null && (System.currentTimeMillis() - location.getTime()) < MIN_LOCATION_TIME) {
                    return location;
                }
            }
            //get the location by gps
            if (isGPSEnabled) {

                Log.d("GPS Enabled", "GPS Enabled");
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null && (System.currentTimeMillis() - location.getTime()) < MIN_LOCATION_TIME) {
                    return location;
                }
            }
        }

        if (isNetworkEnabled)
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
        if (isGPSEnabled)
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

        return null;
    }


//    public void searchPlaces() {
//
//        SearchView searchView = new SearchView(getActivity());
//        searchView.findViewById(R.id.search_field);
//        searchView.getQueryHint();
//        searchView.getSuggestionsAdapter();
//        searchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//
//            }
//        });

//    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        }

    };

}