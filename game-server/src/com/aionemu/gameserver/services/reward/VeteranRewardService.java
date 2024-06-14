package com.aionemu.gameserver.services.reward;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dao.VeteranRewardDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Neon
 */
public final class VeteranRewardService {

	private static final List<List<RewardItem>> rewards = new ArrayList<>();

	private static final List<RewardItem> randomRewards = new ArrayList<>();

	private static final int RANDOM_ITEMS_PER_MONTH = 4;

	static {
		for (int i = 0; i < 60; i++)
			rewards.add(new ArrayList<>());
		// month 1
		rewards.get(0).add(new RewardItem(169630007, 1)); // [Expand Card] Expand Cube Ticket (lvl 4)
		rewards.get(0).add(new RewardItem(169620094, 1)); // Crafting Boost Charm III - 100%
		rewards.get(0).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(0).add(new RewardItem(162000124, 50)); // Superior Recovery Serum

		// month 2
		rewards.get(1).add(new RewardItem(190020075, 1)); // Flash Bogel Egg
		rewards.get(1).add(new RewardItem(169600064, 1)); // [Emotion Card] Playing Dead
		rewards.get(1).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(1).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 3
		rewards.get(2).add(new RewardItem(125040038, 1)); // Devil Horns
		rewards.get(2).add(new RewardItem(186000199, 100)); // Legion Coin
		rewards.get(2).add(new RewardItem(166000195, 5)); // Epsilon Enchantment Stone
		rewards.get(2).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 4
		rewards.get(3).add(new RewardItem(169640006, 1)); // [Expand Card] Expand Warehouse Ticket (lvl 4)
		rewards.get(3).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(3).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(3).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 5
		rewards.get(4).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(4).add(new RewardItem(169600103, 1)); // [Emotion Card] Diving
		rewards.get(4).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(4).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(4).add(new RewardItem(186000051, 5)); // Major Ancient Crown

		// month 6
		rewards.get(5).add(new RewardItem(187000057, 1)); // Kahrun's Wing
		rewards.get(5).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(5).add(new RewardItem(164002264, 25)); // Flame Pillar Firecracker
		rewards.get(5).add(new RewardItem(169670000, 1)); // Name Change Ticket

		// month 7
		rewards.get(6).add(new RewardItem(169630007, 1)); // [Expand Card] Expand Cube Ticket (lvl 4)
		rewards.get(6).add(new RewardItem(169600065, 1)); // [Emotion Card] Sing
		rewards.get(6).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%
		rewards.get(6).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(6).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 8
		rewards.get(7).add(new RewardItem(190000048, 1)); // Golden Nyanco Egg
		rewards.get(7).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(7).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(7).add(new RewardItem(186000199, 100)); // Legion Coin

		// month 9
		rewards.get(8).add(new RewardItem(166000195, 5)); // Epsilon Enchantment Stone
		rewards.get(8).add(new RewardItem(169600087, 1)); // [Emotion Card] 'Bad Girl' Dance
		rewards.get(8).add(new RewardItem(188052761, 5)); // [Event] Bonus Entry Scroll Bundle
		rewards.get(8).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 10
		rewards.get(9).add(new RewardItem(169640006, 1)); // [Expand Card] Expand Warehouse Ticket (lvl 4)
		rewards.get(9).add(new RewardItem(169650007, 1)); // [Event] Plastic Surgery Ticket
		rewards.get(9).add(new RewardItem(186000051, 5)); // Major Ancient Crown
		rewards.get(9).add(new RewardItem(161001001, 5)); // Revival Stone

		// month 11
		rewards.get(10).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(10).add(new RewardItem(164002284, 25)); // [Event] Ornate Firecrackers
		rewards.get(10).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(10).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 12
		rewards.get(11).add(new RewardItem(190100107, 1)); // Emerald Crestlich
		rewards.get(11).add(new RewardItem(169600062, 1)); // [Emotion Card] Play Harp
		rewards.get(11).add(new RewardItem(169610343, 1)); // [Title] Forgotten Hero
		rewards.get(11).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone

		// month 13
		rewards.get(12).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(12).add(new RewardItem(169660003, 1)); // [Event] Gender Switch Ticket
		rewards.get(12).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(12).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 14
		rewards.get(13).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(13).add(new RewardItem(169600063, 1)); // [Emotion Card] Play the Saxophone
		rewards.get(13).add(new RewardItem(188052761, 5)); // [Event] Bonus Entry Scroll Bundle
		rewards.get(13).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 15
		rewards.get(14).add(new RewardItem(110900876, 1)); // Nyerkcarrier
		rewards.get(14).add(new RewardItem(190020156, 1)); // [Event] Medalist Shugo Egg
		rewards.get(14).add(new RewardItem(166000195, 5)); // Epsilon Enchantment Stone
		rewards.get(14).add(new RewardItem(161001001, 5)); // Revival Stone

		// month 16
		rewards.get(15).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(15).add(new RewardItem(169600060, 1)); // [Emotion Card] Play the Drum
		rewards.get(15).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(15).add(new RewardItem(186000051, 5)); // Major Ancient Crown
		rewards.get(15).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 17
		rewards.get(16).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(16).add(new RewardItem(164002284, 25)); // [Event] Ornate Firecrackers
		rewards.get(16).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(16).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(16).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 18
		rewards.get(17).add(new RewardItem(187060162, 1)); // Wings of Agony
		rewards.get(17).add(new RewardItem(168310018, 1)); // Major Blessed Augment: Level 2
		rewards.get(17).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(17).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 19
		rewards.get(18).add(new RewardItem(125050026, 1)); // Elcoro Hat
		rewards.get(18).add(new RewardItem(186000077, 1)); // Hot Heart of Magic
		rewards.get(18).add(new RewardItem(186000247, 5)); // Major Danuar Relic
		rewards.get(18).add(new RewardItem(161001001, 5)); // Revival Stone

		// month 20
		rewards.get(19).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(19).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(19).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(19).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(19).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 21
		rewards.get(20).add(new RewardItem(169630007, 1)); // [Expand Card] Expand Cube Ticket (lvl 4)
		rewards.get(20).add(new RewardItem(169600039, 1)); // [Emotion Card] Chew Bubblegum
		rewards.get(20).add(new RewardItem(186000238, 150)); // Conqueror's Herb
		rewards.get(20).add(new RewardItem(162000107, 50)); // Saam King's Herb

		// month 22
		rewards.get(21).add(new RewardItem(169640006, 1)); // [Expand Card] Expand Warehouse Ticket (lvl 4)
		rewards.get(21).add(new RewardItem(152012593, 3)); // Valor's Heart
		rewards.get(21).add(new RewardItem(152012587, 3)); // Wind Eternity
		rewards.get(21).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(21).add(new RewardItem(164002284, 25)); // [Event] Ornate Firecrackers

		// month 23
		rewards.get(22).add(new RewardItem(188508017, 1)); // [Motion Card] Stormbringer
		rewards.get(22).add(new RewardItem(188053609, 3)); // [Event] Level 60 Composite Manastone Bundle
		rewards.get(22).add(new RewardItem(166200009, 3)); // Mythic Weapon Tuning Scroll
		rewards.get(22).add(new RewardItem(166200010, 3)); // Mythic Armor Tuning Scroll

		// month 24
		rewards.get(23).add(new RewardItem(169650007, 1)); // [Event] Plastic Surgery Ticket
		rewards.get(23).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(23).add(new RewardItem(152012586, 2)); // Wind Breath
		rewards.get(23).add(new RewardItem(152012581, 2)); // Fire Breath
		rewards.get(23).add(new RewardItem(186000238, 150)); // Conqueror's Mark

		// month 25
		rewards.get(24).add(new RewardItem(169630007, 1)); // [Expand Card] Expand Cube Ticket (lvl 4)
		rewards.get(24).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(24).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%
		rewards.get(24).add(new RewardItem(162000107, 50)); // Saam King's Herb

		// month 26
		rewards.get(25).add(new RewardItem(169640006, 1)); // [Expand Card] Expand Warehouse Ticket (lvl 4)
		rewards.get(25).add(new RewardItem(152012593, 3)); // Valor's Heart
		rewards.get(25).add(new RewardItem(152012590, 3)); // Wind Origin
		rewards.get(25).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(25).add(new RewardItem(166000195, 10)); // Epsilon Enchantment Stone

		// month 27
		rewards.get(26).add(new RewardItem(188500014, 1)); // [Motion Card] The Dragon's Set
		rewards.get(26).add(new RewardItem(186000247, 5)); // Major Danuar Relic
		rewards.get(26).add(new RewardItem(164002116, 25)); // [Event] Rx: Accelerox
		rewards.get(26).add(new RewardItem(164002117, 25)); // [Event] Rx: Blitzopan
		rewards.get(26).add(new RewardItem(164002118, 25)); // [Event] Rx: Castafodin

		// month 28
		rewards.get(27).add(new RewardItem(110900731, 1)); // Cogwheel Couture
		rewards.get(27).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(27).add(new RewardItem(152012586, 2)); // Wind Breath
		rewards.get(27).add(new RewardItem(152012581, 2)); // Fire Breath

		// month 29
		rewards.get(28).add(new RewardItem(169600186, 1)); // [Emotion Card] Sing "Good Day"
		rewards.get(28).add(new RewardItem(166200009, 3)); // Mythic Weapon Tuning Scroll
		rewards.get(28).add(new RewardItem(166200010, 3)); // Mythic Armor Tuning Scroll
		rewards.get(28).add(new RewardItem(162000107, 50)); // Saam King's Herb

		// month 30
		rewards.get(29).add(new RewardItem(169610137, 1)); // [Title Card] Aion's Chosen
		rewards.get(29).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(29).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(29).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 31
		rewards.get(30).add(new RewardItem(166030007, 3)); // [Event] Tempering Solution
		rewards.get(30).add(new RewardItem(166000195, 10)); // Epsilon Enchantment Stone
		rewards.get(30).add(new RewardItem(164002116, 25)); // [Event] Rx: Accelerox
		rewards.get(30).add(new RewardItem(164002117, 25)); // [Event] Rx: Blitzopan
		rewards.get(30).add(new RewardItem(164002118, 25)); // [Event] Rx: Castafodin

		// month 32
		rewards.get(31).add(new RewardItem(169600086, 1)); // [Emotion Card] 'Shut Up' Dance
		rewards.get(31).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(31).add(new RewardItem(188053609, 3)); // [Event] Level 60 Composite Manastone Bundle
		rewards.get(31).add(new RewardItem(186000247, 5)); // Major Danuar Relic
		rewards.get(31).add(new RewardItem(188052761, 5)); // [Event] Bonus Entry Scroll Bundle

		// month 33
		rewards.get(32).add(new RewardItem(187060178, 1)); // Aether Glider
		rewards.get(32).add(new RewardItem(188053295, 1)); // Empyrean Plume Chest
		rewards.get(32).add(new RewardItem(162000107, 50)); // Saam King's Herb
		rewards.get(32).add(new RewardItem(161001001, 5)); // Revival Stone

		// month 34
		rewards.get(33).add(new RewardItem(168310018, 1)); // Major Blessed Augment: Level 2
		rewards.get(33).add(new RewardItem(188052638, 1)); // [Event] Fabled Godstone Bundle
		rewards.get(33).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(33).add(new RewardItem(164002284, 25)); // [Event] Ornate Firecrackers

		// month 35
		rewards.get(34).add(new RewardItem(169600102, 1)); // [Emotion Card] Floor Sweep
		rewards.get(34).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(34).add(new RewardItem(164002272, 25)); // [Event] Enduring Greater Raging Wind Scroll
		rewards.get(34).add(new RewardItem(162000141, 25)); // Sublime Wind Serum
		rewards.get(34).add(new RewardItem(186000238, 150)); // Conqueror's Mark

		// month 36
		rewards.get(35).add(new RewardItem(190100042, 1)); // Legion Pagati
		rewards.get(35).add(new RewardItem(166030007, 3)); // [Event] Tempering Solution
		rewards.get(35).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(35).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 37
		rewards.get(36).add(new RewardItem(169650007, 1)); // [Event] Plastic Surgery Ticket
		rewards.get(36).add(new RewardItem(186000247, 5)); // Major Danuar Relic
		rewards.get(36).add(new RewardItem(164002116, 25)); // [Event] Rx: Accelerox
		rewards.get(36).add(new RewardItem(164002117, 25)); // [Event] Rx: Blitzopan
		rewards.get(36).add(new RewardItem(164002118, 25)); // [Event] Rx: Castafodin

		// month 38
		rewards.get(37).add(new RewardItem(165020016, 1)); // Accessory Wrapping Scroll (Eternal/Lv. 65 and lower)
		rewards.get(37).add(new RewardItem(188053610, 3)); // [Event] Level 70 Composite Manastone Bundle
		rewards.get(37).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(37).add(new RewardItem(186000399, 100)); // Honorable Conqueror's Mark

		// month 39
		rewards.get(38).add(new RewardItem(110900695, 1)); // Biker Costume
		rewards.get(38).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(38).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%
		rewards.get(38).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 40
		rewards.get(39).add(new RewardItem(125045415, 1)); // Biker Hat
		rewards.get(39).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(39).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(39).add(new RewardItem(186000199, 150)); // Legion Coin

		// month 41
		rewards.get(40).add(new RewardItem(165020015, 1)); // Armor Wrapping Scroll (Eternal/Lv. 65 and lower)
		rewards.get(40).add(new RewardItem(152012593, 3)); // Valor's Heart
		rewards.get(40).add(new RewardItem(152012587, 3)); // Wind Eternity
		rewards.get(40).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone

		// month 42
		rewards.get(41).add(new RewardItem(168310018, 1)); // Major Blessed Augment: Level 2
		rewards.get(41).add(new RewardItem(186000051, 5)); // Major Ancient Crown
		rewards.get(41).add(new RewardItem(166200009, 3)); // Mythic Weapon Tuning Scroll
		rewards.get(41).add(new RewardItem(166200010, 3)); // Mythic Armor Tuning Scroll

		// month 43
		rewards.get(42).add(new RewardItem(188508005, 1)); // [Motion Card] Socialite
		rewards.get(42).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(42).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%
		rewards.get(42).add(new RewardItem(152012590, 3)); // Wind Origin
		rewards.get(42).add(new RewardItem(186000238, 150)); // Conqueror's Mark

		// month 44
		rewards.get(43).add(new RewardItem(165020014, 1)); // Weapon Wrapping Scroll (Eternal/Lv. 65 and lower)
		rewards.get(43).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(43).add(new RewardItem(188053610, 3)); // [Event] Level 70 Composite Manastone Bundle
		rewards.get(43).add(new RewardItem(186000247, 5)); // Major Danuar Relic
		rewards.get(43).add(new RewardItem(188052761, 5)); // [Event] Bonus Entry Scroll Bundle

		// month 45
		rewards.get(44).add(new RewardItem(169600217, 1)); // [Emotion Card] Summer Vacation
		rewards.get(44).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(44).add(new RewardItem(164002272, 25)); // [Event] Enduring Greater Raging Wind Scroll
		rewards.get(44).add(new RewardItem(162000141, 25)); // Sublime Wind Serum

		// month 46
		rewards.get(45).add(new RewardItem(170100041, 1)); // Club Speaker Cabinet
		rewards.get(45).add(new RewardItem(152012586, 2)); // Wind Breath
		rewards.get(45).add(new RewardItem(152012581, 2)); // Fire Breath
		rewards.get(45).add(new RewardItem(166000195, 10)); // Epsilon Enchantment Stone

		// month 47
		rewards.get(46).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(46).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(46).add(new RewardItem(186000242, 5)); // Ceramium Medal
		rewards.get(46).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(46).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 48
		rewards.get(47).add(new RewardItem(169610158, 1)); // [Title Card] Prestigious Adept
		rewards.get(47).add(new RewardItem(162000107, 50)); // Saam King's Herbs
		rewards.get(47).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(47).add(new RewardItem(186000399, 100)); // Honorable Conqueror's Mark

		// month 49
		rewards.get(48).add(new RewardItem(169600098, 1)); // [Emotion Card] Hug Me
		rewards.get(48).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(48).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(48).add(new RewardItem(186000247, 5)); // Major Danuar Relic
		rewards.get(48).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 50
		rewards.get(49).add(new RewardItem(165020015, 1)); // Armor Wrapping Scroll (Eternal/Lv. 65 and lower)
		rewards.get(49).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(49).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(49).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 51
		rewards.get(50).add(new RewardItem(110900603, 1)); // Lawful Uniform
		rewards.get(50).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(50).add(new RewardItem(164002116, 25)); // [Event] Rx: Accelerox
		rewards.get(50).add(new RewardItem(164002117, 25)); // [Event] Rx: Blitzopan
		rewards.get(50).add(new RewardItem(164002118, 25)); // [Event] Rx: Castafodin

		// month 52
		rewards.get(51).add(new RewardItem(125045283, 1)); // Lawful Headgear
		rewards.get(51).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(51).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(51).add(new RewardItem(164002284, 15)); // [Event] Ornate Firecrackers
		rewards.get(51).add(new RewardItem(186000409, 150)); // Daeva's Respite Coin

		// month 53
		rewards.get(52).add(new RewardItem(165020016, 1)); // Accessory Wrapping Scroll (Eternal/Lv. 65 and lower)
		rewards.get(52).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(52).add(new RewardItem(186000051, 5)); // Major Ancient Crown
		rewards.get(52).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle

		// month 54
		rewards.get(53).add(new RewardItem(169670000, 1)); // Name Change Ticket
		rewards.get(53).add(new RewardItem(162000107, 50)); // Saam King's Herb
		rewards.get(53).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(53).add(new RewardItem(164002272, 25)); // [Event] Enduring Greater Raging Wind Scroll
		rewards.get(53).add(new RewardItem(162000141, 25)); // Sublime Wind Serum

		// month 55
		rewards.get(54).add(new RewardItem(168310018, 1)); // Major Blessed Augment: Level 2
		rewards.get(54).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(54).add(new RewardItem(162000139, 25)); // Sublime Mana Serum
		rewards.get(54).add(new RewardItem(188053610, 3)); // [Event] Level 70 Composite Manastone Bundle
		rewards.get(54).add(new RewardItem(164002284, 15)); // [Event] Ornate Firecrackers

		// month 56
		rewards.get(55).add(new RewardItem(165020014, 1)); // Weapon Wrapping Scroll (Eternal/Lv. 65 and lower)
		rewards.get(55).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(55).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%
		rewards.get(55).add(new RewardItem(188053618, 1)); // Honorable Elim's Idian Bundle

		// month 57
		rewards.get(56).add(new RewardItem(166200009, 3)); // Mythic Weapon Tuning Scroll
		rewards.get(56).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(56).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(56).add(new RewardItem(166000195, 10)); // Epsilon Enchantment Stone

		// month 58
		rewards.get(57).add(new RewardItem(190010001, 1)); // Potbelly Inquin Egg
		rewards.get(57).add(new RewardItem(188052761, 5)); // [Event] Bonus Entry Scroll Bundle
		rewards.get(57).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(57).add(new RewardItem(186000247, 5)); // Major Danuar Relic
		rewards.get(57).add(new RewardItem(166150026, 2)); // [Stamp] Greater Felicitous Socketing (Heroic)

		// month 59
		rewards.get(58).add(new RewardItem(166200010, 3)); // Mythic Armor Tuning Scroll
		rewards.get(58).add(new RewardItem(166000195, 15)); // Epsilon Enchantment Stone
		rewards.get(58).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(58).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution

		// month 60
		rewards.get(59).add(new RewardItem(188053996, 1)); // Emperor Trillirunerk's Feather Box
		rewards.get(59).add(new RewardItem(162000107, 50)); // Saam King's Herb
		rewards.get(59).add(new RewardItem(186000399, 125)); // Honorable Conqueror's Mark
		rewards.get(59).add(new RewardItem(166150027, 2)); // [Stamp] Greater Felicitous Socketing (Mythic)

		// random rewards for month 61+
		randomRewards.add(new RewardItem(161001001, 5)); // Revival Stone
		randomRewards.add(new RewardItem(162000137, 15)); // Sublime Life Serum
		randomRewards.add(new RewardItem(162000139, 15)); // Sublime Mana Serum
		randomRewards.add(new RewardItem(162000141, 15)); // Sublime Wind Serum
		randomRewards.add(new RewardItem(164002167, 15)); // Drana Coffee
		randomRewards.add(new RewardItem(188054198, 3)); // Greater Scroll Bundle
		randomRewards.add(new RewardItem(186000051, 5)); // Major Ancient Crown
		randomRewards.add(new RewardItem(186000247, 5)); // Major Danuar Relic
		randomRewards.add(new RewardItem(188053666, 2)); // [Event] Ceramium Medal Box
		randomRewards.add(new RewardItem(188053667, 1)); // [Event] Mithril Medal Box
		randomRewards.add(new RewardItem(186000243, 10)); // Fragmented Ceramium
		randomRewards.add(new RewardItem(186000236, 75)); // Blood Mark
		randomRewards.add(new RewardItem(188053610, 3)); // [Event] Level 70 Composite Manastone Bundle
		randomRewards.add(new RewardItem(169620094, 1)); // Crafting Boost Charm III - 100%
		randomRewards.add(new RewardItem(169620082, 1)); // Gathering Boost Charm II - 100%
		randomRewards.add(new RewardItem(169620072, 1)); // AP Boost Charm II - 30%
		randomRewards.add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		randomRewards.add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		randomRewards.add(new RewardItem(166500005, 5)); // [Event] Amplification Stone
		randomRewards.add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		randomRewards.add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		randomRewards.add(new RewardItem(186000238, 150)); // Conqueror's Herb
		randomRewards.add(new RewardItem(186000399, 125)); // Honorable Conqueror's Mark
		randomRewards.add(new RewardItem(186000409, 50)); // Daeva's Respite Coin
		randomRewards.add(new RewardItem(188052761, 3)); // [Event] Bonus Entry Scroll Bundle
		randomRewards.add(new RewardItem(166150018, 3)); // Assured Greater Felicitous Socketing (Eternal)
		randomRewards.add(new RewardItem(166150019, 3)); // Assured Greater Felicitous Socketing (Mythic)
		randomRewards.add(new RewardItem(166100020, 250)); // [Stamp] High Grade Enchanting Supplement (Eternal)
		randomRewards.add(new RewardItem(166100023, 250)); // [Stamp] High Grade Enchanting Supplement (Mythic)
	}

