package io.netpie.piedroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import netpie.io.netpiegear.Microgear;
import netpie.io.netpiegear.MicrogearEventListener;

public class MainActivity extends Activity  {

    private Microgear microgear = new Microgear(this);
    private Button button;
    private String appid = "APPID"; //APP_ID
    private String key = "APPKEY"; //KEY
    private String secret = "SECRET"; //SECRET

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
                MicrogearCallBack callback = new MicrogearCallBack();
                microgear.connect(appid,key,secret);
                microgear.setCallback(callback);
                microgear.subscribe("Topictest");
                microgear.subscribe("/chat");
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

    }


    protected void onDestroy() {
        super.onDestroy();
        microgear.disconnect();
    }

    protected void onResume() {
        super.onResume();
        microgear.bindServiceResume();
    }

    class MicrogearCallBack implements MicrogearEventListener{
        @Override
        public void onConnect() {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("myKey", "Now I'm connected with netpie");
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i("Connected","Now I'm connected with netpie");
        }

        @Override
        public void onMessage(String topic, String message) {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("myKey", topic+" : "+message);
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i("Message",topic+" : "+message);
            if(message.equals("msg#test#test")){
                Log.i("Testtttt","sssssssssssssss");
                MainActivity.this.microgear.publish("/chat", "Hello world#pppppp#qqqqq");
            }
            if(message.equals("msg#test#bye")){
                MainActivity.this.microgear.disconnect();
            }
            if(message.equals("msg#test#sub")){
                MainActivity.this.microgear.subscribe("qqqq");
            }
            if(message.equals("msg#test#unsub")){
                MainActivity.this.microgear.unsubscribe("qqqq");
            }
        }

        @Override
        public void onPresent(String token) {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("myKey", "New friend Connect :"+token);
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i("present","New friend Connect :"+token);
        }

        @Override
        public void onAbsent(String token) {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("myKey", "Friend lost :"+token);
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i("absent","Friend lost :"+token);
        }

        @Override
        public void onDisconnect() {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("myKey", "Disconnected");
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i("disconnect","Disconnected");
        }

        @Override
        public void onError(String error) {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("myKey", "Exception : "+error);
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i("exception","Exception : "+error);
        }
    }



}
