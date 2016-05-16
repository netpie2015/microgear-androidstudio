package io.netpie.piedroid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import netpie.io.netpiegear.EventListener;
import netpie.io.netpiegear.Microgear;

public class MainActivity extends Activity {

    public Microgear microgear = new Microgear(this);

    EventListener eventListener = new EventListener();
    Button ex;
    String appid = "appid"; //APP_ID
    String key = "key"; //KEY
    String secret = "secret"; //SECRET

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ex = (Button) findViewById(R.id.btn_ex);
        ex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                microgear.connect(appid,key,secret);
                microgear.subscribe("gear");
                (new Thread(new Runnable()
                {
                    int count = 1;
                    @Override
                    public void run()
                    {
                        while (!Thread.interrupted())
                            try
                            {
                                runOnUiThread(new Runnable() // start actions in UI thread
                                {

                                    @Override
                                    public void run(){
                                        microgear.publish("gear", String.valueOf(count)+".  Test message");
                                        count++;
                                    }
                                });
                                Thread.sleep(2000);
                            }
                            catch (InterruptedException e)
                            {
                                // ooops
                            }
                    }
                })).start();
            }
        });

        eventListener.setConnectEventListener(new EventListener.OnServiceConnect() {
            @Override
            public void onConnect(Boolean status) {
                if(status == true){
                    Log.i("Connected","Now I'm connected with netpie");
                }
                else{
                    Log.i("NotConnect","Can't connect to netpie");
                }
            }

        });

        eventListener.setMessageEventListener(new EventListener.OnMessageReceived() {
            @Override
            public void onMessage(String topic, String message) {
                Log.i("Message",topic+" : "+message);
            }
        });

        eventListener.setPresentEventListener(new EventListener.OnPresent() {
            @Override
            public void onPresent(String name) {
                Log.i("present","New friend Connect :"+name);
            }
        });

        eventListener.setAbsentEventListener(new EventListener.OnAbsent() {
            @Override
            public void onAbsent(String name) {
                Log.i("absent","Friend lost :"+name);
            }
        });

        eventListener.setDisconnectEventListener(new EventListener.OnClose() {
            @Override
            public void onDisconnect(Boolean status) {
                Log.i("disconnect","Disconnected");
            }
        });

        eventListener.setOnException(new EventListener.OnException() {
            @Override
            public void onException(String error) {
                Log.i("exception","Exception : "+error);
            }
        });
    }


    protected void onDestroy() {
        super.onDestroy();
        microgear.disconnect();
    }

    protected void onResume() {
        super.onResume();
        microgear.bindServiceResume();
    }

}