	/**
	 * Prevent instantiation
	 */
	private VeteranRewardService() {
	}

	public static VeteranRewardService getInstance() {
		return SingletonHolder.instance;
	}

	public void tryReward(Player player) {
		if (player.getLevel() != 65)
			return;

		ZonedDateTime now = ServerTime.now();
		ZonedDateTime charCreationTime = ServerTime.atDate(player.getCreationDate());
		if (ChronoUnit.MONTHS.between(charCreationTime, now) < 1) // return if char is younger than a month
			return;

		ZonedDateTime accCreationTime = ServerTime.ofEpochMilli(player.getAccount().getCreationDate());
		int maxMonthsToReceive = (int) ChronoUnit.MONTHS.between(accCreationTime, now);
		if (maxMonthsToReceive < 1) // return if account is younger than a month
			return;

		int receivedMonths = VeteranRewardDAO.loadReceivedMonths(player); // -1 means error
		if (receivedMonths < 0 || receivedMonths >= maxMonthsToReceive)
			return;

		if (VeteranRewardDAO.storeReceivedMonths(player, maxMonthsToReceive))
			for (int i = receivedMonths; i < maxMonthsToReceive; i++) {
				List<RewardItem> items;
				if (i < 60) {
					items = rewards.get(i);
				} else {
					items = new ArrayList<>(randomRewards);
					while (items.size() > RANDOM_ITEMS_PER_MONTH)
						items.remove(Rnd.nextInt(items.size()));
				}
				if (player.getMailbox().getLetters().size() >= 100) { // abort on mailbox overflow and save the correct month
					VeteranRewardDAO.storeReceivedMonths(player, i);
					return;
				}
				for (RewardItem item : items)
					SystemMailService.sendMail("Beyond Aion", player.getName(), "Veteran Reward",
						"Greetings Daeva!\n\nIt has been over " + (i == 0 ? "a month" : (i + 1) + " months")
							+ " now, since you joined us.\nWe send you this and hope you stay with us even longer :)\n\n~ Beyond Aion",
						item.getId(), item.getCount(), 0, LetterType.BLACKCLOUD);
			}
	}

	private static final class SingletonHolder {

		static final VeteranRewardService instance = new VeteranRewardService();
	}
}
