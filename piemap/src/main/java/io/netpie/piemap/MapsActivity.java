package io.netpie.piemap;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

import io.netpie.microgear.*;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Microgear microgear = new Microgear(this);
    private String appid = "demoMicrogear"; //APP_ID
    private String key = "WZkTuIs3kkY2JKy"; //KEY
    private String secret = "k6RGdYfTj1eeIwu3Tug7pmrca"; //SECRET
    public int x = 0;
    public int y = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        MicrogearCallBack callback = new MicrogearCallBack();
        microgear.connect(appid,key,secret);
        microgear.setCallback(callback);
        microgear.subscribe("/linkitone/gps");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng sydney = new LatLng(x, y);
                mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                Log.i("Test",x+" "+y);
            }
        });
    }

    class MicrogearCallBack implements MicrogearEventListener{
        @Override
        public void onConnect() {
//            Message msg = handler.obtainMessage();
//            Bundle bundle = new Bundle();
//            bundle.putString("myKey", "Now I'm connected with netpie");
//            msg.setData(bundle);
//            handler.sendMessage(msg);
            Log.i("Connected","Now I'm connected with netpie");
        }

        @Override
        public void onMessage(String topic, String message) {
//            Message msg = handler.obtainMessage();
//            Bundle bundle = new Bundle();
//            bundle.putString("myKey", topic+" : "+message);
//            msg.setData(bundle);
//            handler.sendMessage(msg);
            if(topic.equals("/demoMicrogear/linkitone/gps")){
                String[] messagei = message.split(",");
                MapsActivity.this.x = Integer.parseInt(messagei[0]);
                MapsActivity.this.y = Integer.parseInt(messagei[1]);
            }
            Log.i("Message",topic+" : "+message);
        }

        @Override
        public void onPresent(String token) {
//            Message msg = handler.obtainMessage();
//            Bundle bundle = new Bundle();
//            bundle.putString("myKey", "New friend Connect :"+token);
//            msg.setData(bundle);
//            handler.sendMessage(msg);
            Log.i("present","New friend Connect :"+token);
        }

        @Override
        public void onAbsent(String token) {
//            Message msg = handler.obtainMessage();
//            Bundle bundle = new Bundle();
//            bundle.putString("myKey", "Friend lost :"+token);
//            msg.setData(bundle);
//            handler.sendMessage(msg);
            Log.i("absent","Friend lost :"+token);
        }

        @Override
        public void onDisconnect() {
//            Message msg = handler.obtainMessage();
//            Bundle bundle = new Bundle();
//            bundle.putString("myKey", "Disconnected");
//            msg.setData(bundle);
//            handler.sendMessage(msg);
            Log.i("disconnect","Disconnected");
        }

        @Override
        public void onError(String error) {
//            Message msg = handler.obtainMessage();
//            Bundle bundle = new Bundle();
//            bundle.putString("myKey", "Exception : "+error);
//            msg.setData(bundle);
//            handler.sendMessage(msg);
            Log.i("exception","Exception : "+error);
        }
    }


}
