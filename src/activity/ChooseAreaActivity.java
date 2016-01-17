package activity;

import java.util.ArrayList;
import java.util.List;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import com.example.android_coolweather.R;

import model.City;
import model.CoolWeatherDB;
import model.Country;
import model.Province;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTRY=2;

	private ProgressDialog progressDialog;
	private TextView titleView;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> datalist=new ArrayList<String>();

	//省列表
	private List<Province> provinceList;

	//市列表
	private List<City> cityList;

	//县列表
	private List<Country> countryList;

	//选中的省份
	private Province selectedProvince ;

	//选中的城市
	private City selectedCity;

	//当前选中的级别
	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_area);
		listView=(ListView) findViewById(R.id.list_view);
		titleView=(TextView) findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel==LEVEL_PROVINCE) {
					selectedProvince=provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(position);
					queryCounties();
				}
			}
		});
		queryProvinces();//加载省部数据
	}
	/**
	 * 查询全国所有的省，优先从数据库查询，如果没查询到再去服务器上查询。
	 */

	private void queryProvinces() {
		provinceList=coolWeatherDB.loadProvinces();
		Log.d("長度", provinceList.size()+"");
		if (provinceList.size()>0) {
			datalist.clear();
			for (Province province : provinceList) {
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleView.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}else {
			queryFromServer(null,"province");
		}
	}
	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final  String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else {
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinsh(String response) {
				boolean result=false;
				if ("province".equals(type)) {
					result=Utility.handleProvincesRespone(coolWeatherDB, response);
				}else if ("city".equals(type)) {
					result=Utility.handleCitiesRespone(coolWeatherDB, response, selectedProvince.getId());
				}else if ("country".equals(type)) {
					result=Utility.handleCountiesRespone(coolWeatherDB, response, selectedCity.getId());
				}
				if (result) {
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							}else if ("city".equals(type)) {
								queryCities();
							}else if ("country".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void Error(Exception e) {
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加載失敗", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void showProgressDialog() {
		if (progressDialog==null) {
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加載。。。");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		
		progressDialog.show();
	}


	private void closeProgressDialog() {
		progressDialog.dismiss();
	}
	/**
	 * 查询全国所有的市，优先从数据库查询，如果没查询到再去服务器上查询。
	 */
	private void queryCities() {
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size()>0) {
			datalist.clear();
			for (City city:cityList) {
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleView.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}

	/**
	 * 查询全国所有的县，优先从数据库查询，如果没查询到再去服务器上查询。
	 */
	private void queryCounties() {
		countryList=coolWeatherDB.loadCountries(selectedCity.getId());
		if (countryList.size()>0) {
			datalist.clear();
			for (Country country:countryList) {
				datalist.add(country.getCountryName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleView.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_COUNTRY;
		}else {
			queryFromServer(selectedCity.getCityCode(),"city");
		}
	}

		@Override
		public void onBackPressed() {
			if (currentLevel==LEVEL_COUNTRY) {
				queryCities();
			}else if (currentLevel==LEVEL_CITY) {
				queryProvinces();
			}else {
				finish();
			}
		}

}
