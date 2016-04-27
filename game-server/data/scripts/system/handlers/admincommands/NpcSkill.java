package admincommands;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Wakizashi
 */
public class NpcSkill extends AdminCommand {

	public NpcSkill() {
		super("npcskill");
	}

	@Override
	public void execute(Player admin, String... params) {
		Npc target = null;
		VisibleObject creature = admin.getTarget();
		if (admin.getTarget() instanceof Npc) {
			target = (Npc) creature;
		}

		if (target == null) {
			PacketSendUtility.sendMessage(admin, "You should select a valid target first!");
			return;
		}

		StringBuilder strbld = new StringBuilder("-list of skills:\n");

		List<NpcSkillTemplate> list = null;
		if (DataManager.NPC_SKILL_DATA.getNpcSkillList(target.getNpcId()) != null) {
			list = DataManager.NPC_SKILL_DATA.getNpcSkillList(target.getNpcId()).getNpcSkills();
		}

		if (list != null && !list.isEmpty()) {
			for (NpcSkillTemplate skill : list)
				strbld.append("    level " + skill.getSkillLevel() + " of " + skill.getSkillId() + ", " + skill.getProbability() + "% prob and " + skill.getCooldown() +"ms cd.\n");
			showAllLines(admin, strbld.toString());
		} else {
			PacketSendUtility.sendMessage(admin, "This npc does not have any skills.");
		}
	}

	private void showAllLines(Player admin, String str) {
		int index = 0;
		String[] strarray = str.split("\n");

		while (index < strarray.length - 20) {
			StringBuilder strbld = new StringBuilder();
			for (int i = 0; i < 20; i++, index++) {
				strbld.append(strarray[index]);
				if (i < 20 - 1)
					strbld.append("\n");
			}
			PacketSendUtility.sendMessage(admin, strbld.toString());
		}
		int odd = strarray.length - index;
		StringBuilder strbld = new StringBuilder();
		for (int i = 0; i < odd; i++, index++)
			strbld.append(strarray[index] + "\n");
		PacketSendUtility.sendMessage(admin, strbld.toString());
	}

	@Override
	public void info(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
