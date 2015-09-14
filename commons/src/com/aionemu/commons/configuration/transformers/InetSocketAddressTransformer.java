package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Thransforms string to InetSocketAddress. InetSocketAddress can be represented in following ways:
 * <ul>
 * <li>address:port</li>
 * <li>*:port - will use all avaiable network interfaces</li>
 * </ul>
 * 
 * @author SoulKeeper
 */
public class InetSocketAddressTransformer implements PropertyTransformer<InetSocketAddress> {

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
	 * @return InetSocketAddress that represetns value
	 * @throws TransformationException
	 *           if somehting went wrong
	 */
	@Override
	public InetSocketAddress transform(String value, Field field) throws TransformationException {
		String[] parts = value.split(":");

		if (parts.length != 2) {
			throw new TransformationException("Can't transform property, must be in format \"address:port\"");
		}

		try {
			if ("*".equals(parts[0])) {
				return new InetSocketAddress(Integer.parseInt(parts[1]));
			}
			InetAddress address = InetAddress.getByName(parts[0]);
			int port = Integer.parseInt(parts[1]);
			return new InetSocketAddress(address, port);
		} catch (Exception e) {
			throw new TransformationException(e);
		}
	}
}
