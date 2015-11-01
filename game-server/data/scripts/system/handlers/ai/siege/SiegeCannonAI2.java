package ai.siege;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.handler.TalkEventHandler;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Whoop
 */
@AIName("siege_cannon")
public class SiegeCannonAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		TalkEventHandler.onTalk(this, player);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		int morphSkill = getMorphSkill();
		Npc owner = getOwner();

		if ((DialogAction.getActionByDialogId(dialogId)) == DialogAction.SETPRO1 && (morphSkill != 0)) {
			TeleportService2.teleportTo(player, owner.getWorldId(), owner.getInstanceId(), owner.getX(), owner.getY(), owner.getZ(), owner.getHeading());
			SkillEngine.getInstance().getSkill(getOwner(), morphSkill >> 8, morphSkill & 0xFF, player).useNoAnimationSkill();
			player.getController().stopProtectionActiveTask();
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			AI2Actions.deleteOwner(this);
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
			case 272848: // Silona Elyos - Sky Cannon
			case 272849:
			case 272850:
			case 272851:
			case 272852:
			case 272853:
				return 0x540D41; // 21517 65
			case 272841: // Silona Elyos - normal Cannon
			case 272842:
			case 272843:
			case 272844:
			case 272845:
			case 272846:
			case 272847:
			case 273344: // Pradeth Elyos - normal Cannon
			case 273345:
			case 273346:
			case 273347:
			case 273353:
			case 273354:
			case 273355:
			case 273356:
			case 272342: // Sillus Elyos - normal Cannon
			case 272343:
			case 272344:
			case 272345:
			case 272346:
			case 272347:
			case 272348:
			case 272349:
			case 272350:
			case 272351:
			case 252164: //Wealhtheow Elyos
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
			case 272861: // Silona Asmo - Sky Cannon
			case 272862:
			case 272863:
			case 272864:
			case 272865:
			case 272866:
				return 0x540E41; // 21518 65
			case 272854: // Silona Asmo - normal Cannon
			case 272855:
			case 272856:
			case 272857:
			case 272858:
			case 272859:
			case 272860:
			case 273348: // Pradeth Asmo - normal Cannon
			case 273349:
			case 273350:
			case 273351:
			case 273357:
			case 273358:
			case 273359:
			case 273360:
			case 272352: // Sillus Asmo - normal Cannon
			case 272353:
			case 272354:
			case 272355:
			case 272356:
			case 272357:
			case 272358:
			case 272359:
			case 272360:
			case 272361:
			case 252171: //Wealhtheow Asmo
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
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.POSITIVE;
			case SHOULD_LOOT:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}
}
