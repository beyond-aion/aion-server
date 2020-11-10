package com.aionemu.chatserver.common.netty;

/**
 * @author ATracer
 */
public abstract class AbstractPacket {

	protected byte opCode;

	public AbstractPacket(byte opCode) {
		this.opCode = opCode;
	}

	public byte getOpCode() {
		return opCode;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [opCode=0x%02X]".formatted(getOpCode());
	}
}
