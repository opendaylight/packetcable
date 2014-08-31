/**
 * 
 */
package org.pcmm.base.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import org.pcmm.base.IPCMMBaseObject;
import org.umu.cops.stack.COPSData;

/**
 * 
 * Implementation of the base class {@link IPCMMBaseObject}
 * 
 */
public class PCMMBaseObject /* extends COPSPrObjBase */implements
		IPCMMBaseObject {

	private byte sType;
	private byte sNum;
	private short len;
	private COPSData copsData;
	private COPSData pad;
	protected final short offset = (short) 4;

	public PCMMBaseObject(byte[] data) {
		parse(data);
	}

	public PCMMBaseObject(short len, byte sType, byte sNum) {
		this.len = (len);
		this.sType = (sType);
		this.sNum = (sNum);
		byte[] array = new byte[len - offset];
		Arrays.fill(array, (byte) 0);
		setData(new COPSData(array, 0, array.length));
	}

	protected COPSData getPadding(int len) {
		byte[] padBuf = new byte[len];
		Arrays.fill(padBuf, (byte) 0);
		COPSData d = new COPSData(padBuf, 0, len);
		return d;
	}

	/**
	 * Add head padding to the specified byte array filled with zeros
	 * 
	 * @param off
	 *            offset
	 * @param array
	 *            input array
	 * @return byte array
	 */
	protected byte[] headPadding(int off, byte[] array) {
		byte[] returnArray = new byte[array.length + off];
		Arrays.fill(returnArray, (byte) 0);
		System.arraycopy(array, 0, returnArray, off, array.length);
		return returnArray;
	}

	protected void parse(byte[] data) {
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("data could not be null");
		len = 0;
		len |= ((short) data[0]) << 8;
		len |= ((short) data[1]) & 0xFF;
		sNum = data[2];
		sType = data[3];
		copsData = new COPSData(data, offset, data.length - offset);
	}

	protected void setShort(short value, short startPos) {
		byte[] data = getData().getData();
		data[startPos] = (byte) (value >> 8);
		data[startPos + 1] = (byte) value;
		setData(new COPSData(data, 0, data.length));
	}

	protected short getShort(short startPos) {
		byte[] data = getData().getData();
		short retVal = 0;
		retVal |= ((short) data[startPos]) << 8;
		retVal |= ((short) data[startPos + 1]) & 0xFF;
		return retVal;
	}

	protected void setInt(int value, short startPos) {
		byte[] data = getData().getData();
		data[startPos] = (byte) (value >> 24);
		data[startPos + 1] = (byte) (value >> 16);
		data[startPos + 2] = (byte) (value >> 8);
		data[startPos + 3] = (byte) value;
		setData(new COPSData(data, 0, data.length));
	}

	protected int getInt(short startPos) {
		byte[] data = getData().getData();
		int retVal = 0;
		retVal |= ((short) data[startPos]) << 24;
		retVal |= ((short) data[startPos + 1]) << 16;
		retVal |= ((short) data[startPos + 2]) << 8;
		retVal |= ((short) data[startPos + 3]) & 0xFF;
		return retVal;
	}

	protected void setBytes(byte[] value, short startPos) {
		byte[] data = getData().getData();
		for (byte b : value)
			data[startPos++] = b;
		setData(new COPSData(data, 0, data.length));
	}

	protected byte[] getBytes(short startPos, short size) {
		return Arrays.copyOfRange(getData().getData(), startPos, startPos
				+ size);
	}

	protected void setByte(byte value, short startPos) {
		setBytes(new byte[] { value }, startPos);
	}

	protected byte getByte(short startPos) {
		return getBytes(startPos, (short) 1)[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#setSType(byte)
	 */
	@Override
	public void setSType(byte stype) {
		this.sType = stype;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#getSType()
	 */
	@Override
	public byte getSType() {
		return sType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#setSNum(byte)
	 */
	@Override
	public void setSNum(byte snum) {
		this.sNum = snum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#getSNum()
	 */
	@Override
	public byte getSNum() {
		return sNum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#setLength(short)
	 */
	@Override
	public void setLength(short len) {
		this.len = len;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#getLength()
	 */
	@Override
	public short getLength() {
		return (short) (len + (pad != null ? pad.length() : 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#setData(org.umu.cops.stack.COPSData)
	 */
	@Override
	public void setData(COPSData data) {
		this.copsData = data;
		if (data.length() % offset != 0) {
			int padLen = offset - (data.length() % offset);
			pad = getPadding(padLen);
		}
		len = (short) (data.length() + offset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#getData()
	 */
	@Override
	public COPSData getData() {
		return copsData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#writeData(java.net.Socket)
	 */
	public void writeData(Socket id) throws IOException {
		byte[] data = getAsBinaryArray();
		id.getOutputStream().write(data, 0, data.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.base.IPCMMBaseObject#getAsBinaryArray()
	 */
	@Override
	public byte[] getAsBinaryArray() {
		byte[] array = new byte[getLength()];
		array[0] = (byte) (len >> 8);
		array[1] = (byte) len;
		array[2] = sNum;
		array[3] = sType;
		System.arraycopy(getData().getData(), 0, array, offset, getData()
				.length());
		if (pad != null)
			System.arraycopy(pad.getData(), 0, array, offset
					+ getData().length(), pad.length());
		return array;
	}
}
