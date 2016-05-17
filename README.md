#Microgear-android 
-----------
microgear-android is a client library for Android Studio.The library is used to connect application code with the NETPIE Platform's service for developing IoT applications. For more details on the NETPIE Platform, please visit https://netpie.io .

##Installation
-----------
<br/>




##Usage Example
-----------
```java

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

```

##Library Usage
-----------
###Microgear
-----------


**microgear.connect(appid, key, secret);**  Connect to NETPIE with the appid, key and secret.

arguments

* *appid* `string` - an application that microgear will connect to.
* *key* `string` - is used as a microgear identity.
*  *secret* `string` - comes in a pair with key. The secret is used for authentication and integrity.

<br/>
**microgear.publish(topic, message,qos,retained);**  In the case that the microgear want to send a message to an unspecified receiver, the developer can use the function publish to the desired topic, which all the microgears that subscribe such topic will receive a message.

arguments

* *topic* `string` - name of topic to be send a message to.
* *message* `string` - message to be sent.
* *qos* `int` - Quality of Service
* *retained* `Boolean` - retain a message or not (the default is false)

<br/>
**microgear.subscribe(topic);**  microgear may be interested in some topic. The developer can use the function subscribe() to subscribe a message belong to such topic. If the topic used to retain a message, the microgear will receive a message everytime it subscribes that topic.

arguments

* *topic* `string` - name of topic to waiting for a message .

<br/>
**microgear.unsubscribe(topic);** cancel subscription

arguments

* *topic* `string` -  name of the topic to which to don't wait for a message.

<br/>
**microgear.setalias(name);**  microgear can set its own alias, which to be used for others make a function call chat(). The alias will appear on the key management portal of netpie.io .

arguments

* *name* `string` - name of this microgear.

<br/>
**microgear.chat(name,message);** sending a message to a specified gearname

arguments

* *name* `string` - name of microgear to which to send a message.
* *message* `string` - message to be sent.

<br/>
**microgear.disconnect();** Disconnect to NETPIE.


###Events
-----------
An application that runs on a microgear is an event-driven type, which responses to various events with the callback function in a form of event function call.

```java
EventListener eventListener = new EventListener();
```

<br/>
**Event: 'connected'** This event is created when the microgear library successfully connects to the NETPIE platform.


```java
eventListener.setConnectEventListener(new EventListener.OnServiceConnect() {
	@Override
   	public void onConnect(Boolean status) {
		if(status == true){
			Log.i("Connected","Now I'm connected with netpie");
		}
		else{
			Log.i("NotConnect","Can't connect to netpie")
		}
	}
});
```

arguments

* *status* `boolean` - status connection 

<br/>
**Event: 'closed'** This event is created when the microgear library disconnects the NETPIE platform.

```java
eventListener.setDisconnectEventListener(new EventListener.OnClose() {
   	@Override
   	public void onDisconnect(Boolean status) {
       		Log.i("Disconnect","Disconnect")
   	}
});
```

arguments

* *status* `boolean` - status disconnection

<br/>
**Event: 'message'** When there is an incoming message, this event is created with the related information to be sent via the callback function.

```java
eventListener.setMessageEventListener(new EventListener.OnMessageReceived() {
   	public void onMessage(String topic, String message) {
		Log.i("Message",topic+" "+message)
   	}
});
```

arguments

* *topic* `string` - topic of message 
* *message* `string` - message received 

<br/>
**Event: 'present'** This event is created when there is a microgear under the same appid appears online to connect to NETPIE.

```java
eventListener.setPresentEventListener(new EventListener.OnPresent() {
   	public void onPresent(String name) {
		Log.i("Present",name+ "become online");
  	}
});
```

arguments

*name* `string` - Name of microgear under the same appid appears online

<br/>
**Event: 'absent'** This event is created when the microgear under the same appid appears offline.

```java
eventListener.setAbsentEventListener(new EventListener.OnAbsent() {
   	public void onAbsent(String name) {
		Log.i("Absent",name+ "become offline");
   	}
});
```

arguments

* *name* `string` - Name of microgear under the same appid appears offline

<br/>
**Event: 'error'** This event is created when an error occurs within a microgear.

```java
eventListener.setOnException(new EventListener.OnException() {
   	public void onException(String error) {
		Log.i("Error",error);
   	}
});
```

arguments

* *error* `string` - Log error message











