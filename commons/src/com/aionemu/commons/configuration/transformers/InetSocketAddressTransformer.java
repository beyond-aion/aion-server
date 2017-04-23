package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.aionemu.commons.configuration.PropertyTransformer;

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

	/**
	 * Transforms string to InetSocketAddress
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return InetSocketAddress that represents value
	 * @throws Exception
	 *           if something went wrong
	 */
	@Override
	protected InetSocketAddress parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		String[] parts = value.split(":", 2);
		if (parts.length != 2)
			throw new IllegalArgumentException("InetSocketAdress must be specified in the following format: \"adress:port\" or \"*:port\"");
		int port = Integer.parseInt(parts[1]);
		if ("*".equals(parts[0])) {
			return new InetSocketAddress(port);
		}
		InetAddress address = InetAddress.getByName(parts[0]);
		return new InetSocketAddress(address, port);
	}
}
