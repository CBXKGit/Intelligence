package serialapi;

import android.util.Log;

import com.dyna.sdk.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class SerialPortSession implements Runnable {

    private SerialPort serialPort = null;
    private String serialDevice;
    private int baudRate;
    private InputStream in = null;
    private OutputStream out = null;
    private Thread thread = null;
    private boolean isConnected = false;
    private String TAG = "SerialPortSession";
    private PowerCallBack callBack;

    private static SerialPortSession serialPortSession;

    public interface PowerCallBack {
        void receiveData(byte[] receiveData, int length); //收到的完整数据,收到的数据长度
        void displayProgress(int type, String msg);//显示进度条信息
    }

    public static SerialPortSession getInstance(PowerCallBack callBack) {
        if (serialPortSession == null) {
            serialPortSession = new SerialPortSession(callBack);
        }
        return serialPortSession;
    }

    private SerialPortSession(PowerCallBack callBack) {
        this.callBack = callBack;
        serialDevice = "/dev/ttyS4";
        baudRate = 9600;
        try {
            setSerialPort();
            setConnected(true);
            if (serialPort != null) {
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
            }
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            Log.e(TAG, "串行端口不能打开");
        } catch (SecurityException e) {
            Log.e(TAG, "没有读写串口的权限");
        } catch (InvalidParameterException e) {
            Log.e(TAG, "请输入正确的串口");
        }
    }

    private void setSerialPort() throws IOException, SecurityException, InvalidParameterException {
        try {
            if (serialPort == null) {
                serialPort = new SerialPort(new File(serialDevice), baudRate, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean writeData(byte[] data) {
        boolean result = false;
        if (out != null) {
            try {
                Log.e(TAG, "发送数据数据:" + ByteUtils.byteToHex(data, 0x0, data.length));
                out.write(data);
                out.flush();
                result = true;
            } catch (IOException e) {
                Log.e(TAG, "发送数据失败！");
            }
        }
        return result;
    }

    @Override
    public void run() {

        byte[] receiveData = new byte[100];  //一直循环等待单片机发送数据
        byte[] rBuf = new byte[100]; //多包缓存
        int readNum = receiveData.length;
        int offset = 0, toffset;

        while (getConnected()) {
            try {
                readNum -= offset;
                if (readNum <= 0 && receiveData[0] == 0x6d) {
                    Log.e(TAG, "完整接收到数据:" + ByteUtils.byteToHex(receiveData, 0, offset));
                    int len = ((receiveData[1] & 0xff) << 8) | (receiveData[2] & 0xff) + 2;
                    callBack.receiveData(receiveData, len);
                    if (offset > len) {//存在多包数据
                        System.arraycopy(receiveData, len, rBuf, 0, offset - len);
                        offset = offset - len;
                        resetArray(receiveData);
                        System.arraycopy(rBuf, 0, receiveData, 0, offset);
                        readNum = ((receiveData[1] & 0xff) << 8) | (receiveData[2] & 0xff) + 2;
                        Log.e(TAG, "多包数据:" + ByteUtils.byteToHex(receiveData, 0, offset));
                        continue;

                    } else {
                        offset = 0;
                        resetArray(receiveData);
                        readNum = receiveData.length;
                    }
                }

                offset += in.read(receiveData, offset, readNum);
                if (offset == 0) continue;
                if (receiveData[0] == 0x6d) {
                    if (offset < 3) continue;
                    else {
                        readNum = ((receiveData[1] & 0xff) << 8) | (receiveData[2] & 0xff) + 2;
                        continue;
                    }
                } else { //查找到属于0x6D的部分其他扔掉。
                    for (toffset = 0; toffset < offset; toffset++) {
                        if (receiveData[toffset] == 0x6D) {
                            offset = offset - toffset;
                            System.arraycopy(receiveData, toffset, rBuf, 0, offset);
                            resetArray(receiveData);
                            System.arraycopy(rBuf, 0, receiveData, 0, offset);//截取到现有的BUF中
                            if (offset > 3) {
                                readNum = ((receiveData[1] & 0xff) << 8) | (receiveData[2] & 0xff) + 2;
                            }
                            break;
                        }
                        Log.e(TAG, "ThrowOutData:" + receiveData[toffset]);
                    }
                    if (offset == toffset) {
                        offset = 0;
                        readNum = receiveData.length;
                        resetArray(receiveData);
                    }
                }
            } catch (IOException e) {
                offset = 0;
                readNum = receiveData.length;
                Log.e(TAG, "IOException " + e.getMessage());
            } catch (Exception e) {
                offset = 0;
                readNum = receiveData.length;
                Log.e(TAG, "Exception " + e.getMessage());
            }
        }

    }

    public void resetArray(byte[] a) {
        for (int i = 0; i < a.length; i++) {
            a[i] = 0x00;
        }
    }

    public void close() {
        try {
            setConnected(false);
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
                thread = null;
            }
            serialPortSession = null;
        } catch (IOException e) {
            Log.e(TAG, "串口关闭异常！");
        }
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    private boolean getConnected() {
        return isConnected;
    }

}
