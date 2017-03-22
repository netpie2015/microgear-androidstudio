#Microgear-android
-----------
microgear-android is a client library for Android Studio.The library is used to connect application code with the NETPIE Platform's service for developing IoT applications. For more details on the NETPIE Platform, please visit https://netpie.io .

##Installation
-----------
Refer to the latest version directly from the Jcenter using this Gradle 
```java
compile 'io.netpie:microgear:1.1.1'
```
<br/>




##Usage Example
-----------
```java

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import io.netpie.microgear.Microgear;
import io.netpie.microgear.MicrogearEventListener;

public class MainActivity extends Activity {

    private Microgear microgear = new Microgear(this);
    private String appid = <APPID>; //APP_ID
    private String key = <KEY>; //KEY
    private String secret = <SECRET>; //SECRET
    private String alias = "android";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString("myKey");
            TextView myTextView =
                    (TextView)findViewById(R.id.textView);
            myTextView.append(string+"\n");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MicrogearCallBack callback = new MicrogearCallBack();
        microgear.connect(appid,key,secret,alias);
        microgear.setCallback(callback);
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

        @Override
        public void onInfo(String info) {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("myKey", "Exception : "+info);
            msg.setData(bundle);
            handler.sendMessage(msg);
            Log.i("info","Info : "+info);
        }
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

<br/>
**microgear.wrtieFeed(*feedid*, *data*, *apikey*):** write time series data to a feed storage

arguments

* *feedid* `string` - name of the feed
* *data* `jsonobject` – data JSONObject
* *apikey* `string` - apikey for authorization. If apikey is not specified, you will need to allow the AppID to access feed and then the default apikey will be assigned automatically.


###Events
-----------
An application that runs on a microgear is an event-driven type, which responses to various events with the callback function in a form of event function call.

```java
MicrogearCallBack callback = new MicrogearCallBack();
```

<br/>
**Event: 'connected'** This event is created when the microgear library successfully connects to the NETPIE platform.

```java
public void onConnect() {
    Log.i("Connected","Now I'm connected with netpie");
}
```

<br/>
**Event: 'disconnect'** This event is created when the microgear library disconnects the NETPIE platform.

```java
public void onDisconnect() {
    Log.i("disconnect","Disconnected");
}
```

<br/>
**Event: 'message'** When there is an incoming message, this event is created with the related information to be sent via the callback function.

```java
public void onMessage(String topic, String message) {
    Log.i("Message",topic+" : "+message);
}
```

arguments

* *topic* `string` - topic of message
* *message* `string` - message received

<br/>
**Event: 'present'** This event is created when there is a microgear under the same appid appears online to connect to NETPIE.

```java
public void onPresent(String token) {
    Log.i("present","New friend Connect :"+token);
}
```

arguments

*name* `string` - Name of microgear under the same appid appears online

<br/>
**Event: 'absent'** This event is created when the microgear under the same appid appears offline.

```java
public void onAbsent(String token) {
    Log.i("absent","Friend lost :"+token);
}
```

arguments

* *name* `string` - Name of microgear under the same appid appears offline

<br/>
**Event: 'error'** This event is created when an error occurs within a microgear.

```java
public void onError(String error) {
    Log.i("exception","Exception : "+error);
}
```

arguments

* *error* `string` - Log error message

<br/>
**Event: 'info'** อีเว้นท์นี้จะเกิดขึ้นเมื่อมี info ขึ้นภายใน microgear

```java
public void onInfo(String info) {
    Log.i("info","info : "+info);
}
```

arguments

* *info* `string` - ข้อความที่แสดง info
