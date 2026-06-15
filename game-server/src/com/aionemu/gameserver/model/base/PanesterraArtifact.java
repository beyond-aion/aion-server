package com.aionemu.gameserver.model.base;

/**
 * @author Estrayl
 */
public class PanesterraArtifact extends PanesterraBase {

	public PanesterraArtifact(PanesterraBaseLocation loc) {
		super(loc);
	}

	@Override
	protected int getBossSpawnDelay() {
		return 5 * 60000; // Retail delay
	}
}
