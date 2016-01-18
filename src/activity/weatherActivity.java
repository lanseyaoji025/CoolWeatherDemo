package activity;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import com.example.android_coolweather.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class weatherActivity extends Activity {

	private LinearLayout weatherInfoLayout;
	//������ʾ������
	private TextView cityNameText;
	//������ʾ����ʱ��
	private TextView publishText;
	//������ʾ������������Ϣ
	private TextView  weatherDespText;
	//������ʾ����1
	private TextView temp1Text;
	//������ʾ����2
	private TextView temp2Text;
	//������ʾ��ǰ������
	private TextView currentDateText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout=(LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText=(TextView) findViewById(R.id.city_name);
		publishText=(TextView) findViewById(R.id.publish_text);
		weatherDespText=(TextView) findViewById(R.id.weather_desp);
		temp1Text=(TextView) findViewById(R.id.temp1);
		temp2Text=(TextView) findViewById(R.id.temp2);
		currentDateText=(TextView) findViewById(R.id.current_date);
		String countyCode=getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			//���ؼ����ž�ȥ��ѯ����
			publishText.setText("ͬ����....");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else {
			//û���ؼ����ž�ֱ����ʾ��������
			showWeather();
		}
	}

	private void showWeather() {
		SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(pres.getString("city_name", ""));
		temp1Text.setText(pres.getString("temp1", ""));
		temp2Text.setText(pres.getString("temp1", ""));
		weatherDespText.setText(pres.getString("weather_desp", ""));
		publishText.setText(pres.getString("publish_time", "")+"����");
		currentDateText.setText(pres.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}

	/**
	 *��ѯ�ؼ����� ����Ӧ������
	 */
	private void queryWeatherCode(String countyCode) {
		String address ="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}

	/**
	 *��ѯ������������Ӧ������
	 */
	private void queryWeatherInfo(String weatherCode){
		String address ="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address, "weatherCode");
	}
	/**
	 *���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ��
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinsh(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//�ӷ��������ص������н�������������
						String [] array=response.split("\\|");
						if (array!=null&&array.length==2) {
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}
					}else if ("weatherCode".equals(type)) {
						//������������ص�������Ϣ
						Utility.handleWeatherResponse(weatherActivity.this, response);

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showWeather();
							}
						});
					}
				}
			}

			@Override
			public void Error(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}
}
