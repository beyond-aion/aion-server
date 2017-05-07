package com.aionemu.commons.network.packet;

/**
 * Basic superclass for packets.
 * <p/>
 * Created on: 29.06.2009 17:59:25
 * 
 * @author Aquanox
 */
public abstract class BasePacket {

	/**
	 * Packet opCode field
	 */
	private int opCode;

	/**
	 * Constructs a new packet.<br>
	 * If this constructor is used, then setOpcode() must be used just after it.
	 */
	protected BasePacket() {
	}

	/**
	 * Constructs a new packet with specified id.
	 * 
	 * @param opCode
	 *          Id of packet
	 */
	protected BasePacket(int opCode) {
		this.opCode = opCode;
	}

	/**
	 * Returns packet opCode.
	 * 
	 * @return packet id
	 */
	public final int getOpCode() {
		return opCode;
	}

	protected final void setOpCode(int opCode) {
		this.opCode = opCode;
	}

	/**
	 * Returns packet name.
	 * <p/>
	 * Actually packet name is a simple name of the underlying class.
	 * 
	 * @return packet name
	 * @see Class#getSimpleName()
	 */
	public String getPacketName() {
		return getClass().getSimpleName();
	}

	public String toFormattedPacketNameString() {
		return String.format("[0x%02X] %s", getOpCode(), getPacketName());
	}

	/**
	 * Returns string representation of this packet based on packet type, opCode and name.
	 * 
	 * @return packet type string
	 * @see #TYPE_PATTERN
	 * @see java.util.Formatter
	 * @see String#format(String, Object[])
	 */
	@Override
	public String toString() {
		return toFormattedPacketNameString();
	}
}
