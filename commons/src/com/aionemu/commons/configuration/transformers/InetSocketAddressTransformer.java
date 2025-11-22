package com.aionemu.commons.configuration.transformers;

import java.net.InetSocketAddress;
import java.net.URI;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Transforms strings in the format {@code host:port} to an InetSocketAddress, where host can be a hostname or an IP address (IPv6 addresses must be
 * enclosed in square brackets).
 * 
 * @author SoulKeeper
 */
public class InetSocketAddressTransformer extends PropertyTransformer<InetSocketAddress> {

	@Override
	public boolean matches(Class<?> targetType) {
		return InetSocketAddress.class.isAssignableFrom(targetType);
	}

	@Override
	protected InetSocketAddress parseObject(String value, TransformationTypeInfo typeInfo) throws Exception {
		URI uri = new URI(null, value, null, null, null);
		return new InetSocketAddress(uri.getHost(), uri.getPort());
	}
}
