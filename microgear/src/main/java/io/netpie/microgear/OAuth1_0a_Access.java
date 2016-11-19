package io.netpie.microgear;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Encoder;

public class OAuth1_0a_Access {

	private String _consumerKey;
	private String _consumerSecret;
	private String Seperator = ",";
	private String method_request = "POST";
	public String version = "1.0";
	private String _signatureMethod = "HMAC-SHA1";
	static final String Access_url = "http://ga.netpie.io:8080/api/atoken";
	public JSONObject token_token_secret = new JSONObject();
	private String oauth_token;
	private String oauth_token_secret;
	public String Verifier = "NJS1a";

	public JSONObject OAuth(String consumerKey, String consumerSecret, String oauth_token, String oauth_token_secret) {
		this._consumerKey = consumerKey;
		this.oauth_token = oauth_token;
		try {
			this.oauth_token_secret = URLEncoder.encode(oauth_token_secret, "UTF-8");
			this._consumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getOAuthAccessToken(this.oauth_token , this.oauth_token_secret, Verifier);

	}

	public int _getTimestamp() {
		return (int) Math.floor((new Date()).getTime() / 1000);
	}

	private String _normaliseRequestParams(String[] headers_key, String[] headers_value) {
		String base = "";
		for (int i = 0; i < headers_value.length-1; i++) {
			base += headers_key[i] + "=" + headers_value[i] + "&";
		}
		base = base.substring(0, base.length() - 1);
		return base;

	}

	public String _getSignature(String method, String url, String parameters, String tokenSecret) {
		String signatureBase = this._createSignatureBase(method, url, parameters);
		return this._createSignature(signatureBase, tokenSecret);
	}

	public String _createSignatureBase(String method, String url, String parameters) {
		try {
			url = URLEncoder.encode(url, "UTF-8");
			parameters = URLEncoder.encode(parameters, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// System.out.println(method + "&" + url + "&" + parameters);
		return method.toUpperCase() + "&" + url + "&" + parameters;
	}

	public String _createSignature(String signatureBase, String tokenSecret) {
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

	static char[] NONCE_CHARS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
			'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9' };

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

	public String _prepareParameters(String oauth_token, String oauth_token_secret, String method, String url,
									 String extra_params) {

		String timestamp = Integer.toString(_getTimestamp());
		String[] headers_key = {"oauth_consumer_key", "oauth_nonce", "oauth_signature_method",
				"oauth_timestamp", "oauth_token", "oauth_verifier", "oauth_version" ,"oauth_signature"};
		String[] headers_value = {this._consumerKey, _getNonce(), _signatureMethod, timestamp,
				oauth_token, Verifier, version ,""};

		String sig;
		sig = _getSignature(method, url, this._normaliseRequestParams(headers_key, headers_value), oauth_token_secret);		
		headers_value[headers_value.length-1] = sig;
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

	public JSONObject getOAuthAccessToken(String oauth_token, String oauth_token_secret, String extraParams) {
		JSONObject token = this._performSecureRequest(oauth_token, oauth_token_secret, method_request, Access_url,
				extraParams);
		return token;

	}

	public JSONObject _performSecureRequest(String oauth_token, String oauth_token_secret, String method, String url,
											String extraParams) {
		String authorization = _prepareParameters(oauth_token, oauth_token_secret, method, url, extraParams);
		URL Url;
		try {
			Url = new URL(Access_url);
			URLConnection conn = Url.openConnection();
			((HttpURLConnection) conn).setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", authorization);
			conn.connect();
			int status = ((HttpURLConnection) conn).getResponseCode();

			if(status >= HttpURLConnection.HTTP_BAD_REQUEST) {
				Log.i(getClass().getCanonicalName(),"Error HTTP Code "+status);

			}

			if(status== HttpURLConnection.HTTP_OK) {
				InputStream is = conn.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = rd.readLine()) != null) {
					response.append(line);
					token_token_secret.put("", response);
				}
				rd.close();
			}
			return token_token_secret;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return token_token_secret;
	}

}
