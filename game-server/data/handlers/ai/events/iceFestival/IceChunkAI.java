package ai.events.iceFestival;

import java.util.concurrent.TimeUnit;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.SkillUseAction;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

@AIName("icefestival_ice_chunk")
public class IceChunkAI extends NpcAI {

	public IceChunkAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		Item iceCarvingTool = player.getInventory().getFirstItemByItemId(164002294);
		if (iceCarvingTool != null) {
			SkillUseAction skillUseAction = (SkillUseAction) iceCarvingTool.getItemTemplate().getActions().getItemActions().getFirst();
			skillUseAction.act(player, iceCarvingTool, null);
		}
	}

	@Override
	public void onEffectApplied(Effect effect) {
		if (getNpcId() < 833516 || getNpcId() > 833519)
			return;
		if (effect.getSkillId() == 11014 && effect.getEffector() instanceof Player player && getOwner().getController().delete()) {
			if (Rnd.chance() < 69) {
				switch (getNpcId()) {
					case 833516 -> ItemService.addItem(player, 188053955, 1, true); // [Event] Ice Sculptor's Box
					case 833517 -> ItemService.addItem(player, 188053956, 1, true); // [Event] Talented Ice Sculptor's Box
					case 833518 -> ItemService.addItem(player, 188053957, 1, true); // [Event] Enheartened Ice Sculptor's Box
					case 833519 -> ItemService.addItem(player, 188053958, 1, true); // [Event] Soulful Ice Sculptor's Box
				}
				int nextId = getNpcId() + 1;
				boolean isCompletedIceSculpture = nextId == 833520;
				if (isCompletedIceSculpture) {
					PacketSendUtility.sendMonologue(player, 1501360); // The ice sculpture is a great success!
					Npc completedIceSculpture = (Npc) spawn(nextId, "icefestival_completed_ice_sculpture");
					completedIceSculpture.getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> {
						completedIceSculpture.getController().delete();
						spawn(833516, getSpawnTemplate().getAiName());
					}, 2, TimeUnit.HOURS));
				} else {
					spawn(nextId, getSpawnTemplate().getAiName());
				}
			} else {
				PacketSendUtility.sendMonologue(player, 1501361); // Oh, the ice sculpture has shattered...
				ThreadPoolManager.getInstance().schedule(() -> spawn(833516, getSpawnTemplate().getAiName()), 2, TimeUnit.MINUTES);
			}
		}
	}

	private VisibleObject spawn(int npcId, String aiName) {
		if (!EventService.getInstance().isEventActive(getSpawnTemplate().getEventTemplate().getName()))
			return null;
		return spawn(npcId, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading(), 0, aiName);
	}

	@Override
	public boolean ask(AIQuestion question) {
		if (question == AIQuestion.ALLOW_RESPAWN)
			return false;
		return super.ask(question);
	}
}
