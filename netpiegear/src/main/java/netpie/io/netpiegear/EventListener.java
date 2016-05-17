package netpie.io.netpiegear;

/**
 * Created by suparlerk on 5/2/2016 AD.
 */
public class EventListener {
    public static OnServiceConnect mConnect;
    public static OnClose mClose;
    public static OnMessageReceived mMessage;
    public static OnPresent mPresent;
    public static OnAbsent mAbsent;
    public static OnException mError;


    //============ event connect ===========================
    public interface OnServiceConnect {
        void onConnect(Boolean status);
    }

    public void setConnectEventListener(OnServiceConnect eventListener) {
        mConnect = eventListener;

    }

    //============= event disconnect =========================
    public interface OnClose {
        void onDisconnect(Boolean status);
    }

    public void setDisconnectEventListener(OnClose eventListener) {
        mClose = eventListener;
    }

    //============ event message ===========================
    public interface OnMessageReceived {
        void onMessage(String topic, String message);
    }

    public void setMessageEventListener(OnMessageReceived eventListener) {
        mMessage = eventListener;

    }

    //=========================event present===========================
    public interface OnPresent {
        void onPresent(String name);
    }

    public void setPresentEventListener(OnPresent eventListener) {
        mPresent = eventListener;

    }

    //=======================event absent ==============================
    public interface OnAbsent {
        void onAbsent(String name);
    }

    public void setAbsentEventListener(OnAbsent eventListener) {
        mAbsent = eventListener;
    }

    //==================== event exception ============================
    public interface OnException {
        void onException(String error);
    }

    public void setOnException(OnException eventListener) {
        mError = eventListener;

    }
}
