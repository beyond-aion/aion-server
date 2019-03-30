package com.aionemu.commons.configuration.transformers;

import java.io.File;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Transforms string to file by creating new file instance. It's not checked if file exists.
 * 
 * @author SoulKeeper
 */
public class FileTransformer extends PropertyTransformer<File> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final FileTransformer SHARED_INSTANCE = new FileTransformer();

	@Override
	protected File parseObject(String value, TransformationTypeInfo typeInfo) {
		return new File(value);
	}
}
