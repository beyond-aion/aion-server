package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Source, kecimis
 */
public class UseSkill extends AdminCommand {

	private final String syntax = "Syntax: //skill <skillId> <skillLevel> [true|false|target] <duration>";

	public UseSkill() {
		super("useskill");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length > 4 || params.length <= 0) {
			info(admin, null);
			return;
		}

		if (params[0].equalsIgnoreCase("help")) {
			PacketSendUtility.sendMessage(admin, syntax + " \n" + "TARGET - targetted creature will use skill on its target.\n"
				+ "TRUE - effect of skill is applied without any checks.\n" + "FALSE - effect of skill is applied as regular effect\n"
				+ "If you want to add duration, you have to use TRUE!\n" + "Example: //useskill 1968 1 or //useskill 1968 1 true 1\n"
				+ "Duration is in seconds, 0 means its taken from skill_template.");
			return;
		}

		VisibleObject target = admin.getTarget();

		int skillId = 0;
		int skillLevel = 0;

		try {
			skillId = Integer.parseInt(params[0]);
			skillLevel = Integer.parseInt(params[1]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "SkillId and skillLevel need to be an integer.");
			return;
		}

		if (target == null || !(target instanceof Creature)) {
			PacketSendUtility.sendMessage(admin, "You must select a target!");
			return;
		}

		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (skillTemplate == null) {
			PacketSendUtility.sendMessage(admin, "No skill template id:" + skillId);
			return;
		}

		if (params.length >= 3) {
			if (params[2].equalsIgnoreCase("true")) {
				int time = 0;
				if (params.length == 4) {
					try {
						time = Integer.parseInt(params[3]);
					} catch (NumberFormatException e) {
						PacketSendUtility.sendMessage(admin, "Time has to be integer!");
						return;
					}
				}
				if (time < 0 || time > 86400) {
					PacketSendUtility.sendMessage(admin, "Time has to be in range 0 - 86400!");
					return;
				}

				SkillEngine.getInstance().applyEffectDirectly(skillId, admin, (Creature) target, (time * 1000));

				PacketSendUtility.sendMessage(admin, "SkillId:" + skillId + " was directlz applied on target " + target.getName());
			} else if (params[2].equalsIgnoreCase("false")) {
				SkillEngine.getInstance().applyEffect(skillId, admin, (Creature) target);
				PacketSendUtility.sendMessage(admin, "SkillId:" + skillId + " was applied on target " + target.getName());
			} else if (params[2].equalsIgnoreCase("target")) {
				if (target.getTarget() == null || !(target.getTarget() instanceof Creature)) {
					PacketSendUtility.sendMessage(admin, "Target must select some creature!");
					return;
				}

				this.useSkill(admin, (Creature) target, (Creature) target.getTarget(), skillId, skillLevel);
				PacketSendUtility.sendMessage(admin, "Target: " + target.getName() + " used skillId:" + skillId + " on its target "
					+ target.getTarget().getName());
			} else {
				info(admin, null);
				return;
			}
		} else {
			this.useSkill(admin, admin, (Creature) target, skillId, skillLevel);
			PacketSendUtility.sendMessage(admin, "SkillId:" + skillId + " was used on target " + target.getName());
		}
	}

	private void useSkill(Player admin, Creature effector, Creature target, int skillId, int skillLevel) {
		Skill skill = SkillEngine.getInstance().getSkill(effector, skillId, skillLevel, target);
		if (skill != null) {
			skill.useNoAnimationSkill();
		} else
			info(admin, null);
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, syntax + " \n" + "or use //useskill help.");
	}
}
