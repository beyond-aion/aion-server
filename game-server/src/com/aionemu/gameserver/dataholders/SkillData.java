package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.skillengine.effect.ArmorMasteryEffect;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.ShieldMasteryEffect;
import com.aionemu.gameserver.skillengine.effect.WeaponMasteryEffect;
import com.aionemu.gameserver.skillengine.model.Motion;
import com.aionemu.gameserver.skillengine.model.MotionTime;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer, Neon
 */
@XmlRootElement(name = "skill_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillData {

	/**
	 * Motions of skills that have no attack animation with a hit point on any model of the client, so they keep the default hit time
	 */
	private static final Set<String> MOTIONS_WITHOUT_ANIMATION = Set.of("areaatklh", "areaatkrh", "areafireod", "axe", "breathl4s", "breathl8s",
		"breathm4s", "breathm8s", "breathr4s", "breathr8s", "cash_social_diving", "cidle", "earthquake", "herb", "mine", "normalfirehd", "normalfiremo",
		"open", "pointfirelh", "pointfirerh", "quest_drinkpoint", "sanctuaryfire", "say", "sumrobot", "talk", "transform");

	@XmlElement(name = "skill_template")
	private List<SkillTemplate> skillTemplates;

	@XmlTransient
	private final Map<Integer, SkillTemplate> skillTemplateById = new HashMap<>();

	@XmlTransient
	private final Map<String, List<SkillTemplate>> skillTemplatesByGroup = new LinkedHashMap<>();

	@XmlTransient
	private final Map<String, List<SkillTemplate>> skillTemplatesByStack = new LinkedHashMap<>();

	@XmlTransient
	private final Map<ItemGroup, Set<Integer>> masterySkillsByWeapon = new EnumMap<>(ItemGroup.class);

	@XmlTransient
	private final Map<ItemSubType, Set<Integer>> masterySkillsByArmor = new EnumMap<>(ItemSubType.class);

	@XmlTransient
	private final Set<Integer> shieldMasterySkills = new HashSet<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		skillTemplateById.clear();
		skillTemplatesByGroup.clear();
		skillTemplatesByStack.clear();
		masterySkillsByWeapon.clear();
		masterySkillsByArmor.clear();
		shieldMasterySkills.clear();
		for (SkillTemplate skillTemplate : skillTemplates) {
			int skillId = skillTemplate.getSkillId();
			skillTemplateById.put(skillId, skillTemplate);
			if (skillTemplate.getGroup() != null)
				skillTemplatesByGroup.computeIfAbsent(skillTemplate.getGroup(), k -> new ArrayList<>()).add(skillTemplate);
			if (skillTemplate.getStack() != null)
				skillTemplatesByStack.computeIfAbsent(skillTemplate.getStack(), k -> new ArrayList<>()).add(skillTemplate);
			if (skillTemplate.getEffects() != null)
				indexMasterySkills(skillId, skillTemplate.getEffects().getEffects());
		}
		skillTemplates = null;
	}

	private void indexMasterySkills(int skillId, List<EffectTemplate> effects) {
		for (EffectTemplate effect : effects) {
			switch (effect) {
				case WeaponMasteryEffect e -> {
					if (e.getItemGroup() == null)
						throw new IllegalArgumentException("Weapon mastery effect of skill " + skillId + " has no weapon attribute");
					masterySkillsByWeapon.computeIfAbsent(e.getItemGroup(), _ -> new HashSet<>()).add(skillId);
				}
				case ArmorMasteryEffect e -> {
					if (e.getArmorType() == null)
						throw new IllegalArgumentException("Armor mastery effect of skill " + skillId + " has no armor attribute");
					masterySkillsByArmor.computeIfAbsent(e.getArmorType(), _ -> new HashSet<>()).add(skillId);
				}
				case ShieldMasteryEffect _ -> shieldMasterySkills.add(skillId);
				default -> {
				}
			}
		}
	}

	/**
	 * @return The skills that allow equipping items of this group, empty if the group needs no mastery skill.
	 */
	public Set<Integer> getMasterySkills(ItemGroup itemGroup) {
		if (itemGroup == null || !itemGroup.requiresMastery())
			return Set.of();
		if (itemGroup == ItemGroup.SHIELD)
			return shieldMasterySkills;
		if (itemGroup.getEquipType() == EquipType.WEAPON)
			return masterySkillsByWeapon.getOrDefault(itemGroup, Set.of());
		return masterySkillsByArmor.getOrDefault(itemGroup.getItemSubType(), Set.of());
	}

	public SkillTemplate getSkillTemplate(int skillId) {
		return skillTemplateById.get(skillId);
	}

	/**
	 * @return All skill templates of this group. A group is less precise and may be null.
	 */
	public List<SkillTemplate> getSkillTemplatesByGroup(String skillGroup) {
		return skillTemplatesByGroup.get(skillGroup);
	}

	/**
	 * @return All skill templates of this stack. A stack is more precise than a group.
	 *         For example: Charge skills have their own stack per charging level. Also, skills that are similar for both factions have the same group,
	 *         but different stacks.
	 */
	public List<SkillTemplate> getSkillTemplatesByStack(String skillStack) {
		return skillTemplatesByStack.get(skillStack);
	}

	public int size() {
		return skillTemplateById.size();
	}

	public Collection<SkillTemplate> getSkillTemplates() {
		return skillTemplateById.values();
	}

	public void validateMotions() {
		Set<String> missing = new TreeSet<>();
		for (SkillTemplate t : getSkillTemplates()) {
			Motion m = t.getMotion();
			if (m != null && m.getName() != null && DataManager.MOTION_DATA.getMotionTime(m.getName()) == null)
				missing.add(m.getName());
		}
		Set<String> unexpected = new TreeSet<>(missing);
		unexpected.removeAll(MOTIONS_WITHOUT_ANIMATION);
		if (!unexpected.isEmpty())
			LoggerFactory.getLogger(SkillData.class).warn("Missing motion times for these motion names: {}", unexpected);
		Set<String> stale = new TreeSet<>(MOTIONS_WITHOUT_ANIMATION);
		stale.removeAll(missing);
		if (!stale.isEmpty())
			LoggerFactory.getLogger(SkillData.class).warn("These motion names are listed as without animation, but have motion times or no skill uses them: {}", stale);
	}
}
