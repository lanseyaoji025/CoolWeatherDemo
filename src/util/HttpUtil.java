package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpConnection;

public class HttpUtil {
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener){

		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpURLConnection connection=null;
				try {
					URL url=new URL(address);
					connection= (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in=connection.getInputStream();
					BufferedReader reader=new BufferedReader(new InputStreamReader(in));
					StringBuilder sb=new StringBuilder();
					String line ;
					while ((line=reader.readLine())!=null) {
						sb.append(line);
					}
					if (listener!=null) {
						listener.onFinsh(sb.toString());
					}
				} catch (Exception e) {
					listener.Error(e);
				}finally{
					if (connection!=null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}
}
