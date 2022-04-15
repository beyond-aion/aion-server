package com.aionemu.commons.configuration.transformers;

import java.net.InetSocketAddress;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Transforms string to InetSocketAddress. InetSocketAddress can be represented in following ways:
 * <ul>
 * <li>address:port</li>
 * <li>*:port - will use all available network interfaces</li>
 * </ul>
 * 
 * @author SoulKeeper
 */
public class InetSocketAddressTransformer extends PropertyTransformer<InetSocketAddress> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final InetSocketAddressTransformer SHARED_INSTANCE = new InetSocketAddressTransformer();

	@Override
	protected InetSocketAddress parseObject(String value, TransformationTypeInfo typeInfo) throws Exception {
		int delimiterIndex = value.lastIndexOf(':');
		if (delimiterIndex < 1 || value.length() < 3)
			throw new IllegalArgumentException("InetSocketAddress must be specified in the following format: \"address:port\" or \"*:port\"");
		String address = value.substring(0, delimiterIndex);
		int port = Integer.parseInt(value.substring(delimiterIndex + 1));
		return "*".equals(address) ? new InetSocketAddress(port) : new InetSocketAddress(address, port);
	}
}
