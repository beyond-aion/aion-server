package consolecommands;

import java.io.File;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author ginho1
 */
public class Combineskill extends ConsoleCommand {

	public Combineskill() {
		super("combineskill");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

		String skillName = params[0];
		int skillId = 0;
		int skillLvl = 1;

		try {
			skillLvl = Integer.parseInt(params[1]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "Parameters need to be an integer.");
			return;
		}

		File xml = new File("./data/handlers/consolecommands/data/skills.xml");
		SkillData data = JAXBUtil.deserialize(xml, SkillData.class);
		SkillTemplate skillTemplate = data.getSkillTemplate(skillName);

		if (skillTemplate != null)
			skillId = skillTemplate.getTemplateId();

		if (skillId > 0) {
			player.getSkillList().addSkill(player, skillId, skillLvl);
			PacketSendUtility.sendMessage(admin, "You have success add skill");
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
