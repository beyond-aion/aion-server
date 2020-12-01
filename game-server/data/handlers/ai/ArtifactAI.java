package ai;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.ArtifactStatus;
import com.aionemu.gameserver.model.team.legion.LegionPermissionsMask;
import com.aionemu.gameserver.model.templates.siegelocation.ArtifactActivation;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_ARTIFACT_INFO3;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.properties.TargetSpeciesAttribute;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer, Source
 */
@AIName("artifact")
public class ArtifactAI extends NpcAI {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");

	private Map<Integer, ItemUseObserver> observers = new HashMap<>();

	public ArtifactAI(Npc owner) {
		super(owner);
	}

	@Override
	protected SiegeSpawnTemplate getSpawnTemplate() {
		return (SiegeSpawnTemplate) super.getSpawnTemplate();
	}

	@Override
	protected void handleDialogStart(Player player) {
		ArtifactLocation loc = SiegeService.getInstance().getArtifact(getSpawnTemplate().getSiegeId());
		// open artifact activation window
		AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_ASK_ARTIFACT_POPUPDIALOG, loc.getCoolDown(), new AIRequest() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				// show required item and count in confirm dialog
				AIActions.addRequest(ArtifactAI.this, player, SM_QUESTION_WINDOW.STR_ASK_USE_ARTIFACT, new AIRequest() {

					@Override
					public void acceptRequest(Creature requester, Player responder, int requestId) {
						onActivate(responder);
					}

				}, ChatUtil.l10n(716570), SiegeService.getInstance().getArtifact(getSpawnTemplate().getSiegeId()).getTemplate().getActivation().getCount());
			}
		});
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}

	public void onActivate(final Player player) {
		final ArtifactLocation loc = SiegeService.getInstance().getArtifact(getSpawnTemplate().getSiegeId());

		// Get Skill id, item, count and target defined for each artifact.
		ArtifactActivation activation = loc.getTemplate().getActivation();
		int skillId = activation.getSkillId();
		final int itemId = activation.getItemId();
		final int count = activation.getCount();
		final SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (skillTemplate == null) {
			LoggerFactory.getLogger(ArtifactAI.class).error("No skill template for artifact effect id : " + skillId);
			return;
		}

		if (loc.getCoolDown() > 0 || !loc.getStatus().equals(ArtifactStatus.IDLE)) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ARTIFACT_OUT_OF_ORDER());
			return;
		}

		if (loc.getLegionId() != 0)
			if (!player.isLegionMember() || player.getLegion().getLegionId() != loc.getLegionId()
				|| !player.getLegionMember().hasRights(LegionPermissionsMask.ARTIFACT)) {
				PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ARTIFACT_HAVE_NO_AUTHORITY());
				return;
			}

		if (player.getInventory().getItemCountByItemId(itemId) < count)
			return;

		if (LoggingConfig.LOG_SIEGE)
			log.info("Artifact " + getSpawnTemplate().getSiegeId() + " activated by " + player.getName() + " (race: " + player.getRace() + ")");

		if (!loc.getStatus().equals(ArtifactStatus.IDLE))
			return;
		// Brodcast start activation.
		final SM_SYSTEM_MESSAGE startMessage = STR_ARTIFACT_CASTING(player.getRace().getL10n(), player.getName(), skillTemplate.getL10n());
		loc.setStatus(ArtifactStatus.ACTIVATION);
		final SM_ABYSS_ARTIFACT_INFO3 artifactInfo = new SM_ABYSS_ARTIFACT_INFO3(loc.getLocationId());
		player.getPosition().getWorldMapInstance().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				PacketSendUtility.sendPacket(player, startMessage);
				PacketSendUtility.sendPacket(player, artifactInfo);
			}

		});

		PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 10000, 1));
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);

		ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 10000, 0));
				final SM_SYSTEM_MESSAGE message = STR_ARTIFACT_CANCELED(loc.getRace().getL10n(), skillTemplate.getL10n());
				loc.setStatus(ArtifactStatus.IDLE);
				final SM_ABYSS_ARTIFACT_INFO3 artifactInfo = new SM_ABYSS_ARTIFACT_INFO3(loc.getLocationId());
				getOwner().getPosition().getWorldMapInstance().forEachPlayer(new Consumer<Player>() {

					@Override
					public void accept(Player player) {
						PacketSendUtility.sendPacket(player, message);
						PacketSendUtility.sendPacket(player, artifactInfo);
					}

				});
			}

		};
		observers.put(player.getObjectId(), observer);
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				ItemUseObserver observer = observers.remove(player.getObjectId());
				if (observer != null)
					player.getObserveController().removeObserver(observer);

				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 10000, 0));
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
				if (!player.getInventory().decreaseByItemId(itemId, count))
					return;
				final SM_SYSTEM_MESSAGE message = STR_ARTIFACT_CORE_CASTING(loc.getRace().getL10n(), skillTemplate.getL10n());
				loc.setStatus(ArtifactStatus.CASTING);
				final SM_ABYSS_ARTIFACT_INFO3 artifactInfo = new SM_ABYSS_ARTIFACT_INFO3(loc.getLocationId());

				player.getPosition().getWorldMapInstance().forEachPlayer(new Consumer<Player>() {

					@Override
					public void accept(Player player) {
						PacketSendUtility.sendPacket(player, message);
						PacketSendUtility.sendPacket(player, artifactInfo);
					}

				});

				loc.setLastActivation(System.currentTimeMillis());
				if (loc.getTemplate().getRepeatCount() == 1)
					ThreadPoolManager.getInstance().schedule(new ArtifactUseSkill(loc, player, skillTemplate), 13000);
				else {
					final ScheduledFuture<?> s = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ArtifactUseSkill(loc, player, skillTemplate), 13000,
						loc.getTemplate().getRepeatInterval() * 1000);
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							s.cancel(true);
							loc.setStatus(ArtifactStatus.IDLE);
						}

					}, 13000 + (loc.getTemplate().getRepeatInterval() * loc.getTemplate().getRepeatCount() * 1000));
				}

			}

		}, 10000));
	}

	class ArtifactUseSkill implements Runnable {

		private ArtifactLocation artifact;
		private Player player;
		private SkillTemplate skill;
		private int runCount = 1;
		private SM_ABYSS_ARTIFACT_INFO3 pkt;
		private SM_SYSTEM_MESSAGE message;

		/**
		 *
		 * @param artifact
		 * @param activator
		 * @param skill used by this artifact
		 */
		private ArtifactUseSkill(ArtifactLocation artifact, Player activator, SkillTemplate skill) {
			this.artifact = artifact;
			this.player = activator;
			this.skill = skill;
			this.pkt = new SM_ABYSS_ARTIFACT_INFO3(artifact.getLocationId());
			this.message = STR_ARTIFACT_FIRE(activator.getRace().getL10n(), player.getName(), skill.getL10n());
		}

		@Override
		public void run() {
			if (artifact.getTemplate().getRepeatCount() < runCount)
				return;

			final boolean start = (runCount == 1);
			final boolean end = (runCount == artifact.getTemplate().getRepeatCount());
			runCount++;
			if (start) {
				artifact.setStatus(ArtifactStatus.ACTIVATED);
			}
			getOwner().getPosition().getWorldMapInstance().forEachPlayer(new Consumer<Player>() {

				@Override
				public void accept(Player player) {
					if (start) {
						PacketSendUtility.sendPacket(player, message);
						PacketSendUtility.sendPacket(player, pkt);
					}
					if (end) {
						PacketSendUtility.sendPacket(player, pkt);
					}
				}

			});
			boolean pc = skill.getProperties().getTargetSpecies() == TargetSpeciesAttribute.PC;
			artifact.forEachCreature(creature -> {
				if (creature.getActingCreature() instanceof Player || (creature instanceof SiegeNpc && !pc)) {
					switch (skill.getProperties().getTargetRelation()) {
						case FRIEND:
							if (player.isEnemy(creature))
								return;
							break;
						case ENEMY:
							if (!player.isEnemy(creature))
								return;
							break;
					}
					SkillEngine.getInstance().applyEffectDirectly(skill, skill.getLvl(), getOwner(), creature);
				}
			});
		}

	}

}
