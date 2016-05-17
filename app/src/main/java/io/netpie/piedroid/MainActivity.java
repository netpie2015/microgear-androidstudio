package io.netpie.piedroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import netpie.io.netpiegear.EventListener;
import netpie.io.netpiegear.Microgear;

public class MainActivity extends Activity {

    public Microgear microgear = new Microgear(this);

    EventListener eventListener = new EventListener();
    Button button;
    String appid = "APPID"; //APP_ID
    String key = "KEY"; //KEY
    String secret = "SERCRET"; //SECRET

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString("myKey");
            TextView myTextView =
                    (TextView)findViewById(R.id.textView_ex);
            myTextView.append(string+"\n");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.btn_ex);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                microgear.connect(appid,key,secret);
                microgear.subscribe("Topictest");
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
                                        microgear.publish("Topictest", String.valueOf(count)+".  Test message");
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
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("myKey", "Now I'm connected with netpie");
                    msg.setData(bundle);
                    handler.sendMessage(msg);
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
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("myKey", topic+" : "+message);
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.i("Message",topic+" : "+message);
                //text.setText(topic+" "+message);
            }
        });

        eventListener.setPresentEventListener(new EventListener.OnPresent() {
            @Override
            public void onPresent(String name) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("myKey", "New friend Connect :"+name);
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.i("present","New friend Connect :"+name);
            }
        });

        eventListener.setAbsentEventListener(new EventListener.OnAbsent() {
            @Override
            public void onAbsent(String name) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("myKey", "Friend lost :"+name);
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.i("absent","Friend lost :"+name);
            }
        });

        eventListener.setDisconnectEventListener(new EventListener.OnClose() {
            @Override
            public void onDisconnect(Boolean status) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("myKey", "Disconnected");
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.i("disconnect","Disconnected");
            }
        });

        eventListener.setOnException(new EventListener.OnException() {
            @Override
            public void onException(String error) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("myKey", "Exception : "+error);
                msg.setData(bundle);
                handler.sendMessage(msg);
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
