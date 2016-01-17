package util;

public interface HttpCallbackListener  {
	void onFinsh(String response);
	void Error(Exception e);
}
