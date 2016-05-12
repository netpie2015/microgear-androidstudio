#Microgear-android -----------microgear-android is a client library for Android Studio.The library is used to connect application code with the NETPIE Platform's service for developing IoT applications. For more details on the NETPIE Platform, please visit https://netpie.io .##Installation-----------Import .arr File in Android Projecthttps://docs.google.com/presentation/d/1TTYzLZZ8Vmr3muGi5RjSdm8UYzCIkBBPxhpOWaLqfoU/edit#slide=id.g12797aea62_0_0https://docs.google.com/presentation/d/1q2qfbzFQpywMap_bHpQawKS-zRampCZTeMPuhWta8Jg/edit?usp=sharing##Usage Example-----------```javaMicrogear microgear = new Microgear(this);EventListener eventListener = new EventListener();String appid = "APPID";String key = "KEY";String secret = "SECRET";microgear.connect(appid,key,secret);microgear.setalias("Mygear");microgear.chat("Mygear","Hello World");eventListener.setConnectEventListener(new EventListener.OnServiceConnect() {   	public void onConnect(Boolean status) {		Log.i(“connect”,”Connected”);   	}});eventListener.setMessageEventListener(new EventListener.OnMessageReceived() {   	public void onMessage(String topic, String message) {       		Log.i("Message",topic+" : "+message);   	}});```##Library Usage-----------###Microgear-----------**microgear.connect(appid, key, secret);**  Connect to NETPIE with the appid, key and secret.arguments* *appid* `string` - an application that microgear will connect to.* *key* `string` - is used as a microgear identity.*  *secret* `string` - comes in a pair with key. The secret is used for authentication and integrity.<br/>**microgear.publish(topic, message,qos,retained);**  In the case that the microgear want to send a message to an unspecified receiver, the developer can use the function publish to the desired topic, which all the microgears that subscribe such topic will receive a message.arguments* *topic* `string` - name of topic to be send a message to.* *message* `string` - message to be sent.* *qos* `int` - Quality of Service* *retained* `Boolean` - retain a message or not (the default is false)<br/>**microgear.subscribe(topic);**  microgear may be interested in some topic. The developer can use the function subscribe() to subscribe a message belong to such topic. If the topic used to retain a message, the microgear will receive a message everytime it subscribes that topic.arguments* *topic* `string` - name of topic to waiting for a message .<br/>**microgear.unsubscribe(topic);** cancel subscriptionarguments* *topic* `string` -  name of the topic to which to don't wait for a message.<br/>**microgear.setalias(name);**  microgear can set its own alias, which to be used for others make a function call chat(). The alias will appear on the key management portal of netpie.io .arguments* *name* `string` - name of this microgear.<br/>**microgear.chat(name,message);** sending a message to a specified gearnamearguments* *name* `string` - name of microgear to which to send a message.* *message* `string` - message to be sent.<br/>**microgear.disconnect();** Disconnect to NETPIE.###Events-----------An application that runs on a microgear is an event-driven type, which responses to various events with the callback function in a form of event function call.```javaEventListener eventListener = new EventListener();```<br/>**Event: 'connected'** This event is created when the microgear library successfully connects to the NETPIE platform.```javaeventListener.setConnectEventListener(new EventListener.OnServiceConnect() {	@Override   	public void onConnect(Boolean status) {		if(status == true){			Log.i("Connected","Now I'm connected with netpie");		}		else{			Log.i("NotConnect","Can't connect to netpie")		}	}});```arguments* *status* `boolean` - status connection <br/>**Event: 'closed'** This event is created when the microgear library disconnects the NETPIE platform.```javaeventListener.setDisconnectEventListener(new EventListener.OnClose() {   	@Override   	public void onDisconnect(Boolean status) {       		Log.i("Disconnect","Disconnect")   	}});```arguments* *status* `string` - status disconnection<br/>**Event: 'message'** When there is an incoming message, this event is created with the related information to be sent via the callback function.```javaeventListener.setMessageEventListener(new EventListener.OnMessageReceived() {   	public void onMessage(String topic, String message) {		Log.i("Message",topic+" "+message)   	}});```arguments* *topic* `string` - topic of message * *message* `string` - message received <br/>**Event: 'present'** This event is created when there is a microgear under the same appid appears online to connect to NETPIE.```javaeventListener.setPresentEventListener(new EventListener.OnPresent() {   	public void onPresent(String name) {		Log.i("Present",name+ "become online");  	}});```arguments*name* `string` - Name of microgear under the same appid appears online<br/>**Event: 'absent'** This event is created when the microgear under the same appid appears offline.```javaeventListener.setAbsentEventListener(new EventListener.OnAbsent() {   	public void onAbsent(String name) {		Log.i("Absent",name+ "become offline");   	}});```arguments* *name* `string` - Name of microgear under the same appid appears offline<br/>**Event: 'error'** This event is created when an error occurs within a microgear.```javaeventListener.setOnException(new EventListener.OnException() {   	public void onException(String error) {		Log.i("Error",error);   	}});```arguments* *error* `string` - Log error message