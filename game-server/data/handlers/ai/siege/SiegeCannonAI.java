package ai.siege;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.handler.TalkEventHandler;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Whoop
 */
@AIName("siege_cannon")
public class SiegeCannonAI extends NpcAI {

	public SiegeCannonAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		TalkEventHandler.onTalk(this, player);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		int morphSkill = getMorphSkill();
		Npc owner = getOwner();

		if (dialogActionId == SETPRO1 && morphSkill != 0) {
			TeleportService.teleportTo(player, owner.getWorldId(), owner.getInstanceId(), owner.getX(), owner.getY(), owner.getZ(), owner.getHeading());
			SkillEngine.getInstance().getSkill(getOwner(), morphSkill >> 8, morphSkill & 0xFF, player).useNoAnimationSkill();
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			AIActions.deleteOwner(this);
		}
		return true;
	}

	public final int getMorphSkill() {
		switch (getNpcId()) {
			case 251725: // Krotan Elyos - Sky Cannon
			case 251726:
			case 251727:
			case 251728:
			case 251729:
			case 251730:
			case 251731:
			case 251732:
			case 251733:
			case 251734:
			case 251745: // Kysis Elyos - Sky Cannon
			case 251746:
			case 251747:
			case 251748:
			case 251749:
			case 251750:
			case 251751:
			case 251752:
			case 251753:
			case 251754:
			case 251765: // Miren Elyos - Sky Cannon
			case 251766:
			case 251767:
			case 251768:
			case 251769:
			case 251770:
			case 251771:
			case 251772:
			case 251773:
			case 251774:
			case 882253: // Divine
			case 882255:
				return 0x540D41; // 21517 65
			case 252164: // Wealhtheow Elyos
			case 252165:
			case 252166:
			case 252167:
			case 252168:
			case 252169:
			case 252170:
				return 0x538941; // 21385 65
			case 251735: // Krotan Asmo - Sky Cannon
			case 251736:
			case 251737:
			case 251738:
			case 251739:
			case 251740:
			case 251741:
			case 251742:
			case 251743:
			case 251744:
			case 251755: // Kysis Asmo - Sky Cannon
			case 251756:
			case 251757:
			case 251758:
			case 251759:
			case 251760:
			case 251761:
			case 251762:
			case 251763:
			case 251764:
			case 251775: // Miren Asmo - Sky Cannon
			case 251776:
			case 251777:
			case 251778:
			case 251779:
			case 251780:
			case 251781:
			case 251782:
			case 251783:
			case 251784:
			case 882254: // Divine Asmo Sky Cannon
			case 882256:
				return 0x540E41; // 21518 65
			case 252171: // Wealhtheow Asmo
			case 252172:
			case 252173:
			case 252174:
			case 252175:
			case 252176:
			case 252177:
				return 0x538A41; // 21386 65
			default:
				return 0;
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
