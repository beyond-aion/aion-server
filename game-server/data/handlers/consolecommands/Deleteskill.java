package consolecommands;

import java.io.File;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * Sent in the following cases:<br>
 * - Clicking "Remove all skills" in the GM Panel (Shift + F1)<br>
 * - Pressing Ctrl + Shift + Alt while clicking on a skill in the skill search results, or from the player's regular skill list if the console has
 *   been activated via "\con_disable_console 0" from the command tab of the GM Panel (Shift + F1)<br>
 *
 * @author ginho1
 */
public class Deleteskill extends ConsoleCommand {

	public Deleteskill() {
		super("deleteskill");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		String skillName = params[0];
		int skillId = 0;

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		Player player = (Player) target;
		File xml = new File("./data/handlers/consolecommands/data/skills.xml");
		SkillData data = JAXBUtil.deserialize(xml, SkillData.class);
		SkillTemplate skillTemplate = data.getSkillTemplate(skillName);

		if (skillTemplate != null)
			skillId = skillTemplate.getTemplateId();

		if (skillId > 0 && check(admin, player, skillId))
			apply(admin, player, skillId);
	}

	private static boolean check(Player admin, Player player, int skillId) {
		if (skillId != 0 && !player.getSkillList().isSkillPresent(skillId)) {
			PacketSendUtility.sendMessage(admin, "Player dont have this skill.");
			return false;
		}
		if (player.getSkillList().getSkillEntry(skillId).isStigmaSkill()) {
			PacketSendUtility.sendMessage(admin, "You can't remove stigma skill.");
			return false;
		}
		return true;
	}

	public void apply(Player admin, Player player, int skillId) {
		if (skillId != 0) {
			SkillLearnService.removeSkill(player, skillId);
			PacketSendUtility.sendMessage(admin, "You have successfully deleted the specified skill.");
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///addcskill <skill name>");
	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "", name = "SkillTemplate")
	private static class SkillTemplate {

		@XmlAttribute(name = "id", required = true)
		@XmlID
		private String id;

		@XmlAttribute(name = "name")
		private String name;

		public String getName() {
			return name;
		}

		public int getTemplateId() {
			return skillId;
		}

		private int skillId;

		public void setSkillId(int skillId) {
			this.skillId = skillId;
		}

		@SuppressWarnings("unused")
		void afterUnmarshal(Unmarshaller u, Object parent) {
			setSkillId(Integer.parseInt(id));
		}

	}

	@XmlRootElement(name = "skills")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SkillData {

		@XmlElement(name = "skill")
		private List<SkillTemplate> its;

		public SkillTemplate getSkillTemplate(String skill) {

			for (SkillTemplate it : getData()) {
				if (it.getName().toLowerCase().equals(skill.toLowerCase()))
					return it;
			}
			return null;
		}

		protected List<SkillTemplate> getData() {
			return its;
		}
	}
}
