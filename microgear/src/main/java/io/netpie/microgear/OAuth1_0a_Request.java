package io.netpie.microgear;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Encoder;

public class OAuth1_0a_Request {
    private String _consumerKey;
    private String _consumerSecret;
    static final String Seperator = ",";
    static final String version = "1.0";
    static final String Hmac = "HMAC-SHA1";
    static final String request_method = "POST";
    private String _authorize_callback;
    static final String Request_url = "http://ga.netpie.io:8080/api/rtoken";

    public String OAuth(String consumerKey, String consumerSecret, String authorize_callback) {
        this._consumerKey = consumerKey;
        try {
            this._consumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this._authorize_callback = authorize_callback;
        return getOAuthRequestToken();

    }

    public int _getTimestamp() {
        return (int) Math.floor((new Date()).getTime() / 1000);
    }

    private String _normaliseRequestParams(String[] headers_key, String[] headers_value) {
        String base = "";
        for (int i = 0; i < headers_value.length - 1; i++) {
            base += headers_key[i] + "=" + headers_value[i] + "&";
        }
        base = base.substring(0, base.length() - 1);
        return base;

    }

    public String _getSignature(String method, String url, String parameters) {
        String signatureBase = this._createSignatureBase(method, url, parameters);

        return this._createSignature(signatureBase);
    }

    public String _createSignatureBase(String method, String url, String parameters) {
        try {
            url = URLEncoder.encode(url, "UTF-8");
            parameters = URLEncoder.encode(parameters, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return method.toUpperCase() + "&" + url + "&" + parameters;
    }

    public String _createSignature(String signatureBase) {
        String tokenSecret = "";
        String key = this._consumerSecret + "&" + tokenSecret;
        String hash = "";
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] result = mac.doFinal(signatureBase.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            hash = encoder.encode(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return hash.toString();

    }

    static char[] NONCE_CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'};

    public static String _getNonce() {
        StringBuilder sb = new StringBuilder();
        new Random();
        for (int i = 0; i < 32; i++) {
            char c = NONCE_CHARS[(int) Math.floor(Math.random() * NONCE_CHARS.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }

    public String _prepareParameters(String method, String url) {
        String oauth_callback = "";
        try {
            oauth_callback = URLEncoder.encode(this._authorize_callback, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String timestamp = Integer.toString(_getTimestamp());
        String[] headers_key = {"oauth_callback", "oauth_consumer_key", "oauth_nonce", "oauth_signature_method",
                "oauth_timestamp", "oauth_version", "oauth_signature"};
        String[] headers_value = {oauth_callback, this._consumerKey, _getNonce(), Hmac, timestamp, version, ""};

        String sig;
        sig = _getSignature("POST", url, this._normaliseRequestParams(headers_key, headers_value));
        headers_value[0] = this._authorize_callback;
        headers_value[6] = sig;
        String authorization = this._buildAuthorizationHeaders(headers_key, headers_value);
        return authorization;

    }

    public String _buildAuthorizationHeaders(String[] headers_key, String[] headers_value) {
        String authHeader = "OAuth ";
        for (int i = 0; i < headers_value.length; i++) {
            try {
                authHeader += URLEncoder.encode(headers_key[i], "UTF-8") + "=\""
                        + URLEncoder.encode(headers_value[i], "UTF-8") + "\"" + Seperator;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        authHeader = authHeader.substring(0, authHeader.length() - 1);
        return authHeader;
    }

    public String _performSecureRequest(String method, String url, String extraParams) {
        String authorization = _prepareParameters(method, url);
        return authorization;
    }

    public String getOAuthRequestToken() {
        String extraParams = _authorize_callback;
        String token = this._performSecureRequest(request_method, Request_url, extraParams);
        return token;
    }

}
