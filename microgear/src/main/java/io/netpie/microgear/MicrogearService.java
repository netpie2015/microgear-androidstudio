package io.netpie.microgear;


import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


public class MicrogearService extends Service {
    private static boolean serviceRunning = false;
    private static String status = "1";
    private static MQTTConnection connection = null;
    private final Messenger clientMessenger = new Messenger(new ClientHandler());
    private static String _username = "", _password = "", _clientid = "", appid1 = "", key1, secret1, path1;
    private MqttClient client ;

    public void onCreate() {
        super.onCreate();
        try {
            connection = new MQTTConnection();
        } catch (IllegalArgumentException e) {
            Microgear.microgeareventListener.onError("Check format App id ,key and Secret");
        }

    }

    public void setValue(String username, String pass, String clientid, String appid, String key, String secret, String path) {
        _username = username;
        _password = pass;
        _clientid = clientid;
        appid1 = appid;
        key1 = key;
        secret1 = secret;
        path1 = path;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isRunning()) {
            return START_STICKY;
        }

        super.onStartCommand(intent, flags, startId);
        /*
         * Start the MQTT Thread.
		 */
        try {
            connection.start();
        } catch (NullPointerException e) {
            Microgear.microgeareventListener.onError("Error Check appid,appkey or appsecret");
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            connection.end();
        } catch (NullPointerException e) {

        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Return a reference to our client handler.
		 */
        return clientMessenger.getBinder() ;
    }

    public synchronized static boolean isRunning() {
         /*
          * Only run one instance of the service.
		  */
        if (serviceRunning == false) {
            serviceRunning = true;
            return false;
        } else {
            return true;
        }
    }


    /*
      * These are the supported messages from bound clients
      */
    public static final int REGISTER = 0;
    public static final int SUBSCRIBE = 1;
    public static final int PUBLISH = 2;
    public static final int UNSUBSCRIBE = 6;
    public static final int SETNAME = 7;
    public static final int CHAT = 8;
    public static final int PUBLISHRE = 9;
    /*
      * Fixed strings for the supported messages.
      */
    public static final String TOPIC = "topic";
    public static final String MESSAGE = "message";
    public static final String RETAIN = "retain";
    public static final String QOS = "qos";
    public static final String STATUS = "status";
    public static final String CLASSNAME = "classname";
    public static final String INTENTNAME = "intentname";

    /*
      * This class handles messages sent to the service by
      * bound clients.
      */
    class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            boolean status = false;

