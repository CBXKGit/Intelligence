package com.dyna.sdk;



import java.io.UnsupportedEncodingException;

public class ByteUtils {

	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	/**
	 * 字符串转换 Uncode
	 */
	
	
	public static String UByteToString(byte[] data, int offset, int len) {
           
       byte[] des=new byte[len];
		try {
			 System.arraycopy(data, offset, des, 0x00, len);
			return new String(des,"unicode");
		 } catch (UnsupportedEncodingException e) {
	         return null;
		}
	}

	/**
	 * 字符byte转化为 UnCode Byte
	 */
	public static byte[] AByteToUByte(String data) {

		try {
			if (!data.isEmpty()) {
				byte[] UData = data.getBytes("unicode");
				return ByteUtils.InterceptData(UData, 0x02, UData.length-2);
			} else {
				return new byte[] { 0x00, 0x00, 0x00, 0x00 };
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
   /***
    * 两种byte数组相加
    ***/
	public static byte[] add(byte[] data0, byte[] data1) {
		
		byte[] data = new byte[data0.length + data1.length];
		System.arraycopy(data0, 0x00, data, 0x00, data0.length);
		System.arraycopy(data1, 0x00, data, data0.length, data1.length);
		return data;
	}

	/**
	 * 字节转换成HEX 字符串
	 * 
	 * @param data
	 * @return
	 */
	public static String byteToHex(byte data) {
		return String.valueOf(HEX[(data & 0xF0) >> 4]) + String.valueOf(HEX[data & 0x0F]);
	}

	/**
	 * 字节转换成HEX 字符串
	 * 
	 * @param raw
	 * @return
	 */
	public static String byteToHex(byte... raw) {
		if (raw != null) {
			return byteToHex(raw, 0, raw.length);
		} else {
			return null;
		}
	}

	/**
	 * 字节转换成HEX 字符串
	 * 
	 * @param raw
	 * @param offset
	 * @param count
	 * @return
	 */
	public static String byteToHex(byte[] raw, int offset, int count) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);

		offset = offset > (raw.length - 1) ? 0 : offset;
		count = count > raw.length ? raw.length : count;
		int end = offset + count;

		for (int i = offset; i < end; i++) {
			byte b = raw[i];
			hex.append(HEX[(b & 0xF0) >> 4]).append(HEX[b & 0x0F]);
		}
		return hex.toString();
	}

	/**
	 * 打印 内容
	 * 
	 * @param raw
	 * @param offset
	 * @param count
	 * @return
	 */
	public static String printBytes(byte[] raw, int offset, int count) {
		if (raw == null) {
			return " waring data is null!";
		}
		final StringBuilder hex = new StringBuilder();
		int len = raw.length;

		offset = offset > (len - 1) ? len - 1 : offset;

		int end = offset + count;
		end = end > len ? len : end;
		for (int i = offset; i < end; i++) {
			byte b = raw[i];
			hex.append(HEX[(b & 0xF0) >> 4]).append(HEX[b & 0x0F]).append(" ");
		}
		return hex.toString();
	}

	public static String printBytes(byte[] raw) {
		if (raw != null) {
			return printBytes(raw, 0, raw.length);
		} else {
			return null;
		}
	}

	public static String printByte(byte b) {
		return String.valueOf(HEX[(b & 0xF0) >> 4]) + String.valueOf(HEX[b & 0x0F]);
	}

	/**
	 * Hex 字符串转换成字节数组
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexToByte(String hex) {
		if (hex == null || "".equals(hex))
			return null;

		hex = hex.replaceAll(" ", "");

		int len = hex.length() / 2;
		byte[] data = new byte[len];
		int offset = 0;
		int at = 0;
		int at1 = 0;
		for (int i = 0; i < len; i++) {
			offset = i * 2;
			at = asciiToHex(hex.charAt(offset));
			at1 = asciiToHex(hex.charAt(offset + 1)) & 0xFF;
			data[i] = (byte) (at << 4 | at1);
		}
		return data;
	}

	public static byte[] InterceptData(byte[] src, int offset, int count) {
		byte[] des = new byte[count];
		System.arraycopy(src, offset, des, 0, des.length);
		return des;
	}

	private static int asciiToHex(int asc) {
		if (asc <= 57) {
			return asc - 48;
		} else if (asc <= 70) {
			return asc - 55;
		} else {
			return asc - 87;
		}
	}

	/**
	 * BCD 转字符串
	 * 
	 * @param data
	 * @param offset
	 * @param len
	 * @return
	 */
	public static String bcdToString(byte[] data, int offset, int len) {
		StringBuilder sb = new StringBuilder(len * 2);
		int end = offset + len;
		for (int i = offset; i < end; i++) {
			sb.append((data[i] & 0xF0) >> 4).append(data[i] & 0x0F);
		}
		return sb.toString();
	}

	/**
	 * 右靠BCD
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] toAlignRightBcd(String value) {
		byte[] buf = new byte[Math.round((float) (value.length() / (2.0)))];
		int charpos = 0;
		int bufpos = 0;
		if (value.length() % 2 == 1) {
			buf[0] = (byte) (value.charAt(0) - 48);
			charpos = 1;
			bufpos = 1;
		}
		while (charpos < value.length()) {
			buf[bufpos] = (byte) (((value.charAt(charpos) - 48) << 4) | (value.charAt(charpos + 1) - 48));
			charpos += 2;
			bufpos++;
		}

		return buf;
	}

	/**
	 * 左靠BCD
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] toAlignLeftBcd(String value) {
		byte[] buf = new byte[Math.round((float) (value.length() / (2.0)))];

		int len = value.length() / 2;
		for (int i = 0; i < len; i++) {
			buf[i] = (byte) (((value.charAt(i * 2) - 48) << 4) | (value.charAt(i * 2 + 1) - 48));
		}

		if (value.length() % 2 == 1) {
			buf[len] = (byte) ((value.charAt(value.length() - 1) - 48) << 4);
		}
		return buf;
	}

	/**
	 * 2位byte 转换成 int 高前低后
	 * 
	 * @param bit1
	 * @param bit2
	 * @return
	 */
	public static int byteToInt(byte bit1, byte bit2) {
		return ((bit1 & 0xFF) << 8) | (bit2 & 0xFF);
	}

	/**
	 * 低位优先(little-endian)
	 * 
	 * @param bit1
	 * @param bit2
	 * @return
	 */
	public static int byteToIntLE(byte bit1, byte bit2) {
		return (bit1 & 0xFF) | ((bit2 & 0xFF) << 8);
	}

	public static long byteToLong(byte[] bi, int offset) {
		return (bi[3 + offset] & 0xFF) | ((bi[2 + offset] & 0xFF) << 8) | ((bi[1 + offset] & 0xFF) << 16)
				| ((bi[0 + offset] & 0xFF) << 24);
	}

	public static long byteToLongLE(byte[] bi, int offset) {
		return (bi[0 + offset] & 0xFF) | ((bi[1 + offset] & 0xFF) << 8) | ((bi[2 + offset] & 0xFF) << 16)
				| ((bi[3 + offset] & 0xFF) << 24);
	}

	public static int byteToInt(byte value) {
		return value & 0xFF;
	}

	/**
	 * 获取 2字节int 高前低后 高位优先(big-endian)
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] unsignedShort(int value) {
		byte[] data = new byte[2];
		data[0] = (byte) (value >> 8);
		data[1] = (byte) (value);
		return data;
	}

	/**
	 * 低位优先(little-endian)
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] unsignedShortLE(int value) {
		byte[] data = new byte[2];
		data[0] = (byte) value;
		data[1] = (byte) (value >> 8);
		return data;
	}

	public static byte[] unsignedInt(long value) {
		byte[] data = new byte[4];
		data[0] = (byte) (value >> 24);
		data[1] = (byte) (value >> 16);
		data[2] = (byte) (value >> 8);
		data[3] = (byte) (value);
		return data;
	}

	public static byte[] unsignedIntLE(long value) {
		byte[] data = new byte[4];
		data[0] = (byte) (value);
		data[1] = (byte) (value >> 8);
		data[2] = (byte) (value >> 16);
		data[3] = (byte) (value >> 24);
		return data;
	}

	public static void memcpy(byte[] src, int SrcOffset, byte[] des, int DesOffset, int len) {
		for (int i = 0; i < len; i++) {
			des[i + DesOffset] = src[i + SrcOffset];
		}
	}

	/**
	 * 获取 LRC
	 * 
	 * @param data
	 * @param offset
	 * @param len
	 * @return
	 */
	public static byte genLRC(byte[] data, int offset, int len) {
		byte lrc = 0;
		for (int i = offset, end = offset + len; i < end; i++) {
			lrc ^= data[i];
		}
		return lrc;
	}

	public static int genCRC(byte[] data, int len) {
		int crc = CRC16_X25(data, len);
		return crc;
	}

	private static void InvertUint8(byte[] dBuf, byte[] srcBuf) {
		int i;
		byte tmp = 0;
		byte bit = 0;
		for (i = 0; i < 8; i++) {
			bit = (byte) (1 << i);
			if ((srcBuf[0] & bit) == bit)
				tmp |= 1 << (7 - i);
		}
		dBuf[0] = tmp;
	}

	private static void InvertUint16(short[] dBuf, short[] srcBuf) {
		short i;
		short tmp = 0;
		short bit = 0;
		for (i = 0; i < 16; i++) {
			bit = (short) (1 << i);
			if ((srcBuf[0] & bit) == bit)
				tmp |= 1 << (15 - i);
		}
		dBuf[0] = tmp;
	}

	private static short CRC16_X25(byte[] puchMsg, int usDataLen) {

		short[] wCRCin = new short[1];
		wCRCin[0] = (short) 0xFFFF;
		short wCPoly = 0x1021;
		byte[] wChar = new byte[1];
		int ii = 0;

		while (usDataLen-- > 0) {
			wChar[0] = puchMsg[ii++];// *(puchMsg++);
			InvertUint8(wChar, wChar);
			wCRCin[0] ^= (wChar[0] << 8);
			for (int i = 0; i < 8; i++) {
				if ((wCRCin[0] & 0x8000) == 0x8000)
					wCRCin[0] = (short) ((wCRCin[0] << 1) ^ wCPoly);
				else
					wCRCin[0] = (short) (wCRCin[0] << 1);
			}
		}
		InvertUint16(wCRCin, wCRCin);
		return (short) (wCRCin[0] ^ 0xFFFF);
	}

}
