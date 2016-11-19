#Microgear-android
-----------
microgear-android คือ client library สำหรับ Android Studio ที่ทำหน้าที่เป็นตัวกลางในการเชื่อมโยง application code เข้ากับบริการของ netpie platform เพื่อการพัฒนา IOT application รายละเอียดเกี่ยวกับ netpie platform สามารถศึกษาได้จาก http://netpie.io

##การติดตั้ง
-----------
สามารถเรียกใช้เวอร์ชั่นล่าสุดจาก Jcenter โดยใช้ Gradle 
```java
compile 'io.netpie:microgear:1.0.5'
```
<br/>

##ตัวอย่างการใช้งาน
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
    }
}

```

##การใช้งาน Library
-----------
###Microgear
-----------

**microgear.connect(appid, key, secret);**  การ Connect สำหรับเริ่มต้นการทำงาน microgear จะต้องทำการเชื่อมต่อไปที่ NETPIE

arguments

* *appid* `string` - คือกลุ่มของ application ที่ microgear จะทำการเชื่อมต่อ
* *key* `string` - เป็น key สำหรับ gear ที่จะรัน ใช้ในการอ้างอิงตัวตนของ gear
*  *secret* `string` - เป็น secret ของ key ซึ่งจะใช้ประกอบในกระบวนการยืนยันตัวตน

<br/>
**microgear.publish(topic, message,qos,retained);**  การ Publish ในกรณีที่ต้องการส่งข้อความแบบไม่เจาะจงผู้รับ สามารถใช้ฟังชั่น publish ไปยัง topic ที่กำหนดได้ ซึ่งจะมีแต่ microgear ที่ subscribe topoic นี้เท่านั้น ที่จะได้รับข้อความ

arguments

* *topic* `string` - ชื่อของ topic ที่ต้องการจะส่งข้อความไปถึง
* *message* `string` - ข้อความ
* *qos* `int` - การจอง Resource ในระบบ
* *retained* `Boolean` - ให้ retain ข้อความไว้หรือไม่ default เป็น false

<br/>
**microgear.subscribe(topic);**  การ Subscribe ในการทำงาน microgear อาจจะมีความสนใจ topic ใดเป็นการเฉพาะ เราสามารถใช้ฟังก์ชั่น subscribe() ในการบอกรับ message ของ topic นั้นได้ และหาก topic นั้นเคยมีการ retain ข้อความไว้ microgear จะได้รับข้อความนั้นทุกครั้งที่ subscribe topic

arguments

* *topic* `string` - ชื่อของ topic ที่ต้องการจะรอรับข้อความ

<br/>
**microgear.unsubscribe(topic);**  การยกเลิกการ subscribe

arguments

* *topic* `string` - ชื่อของ topic ที่ไม่ต้องการจะรอรับข้อความ

<br/>
**microgear.setalias(name);**  การตั้งชื่อ โดย microgear สามารถตั้งนามแฝงของตัวเองได้ ซึ่งสามารถใช้เป็นชื่อให้คนอื่นเรียกในการใช้ฟังก์ชั่น chat() และชื่อที่ตั้งในโค้ด จะไปปรากฎบนหน้าจัดการ key บนเว็บ netpie.io อย่างอัตโนมัติ

arguments

* *name* `string` - ชื่อของ microgear นี้

<br/>
**microgear.chat(name,message);** การ chat โดย microgear สามารถส่งข้อความแบบเจาะจงถึงผู้รับด้วยการระบุ name ที่ microgear ตัวอื่นเคย setalias ไว้

arguments

* *name* `string` - ชื่อของ microgear ที่ต้องการจะส่งข้อความไปถึง
* *message* `string` - ข้อความ

<br/>
**microgear.disconnect();** การ Disconnect หยุดเชื่อมต่อกับ netpie


###Events
-----------
Application ที่รันบน microgear จะมีการทำงานในแบบ event driven คือเป็นการทำงานตอบสนองต่อ event ต่างๆ ด้วยการเขียน callback function ขึ้นมารองรับในลักษณะนี้

```java
MicrogearCallBack callback = new MicrogearCallBack();
```

<br/>
**Event: 'connected'**อีเว้นท์นี้ จะเกิดขึ้นเมื่อ microgear เชื่อมต่อกับ netpie สำเร็จ

```java
public void onConnect() {
    Log.i("Connected","Now I'm connected with netpie");
}
```

<br/>
**Event: 'disconnect'** อีเว้นท์นี้จะเกิดขึ้นเมื่อ microgear ตัดการเชื่อมต่อกับ netpie

```java
public void onDisconnect() {
    Log.i("disconnect","Disconnected");
}
```

<br/>
**Event: 'message'** อีเว้นท์นี้จะเกิดเมื่อมี message เข้ามา จะเกิด event นี้ขึ้น พร้อมกับส่งผ่านข้อมูลเกี่ยวกับ message นั้นมาทาง argument ของ callback function

```java
public void onMessage(String topic, String message) {
    Log.i("Message",topic+" : "+message);
}
```

arguments

* *topic* `string` - หัวข้อของข้อความที่เข้ามา
* *message* `string` - ข้อความที่เข้ามา

<br/>
**Event: 'present'** อีเว้นท์นี้จะเกิดขึ้นเมื่อมี microgear ใน appid เดียวกัน online เข้ามาเชื่อมต่อ netpie

```java
public void onPresent(String token) {
    Log.i("present","New friend Connect :"+token);
}
```

arguments

*name* `string` - ชื่อของ microgear ใน appid เดียวกันที่ online

<br/>
**Event: 'absent'** อีเว้นท์นี้จะเกิดขึ้นเมื่อมี microgear ใน appid เดียวกัน offline หายไป

```java
epublic void onAbsent(String token) {
    Log.i("absent","Friend lost :"+token);
}
```

arguments

* *name* `string` - ชื่อของ microgear ใน appid เดียวกันที่ offline

<br/>
**Event: 'error'** อีเว้นท์นี้จะเกิดขึ้นเมื่อมี error ขึ้นภายใน microgear

```java
public void onError(String error) {
    Log.i("exception","Exception : "+error);
}
```

arguments

* *error* `string` - ข้อความที่แสดง error
