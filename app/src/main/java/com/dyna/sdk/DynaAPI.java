package com.dyna.sdk;


import android.util.Log;

public class DynaAPI {

	static {
		System.loadLibrary("dyna_api");
	}

	private  final static String TAG="DynaAPI";
	private native int DynaInit(String path); // 打开串口

	private native int DynaExit(); // 关闭串口

	private native int DynaRegister(String userID, int timeOut);// 注册指静脉

	private native String DynaAuth(int timeOut);// 1:N验证用户

	private native int DynaAuth1By1(String userId, int timeOut);// 1:1验证用户

	private native int DynaDeleteUser(String userId);// 删除用户

	private native int DynaClearUsers();// 删除所有用户

	private native byte[] DynaLoadFeature(String userId);// 获取用户指静脉数据

	private native int DynaSaveFeature(String userId, byte[] feature, int len);// 保存用户指静脉数据

	private native int DynaHaltRega(); // 中止注册

	private native int DynaGetLastErr();// 获取1:N 验证返回的状态编码
	
	private native int DynaLoadr(int timeOut);// 获取用户服务器数据,用于服务器验证方式.
	
	private native int DynaStop(); //服务停止
	
	private static DynaAPI dynaAPI = null;

	private DynaAPI() {
	}

	public static DynaAPI getSingleInstance() {

		if (dynaAPI == null) {
			dynaAPI = new DynaAPI();
		}
		return dynaAPI;
	}

	// 服务端验证--采集返回
	public void onServerAuthUserCallBack(byte[] data, int size) {
		Log.e(TAG,
				"len="+size+"  data="+ ByteUtils.byteToHex(data, 0, data.length > size ? size : data.length));
		/***去服务器验证....***/
		
	}


	// 验证用户返回
	public void onAuthUserCallBack(int status) {
		Log.e(TAG, "status=" + status);
	}

	// 验证用户返回
	public void onAuthUser1ByNCallBack(String userId) {
		Log.e(TAG, "userId=" + userId);
	}


	// 回调采集的状态 和 采集的次数
	public void onRegeistUserCallBack(int status, int detectTimes) {
		Log.e(TAG, "status=" + status + " detectTimes=" + detectTimes);
	}

	// 连接读卡器
	public int ConnectDyna(String path) {
		return DynaInit(path);
	}

	// 注册手指
	public int RegistUser(String userID, int timeout) {
		return DynaRegister(userID, timeout);
	}

	// 验证用户手指 1:1模式
	public int Auth1by1User(String userId, int timeout) {
		return DynaAuth1By1(userId, timeout);
	}

	// 验证用户手指 1:N模式
	public String AuthUser(int timeout) {
		return DynaAuth(timeout);
	}

	// 删除用户
	public int deleteUser(String userID) {
		return DynaDeleteUser(userID);
	}

	// 删除所有用户
	public int ClearUsers() {
		return DynaClearUsers();
	}

	// 中断注册
	public int RegistStop() {
		return DynaHaltRega();
	}

	// 关闭串口
	public int closeComm() {
		return DynaExit();
	}

	// 获取指静脉数据
	public byte[] getFingerFeature(String UserId) {
		return DynaLoadFeature(UserId);
	}

	// 保存指静脉数据
	public int saveFingerFeature(String UserId, byte[] feature, int len) {
		return DynaSaveFeature(UserId, feature, len);
	}
	
	// 服务器验证获取用户模板数据
	public int GetFingerFeatureforServerAuth(int timeout) {
		return DynaLoadr(timeout);
	}
}
