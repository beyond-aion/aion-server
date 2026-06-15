package instance.crucible;

import java.util.List;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.CruciblePlayerReward;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author xTz
 */
public class CrucibleInstance extends GeneralInstanceHandler {

	protected boolean isInstanceDestroyed = false;
	protected StageType stageType = StageType.DEFAULT;
	protected InstanceScore<CruciblePlayerReward> instanceScore;

	public CrucibleInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (!instanceScore.containsPlayer(player.getObjectId())) {
			addPlayerReward(player);
		}
	}

	@Override
	public void onInstanceCreate() {
		instanceScore = new InstanceScore<>();
	}

	private void addPlayerReward(Player player) {
		instanceScore.addPlayerReward(new CruciblePlayerReward(player.getObjectId()));
	}

	protected CruciblePlayerReward getPlayerReward(int objectId) {
		return instanceScore.getPlayerReward(objectId);
	}

	@Override
	public InstanceScore<?> getInstanceScore() {
		return instanceScore;
	}

	protected List<Npc> getNpcs(int npcId) {
		if (!isInstanceDestroyed) {
			return instance.getNpcs(npcId);
		}
		return null;
	}

	protected boolean isInZone(ZoneName zone, Player player) {
		return player.isInsideZone(zone);
	}

	protected void teleport(Player player, float x, float y, float z, byte h) {
		TeleportService.teleportTo(player, instance, x, y, z, h);
	}

	@Override
	public StageType getStage() {
		return stageType;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		return true;
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		instanceScore.clear();
	}

	@Override
	public boolean allowSelfReviveBySkill() {
		return false;
	}

	@Override
	public boolean allowSelfReviveByItem() {
		return false;
	}
}
