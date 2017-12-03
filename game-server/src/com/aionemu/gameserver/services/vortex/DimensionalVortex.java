package com.aionemu.gameserver.services.vortex;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.model.vortex.VortexStateType;
import com.aionemu.gameserver.services.VortexService;

/**
 * @author Source
 */
public abstract class DimensionalVortex<VL extends VortexLocation> {

	private final VL vortexLocation;
	private final GeneratorDestroyListener generatorDestroyListener = new GeneratorDestroyListener(this);
	private final AtomicBoolean finished = new AtomicBoolean();
	private boolean generatorDestroyed;
	private Npc generator;
	private boolean started;

	protected abstract void startInvasion();

	protected abstract void stopInvasion();

	public abstract void addPlayer(Player player, boolean isInvader);

	public abstract void kickPlayer(Player player, boolean isInvader);

	public abstract void updateDefenders(Player defender);

	public abstract void updateInvaders(Player invader);

	public abstract Map<Integer, Player> getDefenders();

	public abstract Map<Integer, Player> getInvaders();

	public DimensionalVortex(VL vortexLocation) {
		this.vortexLocation = vortexLocation;
	}

	public final void start() {

		boolean doubleStart = false;

		synchronized (this) {
			if (started) {
				doubleStart = true;
			} else {
				started = true;
			}
		}

		if (doubleStart) {
			return;
		}

		startInvasion();
	}

	public final void stop() {
		if (finished.compareAndSet(false, true)) {
			stopInvasion();
		}
	}

	protected void initRiftGenerator() {

		Npc gen = null;

		for (VisibleObject obj : getVortexLocation().getSpawned()) {
			int npcId = ((Npc) obj).getNpcId();
			if (npcId == 209487 || npcId == 209486) {
				gen = (Npc) obj;
			}
		}

		if (gen == null) {
			throw new NullPointerException("No generator was found in loc:" + getVortexLocationId());
		}

		setGenerator(gen);
		registerSiegeBossListeners();
	}

	protected void spawn(VortexStateType type) {
		VortexService.getInstance().spawn(getVortexLocation(), type);
	}

	protected void despawn() {
		VortexService.getInstance().despawn(getVortexLocation());
	}

	protected void registerSiegeBossListeners() {
		getGenerator().getAi().addEventListener(generatorDestroyListener);
	}

	protected void unregisterSiegeBossListeners() {
		getGenerator().getAi().removeEventListener(generatorDestroyListener);
	}

	public boolean isGeneratorDestroyed() {
		return generatorDestroyed;
	}

	public void setGeneratorDestroyed(boolean state) {
		this.generatorDestroyed = state;
	}

	public Npc getGenerator() {
		return generator;
	}

	public void setGenerator(Npc generator) {
		this.generator = generator;
	}

	public boolean isFinished() {
		return finished.get();
	}

	public VL getVortexLocation() {
		return vortexLocation;
	}

	public int getVortexLocationId() {
		return vortexLocation.getId();
	}

}
