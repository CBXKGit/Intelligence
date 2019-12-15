package com.dyna.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MainActivity";

    public final static String[] info = { "运行正确", "运行错误", "操作超时", "缓冲区空间不足", "未找到信息", "操作过程被中断", "感应器上无手指", "验证指静脉失败",
            "未连接设备", "函数调用入参错误", "发送报文出错", "发送报文过长", "应答报文异或字错误", "应答报文校验和错误", "应答报文中的命令码错误", "接收应答数据错误",
            "命令应答中，反馈的错误命令数>0", "注册时重复手指", "认证时,手指中途离开","返回设备忙" };

	private DynaAPI dynaAPI = DynaAPI.getSingleInstance();// 指静脉设备的句柄
	private TextView text;
	private final static String userId = "1001";// 手指编码
	private final static int timeOut = 60000;// 超时时间
	private byte[] feature = new byte[0];// 特征值
	private static int touchTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		text = (TextView) findViewById(R.id.text);
		setBtnClickListener();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// 保存指静脉数据
	private void GetFingerFeatureforServerAuth() {
		setText("正在获取....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.GetFingerFeatureforServerAuth(timeOut);
				setText(info[status]);
			}
		}).start();
	}

	// 保存指静脉数据
	private void saveFeatureData() {
		setText("正在获取....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.saveFingerFeature(userId, feature, feature.length);
				setText(info[status]);
			}
		}).start();
	}

	// 获取指静脉数据
	private void getFeatureData() {
		setText("正在获取....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				feature = dynaAPI.getFingerFeature(userId);
				String string = feature == null ? "获取失败" : ByteUtils.byteToHex(feature, 0, feature.length);
				setText(string);
			}
		}).start();
	}

	// 关闭指静脉设备
	private void closeComm() {
		setText("正在连接....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.closeComm();
				setText(info[status]);
			}
		}).start();
	}

	// 连接指静脉设备
	private void connect() {
		setText("正在连接....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.ConnectDyna("/dev/ttyS3");
				Log.e("MainActivity", "status=" + status);
				setText(info[status]);
			}
		}).start();
	}

	// 删除用户
	private void deleteUser() {
		setText("正在删除....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.deleteUser(userId);
				setText(status == 0 ? "删除成功" : info[status]);
			}
		}).start();
	}

	// 删除所有用户
	private void ClearUser() {
		setText("正在删除....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.ClearUsers();
				setText(status == 0 ? "复位成功,请等待滴滴二声..." : info[status]);
			}
		}).start();
	}

	// 验证指静脉 1:N
	private void auth(final boolean AutyType) {
		setText("正在验证....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (AutyType) { // 1:N 验证
					String userId = dynaAPI.AuthUser(timeOut);
					setText(userId == null ? "验证失败" : userId);
				} else {// 1:1 验证
					int status = dynaAPI.Auth1by1User(userId, timeOut);
					setText(status == 0 ? "验证成功" : info[status]);
				}
			}
		}).start();
	}

	// 注册指静脉
	private void RegeistUser() {
		setText("正在注册....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.RegistUser(userId, timeOut);
				setText(status == 0 ? "注册成功" : info[status]);
			}
		}).start();
	}

	// 中止注册
	private void RegeistStop() {
		setText("正在停止....");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status = dynaAPI.RegistStop();
				setText(status == 0 ? "中止成功" : info[status]);
			}
		}).start();
	}

	
	// 显示指静脉内容
	private void setText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				text.setText(str);
			}
		});
	}

	protected void startActivity(Class<?> c) {
		Intent intent = new Intent(this, c);
		startActivity(intent);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setBtnClickListener() {

		this.findViewById(R.id.test).setOnClickListener(this);
		this.findViewById(R.id.regeist).setOnClickListener(this);

		this.findViewById(R.id.auth).setOnClickListener(this);
		this.findViewById(R.id.delUser).setOnClickListener(this);

		this.findViewById(R.id.delAllUsers).setOnClickListener(this);
		this.findViewById(R.id.regeistStop).setOnClickListener(this);

		this.findViewById(R.id.disConnect).setOnClickListener(this);
		this.findViewById(R.id.getFeature).setOnClickListener(this);

		this.findViewById(R.id.saveFeature).setOnClickListener(this);
		this.findViewById(R.id.onebyoneauth).setOnClickListener(this);
		this.findViewById(R.id.severAuthFinger).setOnClickListener(this);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			touchTime++;
			if (touchTime % 2 == 0) {
				dynaAPI.closeComm();
				touchTime = 0;
			}
			return false;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.test:// 连接用户
			connect();
			break;

		case R.id.regeist:// 注册用户

			RegeistUser();
			break;

		case R.id.auth:// 1:N 验证 用户
			auth(true);
			break;

		case R.id.delUser:// 删除用户
			deleteUser();
			break;

		case R.id.delAllUsers: // 删除所有用户
			ClearUser();
			break;

		case R.id.regeistStop:// 中断注册
			RegeistStop();
			break;

		case R.id.disConnect:// 关闭串口
			closeComm();
			break;

		case R.id.getFeature: // 获取指静脉数据
			getFeatureData();
			break;

		case R.id.saveFeature:// 保存指静脉数据
			saveFeatureData();
			break;

		case R.id.severAuthFinger:// 服务器验证获取静脉数据
			GetFingerFeatureforServerAuth();
			break;

		case R.id.onebyoneauth:// 1:1 验证
			auth(false);
			break;
		default:
			break;
		}
	}

}