            switch (msg.what) {
                case SUBSCRIBE:
                case PUBLISH:
                case UNSUBSCRIBE:
                case SETNAME:
                case CHAT:
                case PUBLISHRE:
                        /*
                         * These two requests should be handled by
           		 	 * the connection thread, call makeRequest
           		 	 */
                    connection.makeRequest(msg);
                    break;
                case REGISTER: {
                    Bundle b = msg.getData();
                    if (b != null) {
                        Object target = b.getSerializable(CLASSNAME);
                        if (target != null) {
                         /*
                          * This request can be handled in-line
        				  * call the API
        				  */
                            connection.setPushCallback((Class<?>) target);
                            status = true;
                        }
                        CharSequence cs = b.getCharSequence(INTENTNAME);
                        if (cs != null) {
                            String name = cs.toString().trim();
                            if (name.isEmpty() == false) {
                             /*
                              * This request can be handled in-line
            				  * call the API
            				  */
                                try {
                                    connection.setIntentName(name);
                                    status = true;
                                } catch (NullPointerException e) {

                                }

                            }
                        }
                    }
                    ReplytoClient(msg.replyTo, msg.what, status);
                    break;
                }
            }
        }
    }

    public void ReplytoClient(Messenger responseMessenger, int type, boolean status) {
         /*
          * A response can be sent back to a requester when
		  * the replyTo field is set in a Message, passed to this
		  * method as the first parameter.
		  */
        if (responseMessenger != null) {
            Bundle data = new Bundle();
            data.putBoolean(STATUS, status);
            Message reply = Message.obtain(null, type);
            reply.setData(data);

            try {
                responseMessenger.send(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    enum CONNECT_STATE {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }


    public class MQTTConnection extends Thread {
        public Class<?> launchActivity = null;
        public String intentName = null;
        public MsgHandler msgHandler = null;
        public static final int STOP = PUBLISH + 1;
        public static final int CONNECT = PUBLISH + 2;
        public static final int RESETTIMER = PUBLISH + 3;
        public CONNECT_STATE connState = CONNECT_STATE.DISCONNECTED;

        MQTTConnection() {
            msgHandler = new MsgHandler();
            msgHandler.sendMessage(Message.obtain(null, CONNECT));
        }


        public void end() {
            msgHandler.sendMessage(Message.obtain(null, STOP));
        }

        public void makeRequest(Message msg) {
            /*
             * It is expected that the caller only invokes
			 * this method with valid msg.what.
			 */
            msgHandler.sendMessage(Message.obtain(msg));
        }

        public void setPushCallback(Class<?> activityClass) {
            launchActivity = activityClass;
        }

        public void setIntentName(String name) {
            intentName = name;
        }

        public class MsgHandler extends Handler implements MqttCallback {
            public final String HOST = "gb.netpie.io"; //mygear3
            public final int PORT = 1883;
            public final String uri = "tcp://" + HOST + ":" + PORT;
            public final int MINTIMEOUT = 2000;
            public int timeout = MINTIMEOUT; // timeout = 2000
            public MqttConnectOptions options = new MqttConnectOptions();
            public Vector<String> topics = new Vector<String>();
            public ArrayList<Microgear.Publish> PublishList;
            public ArrayList<String> SubscribeList;
            public ArrayList<String> UnsubscribeList;
            public String Namedrive = null;
            public int qos = 0;

            public MsgHandler() {
                options.setCleanSession(true);
                options.setUserName(_username);
                options.setPassword(_password.toCharArray());
                try {
                    client = new MqttClient(uri, _clientid, null);
                    client.setCallback(this);
                } catch (MqttException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            public void ReSubcribeAndPublish() {
                SubscribeList = Microgear.SubscribeList;
                PublishList = Microgear.PublishList;
                UnsubscribeList = Microgear.UnsubscribeList;
                Namedrive = Microgear.Namedrive;
                if (Namedrive != null) {
                    setalias(Namedrive);
                }
                for (String i : SubscribeList) {
                    subscribe(i);
                }
                for (Microgear.Publish i : PublishList) {
                    publish(i.Topic, i.Message);
                }
                for (String i : UnsubscribeList) {
                    unsubscribe(i);
                }
            }

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case STOP: {
                    /*
                     * Clean up, and terminate.
					 */
                        client.setCallback(null);
                        if (client.isConnected()) {
                                DisconnectThread Disconnect = new DisconnectThread();
                                Thread DisconnectThread = new Thread(Disconnect);
                                DisconnectThread.start();
                                //client.disconnect();
                                //client.close();
                                //EventListener.onDisconnect();

                        }
                        break;
                    }
                    case CONNECT: {

                        if (connState != CONNECT_STATE.CONNECTED) {
                            try {
                                client.connect(options);
                                connState = CONNECT_STATE.CONNECTED;
                                Microgear.microgeareventListener.onConnect();
                                subscribe("&present");
                                subscribe("&absent");
                                ReSubcribeAndPublish();
                                this.sendMessageDelayed(Message.obtain(null, CONNECT), timeout);
                            } catch (MqttException e) { // if connect fail

                                if (e.getReasonCode() == 0) {
                                    Microgear.microgeareventListener.onError("No Internet connection");
                                    this.sendMessageDelayed(Message.obtain(null, CONNECT), timeout);
                                } else if (e.getReasonCode() == 5) {
                                    Microgear.microgeareventListener.onError("Thing is disable");
                                    this.sendMessageDelayed(Message.obtain(null, CONNECT), timeout);
                                } else if (e.getReasonCode() == 4) {
                                    Microgear.brokereventListener.reconnect();
                                    Microgear.microgeareventListener.onError("Invalid credential");
                                    //Microgear microgear = new Microgear(getApplicationContext());
                                    //microgear.
                                    //microgear.connect(Microgear.appidvalue, Microgear.keyvalue, Microgear.secretvalue);
                                }
                                return;
                            }

					    /*
					     * Re-subscribe to previously subscribed topics
					     */
                            Iterator<String> i = topics.iterator();
                            while (i.hasNext()) {
                                subscribe(i.next());
                            }
                        }
                        break;
                    }
                    case RESETTIMER: {
                        timeout = MINTIMEOUT;
                        break;
                    }
                    case SUBSCRIBE: {
                        boolean status = false;
                        Bundle b = msg.getData();
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                String topic = cs.toString().trim();
                                if (topic.isEmpty() == false) {
                                    status = true;
                                    subscribe(topic);
	        					/*
	        					 * Save this topic for re-subscription if needed.
	        					 */
                                    if (status) {
                                        topics.add("&present");
                                        topics.add("&absent");
                                        topics.add(topic);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case PUBLISH: {
                        Bundle b = msg.getData();
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                String topic = cs.toString().trim();
                                if (topic.isEmpty() == false) {
                                    cs = b.getCharSequence(MESSAGE);
                                    if (cs != null) {
                                        String message = cs.toString().trim();
                                        if (message.isEmpty() == false) {
                                            publish(topic, message);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case UNSUBSCRIBE: {
                        boolean status;
                        Bundle b = msg.getData();
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                String topic = cs.toString().trim();
                                if (topic.isEmpty() == false) {
                                    status = true;
                                    unsubscribe(topic);
	        					/*
	        					 * Save this topic for re-subscription if needed.
	        					 */
                                    if (status) {
                                        topics.remove("&present");
                                        topics.remove("&absent");
                                        topics.remove(topic);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case SETNAME: {
                        Bundle b = msg.getData();
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                String namedevice = cs.toString().trim();
                                if (namedevice.isEmpty() == false) {
                                    setalias(namedevice);
                                }
                            }
                        }
                        break;
                    }
                    case CHAT: {
                        Bundle b = msg.getData();
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                String namedevice = cs.toString().trim();
                                if (namedevice.isEmpty() == false) {
                                    cs = b.getCharSequence(MESSAGE);
                                    if (cs != null) {
                                        String message = cs.toString().trim();
                                        if (message.isEmpty() == false) {
                                            chat(namedevice, message);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case PUBLISHRE: {
                        Bundle b = msg.getData();
                        if (b != null) {
                            CharSequence cs = b.getCharSequence(TOPIC);
                            if (cs != null) {
                                String topic = cs.toString().trim();
                                if (topic.isEmpty() == false) {
                                    cs = b.getCharSequence(MESSAGE);
                                    Boolean re = b.getBoolean(RETAIN);
                                    if (cs != null) {
                                        String message = cs.toString().trim();
                                        if (message.isEmpty() == false) {
                                            publish(topic, message, re);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }


            public void subscribe(String topic) {

                SubscribeThread Subcribe = new SubscribeThread("/" + appid1 + "/" + topic);
                Thread SubscribeThread = new Thread(Subcribe);
                SubscribeThread.start();
                //client.subscribe("/" + appid1 + "/" + topic);
                //eventListener.mError.onException("/" + appid1 + "/" + topic);

            }


            public void setalias(String namedevice) {
                String msg = "";
                MqttMessage message = new MqttMessage();
                message.setQos(qos);
                message.setPayload(msg.getBytes());
                PublishThread publish = new PublishThread("/" + appid1 + "/" + "@setalias/" + namedevice, message);
                Thread PublishThread = new Thread(publish);
                PublishThread.start();
                //client.publish("/" + appid1 + "/" + "@setalias/" + namedevice, message);
                //eventListener.mError.onException("Setname Complete");

            }

            public void chat(String namedevice, String msg) {

                MqttMessage message = new MqttMessage();
                message.setQos(qos);
                message.setPayload(msg.getBytes());
                PublishThread publish = new PublishThread("/" + appid1 + "/" + "gearname/" + namedevice, message);
                Thread PublishThread = new Thread(publish);
                PublishThread.start();
                //client.publish("/" + appid1 + "/" + "gearname/" + namedevice, message);
                //eventListener.mError.onException("Chat Complete");

            }

            public void publish(String topic, String msg) {

                MqttMessage message = new MqttMessage();
                message.setQos(qos);
                message.setPayload(msg.getBytes());
                PublishThread publish = new PublishThread("/" + appid1 + "/" + topic, message);
                Thread PublishThread = new Thread(publish);
                PublishThread.start();
                //client.publish("/" + appid1 + "/" + topic, message);
                //eventListener.mError.onException("Publish Complete");


            }

            public void publish(String topic, String msg, Boolean retain) {

                MqttMessage message = new MqttMessage();
                message.setQos(qos);
                message.setPayload(msg.getBytes());
                PublishThread publish = new PublishThread("/" + appid1 + "/" + topic, message, retain);
                Thread PublishThread = new Thread(publish);
                PublishThread.start();
                //client.publish("/" + appid1 + "/" + topic, message.getPayload(), qos, retain);
                //eventListener.mError.onException("Publish Complete");
            }

            public void unsubscribe(String topic) {
                UnsubscribeThread Unsubcribe = new UnsubscribeThread("/" + appid1 + "/" + topic);
                Thread UnsubscribeThread = new Thread(Unsubcribe);
                UnsubscribeThread.start();
                //client.unsubscribe("/" + appid1 + "/" + topic);
                //eventListener.mError.onException("UnSubscribe Complete");
            }


            public void connectionLost(Throwable arg0) {
                Microgear.microgeareventListener.onError("connection Lost");
                connState = CONNECT_STATE.DISCONNECTED;
                status = "2";
                sendMessageDelayed(Message.obtain(null, CONNECT), timeout);
            }


            public void deliveryComplete(IMqttDeliveryToken arg0) {

            }

            public void messageArrived(String topic, MqttMessage message) throws Exception {
                int pre = topic.indexOf("&present");
                int ab = topic.indexOf("&absent");
                int error = topic.indexOf("@error");
                int info = topic.indexOf("@info");
                if (pre != -1) {
                    Microgear.microgeareventListener.onPresent(message + "");
                }
                else if (ab != -1) {
                    Microgear.microgeareventListener.onAbsent(message + "");
                }
                else if (error != -1) {
                    Microgear.microgeareventListener.onError(message + "");
                }
                else if (info != -1) {
                    Microgear.microgeareventListener.onInfo(message + "");
                }
                else {
                    Microgear.microgeareventListener.onMessage(topic, message + "");
                }

            }


        }

    }

    private class PublishThread extends Thread {
        String Topic;
        MqttMessage Message;
        boolean Retainde = false;

        public void run() {
            if (Retainde) {
                try {
                    client.publish(this.Topic, this.Message.getPayload(), 0, Retainde);
                } catch (MqttPersistenceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                try {
                    client.publish(this.Topic, this.Message);
                } catch (MqttPersistenceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

        public PublishThread(String topic, MqttMessage message, boolean retainde) {
            this.Topic = topic;
            this.Message = message;
            this.Retainde = retainde;
        }

        public PublishThread(String topic, MqttMessage message) {
            this.Topic = topic;
            this.Message = message;
        }
    }

    private class DisconnectThread extends Thread {
        public void run() {
            try {
                client.disconnect();
                Microgear.microgeareventListener.onDisconnect();
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private class SubscribeThread extends Thread {
        String Topic;

        public void run() {
            try {
                client.subscribe(Topic);
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public SubscribeThread(String topic) {
            this.Topic = topic;
        }
    }

    private class UnsubscribeThread extends Thread {
        String Topic;

        public void run() {
            try {
                client.unsubscribe(Topic);
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public UnsubscribeThread(String topic) {
            this.Topic = topic;
        }
    }

}
