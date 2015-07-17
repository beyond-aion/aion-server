package com.aionemu.chatserver.common.netty;

/**
 * @author ATracer
 */
public abstract class AbstractPacket
{
	protected int opCode;

	/**
	 * @param opCode
	 */
	public AbstractPacket(int opCode)
	{
		this.opCode = opCode;
	}

	/**
	 * @return the opCode
	 */
	public int getOpCode()
	{
		return opCode;
	}
}
