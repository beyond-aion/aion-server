package ai.instance.illuminaryObelisk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.NoActionAI;

/**
 * @author Estrayl
 */
@AIName("idf5_u3_vortex")
public class IDF5_U3_VortexAI extends NoActionAI {

	private final List<Future<?>> tasks = new ArrayList<>();
	private final List<Integer> npcIds = new ArrayList<>();

	public IDF5_U3_VortexAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getPosition().getMapId() == 301230000) // normal mode
			npcIds.addAll(Arrays.asList(233857, 233880, 233881));
		else // hard mode
			npcIds.addAll(Arrays.asList(234687, 234688, 234689));
		synchronized (tasks) {
			tasks.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(this::handlePhaseAttacks, 120000, 120000));
		}
	}

	private void handlePhaseAttacks() {
		switch (getNpcId()) {
			case 702014: // east
				spawn(npcIds.get(0), 252.3243f, 328.5881f, 325.0092f, (byte) 90, 0, "idf5_u3_east_1");
				spawn(npcIds.get(0), 255.3635f, 328.5584f, 325.0038f, (byte) 90, 0, "idf5_u3_east_2");
				spawn(npcIds.get(1), 256.6376f, 328.7015f, 325.0038f, (byte) 90, 0, "idf5_u3_east_3");
				spawn(npcIds.get(1), 258.5159f, 328.5792f, 325.0038f, (byte) 90, 0, "idf5_u3_east_4");
				spawn(npcIds.get(1), 256.9199f, 326.4982f, 325.0038f, (byte) 90, 0, "idf5_u3_east_5");
				spawn(npcIds.get(1), 253.8757f, 326.5010f, 325.0038f, (byte) 90, 0, "idf5_u3_east_6");
				spawn(npcIds.get(0), 252.3243f, 328.5881f, 325.0092f, (byte) 90, 15000, "idf5_u3_east_1");
				spawn(npcIds.get(0), 255.3635f, 328.5584f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_2");
				spawn(npcIds.get(2), 256.6376f, 328.7015f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_3");
				spawn(npcIds.get(2), 258.5159f, 328.5792f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_4");
				spawn(npcIds.get(2), 256.9199f, 326.4982f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_5");
				spawn(npcIds.get(2), 253.8757f, 326.5010f, 325.0038f, (byte) 90, 15000, "idf5_u3_east_6");
				break;
			case 702015: // west
				spawn(npcIds.get(0), 251.9594f, 183.4159f, 325.0038f, (byte) 30, 0, "idf5_u3_west_1");
				spawn(npcIds.get(0), 253.5314f, 183.5728f, 325.0038f, (byte) 30, 0, "idf5_u3_west_2");
				spawn(npcIds.get(1), 255.2491f, 183.4584f, 325.0038f, (byte) 30, 0, "idf5_u3_west_3");
				spawn(npcIds.get(1), 257.0595f, 183.5797f, 325.0045f, (byte) 30, 0, "idf5_u3_west_4");
				spawn(npcIds.get(1), 258.7057f, 183.6840f, 325.0038f, (byte) 30, 0, "idf5_u3_west_5");
				spawn(npcIds.get(1), 255.0448f, 185.5452f, 325.0038f, (byte) 30, 0, "idf5_u3_west_6");
				spawn(npcIds.get(0), 251.9594f, 183.4159f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_1");
				spawn(npcIds.get(0), 253.5314f, 183.5728f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_2");
				spawn(npcIds.get(2), 255.2491f, 183.4584f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_3");
				spawn(npcIds.get(2), 257.0595f, 183.5797f, 325.0045f, (byte) 30, 15000, "idf5_u3_west_4");
				spawn(npcIds.get(2), 258.7057f, 183.6840f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_5");
				spawn(npcIds.get(2), 255.0448f, 185.5452f, 325.0038f, (byte) 30, 15000, "idf5_u3_west_6");
				break;
			case 702016: // south
				spawn(npcIds.get(0), 326.3734f, 251.2209f, 291.8364f, (byte) 60, 0, "idf5_u3_south_1");
				spawn(npcIds.get(0), 326.3337f, 252.6159f, 291.8364f, (byte) 60, 0, "idf5_u3_south_2");
				spawn(npcIds.get(1), 326.3333f, 253.1857f, 291.8364f, (byte) 60, 0, "idf5_u3_south_3");
				spawn(npcIds.get(1), 326.4392f, 255.9983f, 291.8364f, (byte) 60, 0, "idf5_u3_south_4");
				spawn(npcIds.get(1), 326.4354f, 257.6836f, 291.8466f, (byte) 60, 0, "idf5_u3_south_5");
				spawn(npcIds.get(1), 324.7853f, 254.2962f, 291.8364f, (byte) 60, 0, "idf5_u3_south_6");
				spawn(npcIds.get(0), 326.3734f, 251.2209f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_1");
				spawn(npcIds.get(0), 326.3337f, 252.6159f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_2");
				spawn(npcIds.get(2), 326.3333f, 253.1857f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_3");
				spawn(npcIds.get(2), 326.4392f, 255.9983f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_4");
				spawn(npcIds.get(2), 326.4354f, 257.6836f, 291.8466f, (byte) 60, 15000, "idf5_u3_south_5");
				spawn(npcIds.get(2), 324.7853f, 254.2962f, 291.8364f, (byte) 60, 15000, "idf5_u3_south_6");
				break;
			case 702017: // north
				spawn(npcIds.get(0), 184.6565f, 256.3191f, 291.8364f, (byte) 0, 0, "idf5_u3_north_1");
				spawn(npcIds.get(0), 184.6415f, 253.7202f, 291.8364f, (byte) 0, 0, "idf5_u3_north_2");
				spawn(npcIds.get(1), 184.6134f, 253.0914f, 291.8364f, (byte) 0, 0, "idf5_u3_north_3");
				spawn(npcIds.get(1), 184.7428f, 251.3166f, 291.8842f, (byte) 0, 0, "idf5_u3_north_4");
				spawn(npcIds.get(1), 184.6134f, 253.0914f, 291.8364f, (byte) 0, 0, "idf5_u3_north_5");
				spawn(npcIds.get(1), 186.8694f, 254.6730f, 291.8364f, (byte) 0, 0, "idf5_u3_north_6");
				spawn(npcIds.get(0), 184.6565f, 256.3191f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_1");
				spawn(npcIds.get(0), 184.6415f, 253.7202f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_2");
				spawn(npcIds.get(2), 184.6134f, 253.0914f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_3");
				spawn(npcIds.get(2), 184.7428f, 251.3166f, 291.8842f, (byte) 0, 15000, "idf5_u3_north_4");
				spawn(npcIds.get(2), 184.6134f, 253.0914f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_5");
				spawn(npcIds.get(2), 186.8694f, 254.6730f, 291.8364f, (byte) 0, 15000, "idf5_u3_north_6");
				break;
		}
	}

	private void spawn(int npcId, float x, float y, float z, byte h, int delay, String walkerId) {
		addTask(() -> {
			Npc npc = (Npc) spawn(npcId, x, y, z, h);
			npc.getSpawn().setWalkerId(walkerId);
			addTask(() -> {
				WalkManager.startWalking((NpcAI) npc.getAi());
				npc.setState(CreatureState.ACTIVE, true);
				PacketSendUtility.broadcastToMap(getOwner(), new SM_EMOTION(getOwner(), EmotionType.RUN));
			}, 2500);
		}, delay);
	}

	private void addTask(Runnable task, int delayMs) {
		synchronized (tasks) {
			if (!tasks.isEmpty())
				tasks.add(ThreadPoolManager.getInstance().schedule(task, delayMs));
		}
	}

	@Override
	protected void handleDespawned() {
		synchronized (tasks) {
			tasks.stream().filter(t -> !t.isDone()).forEach(t -> t.cancel(true));
			tasks.clear();
		}
		super.handleDespawned();
	}
}
