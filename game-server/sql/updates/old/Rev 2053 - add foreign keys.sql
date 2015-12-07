alter table `craft_cooldowns` change `player_id` `player_id` int(11) NOT NULL;
   
alter table `old_names` change `player_id` `player_id` int(11) NOT NULL;

delete FROM `mail` WHERE `mail_recipient_id` NOT IN (SELECT `id` FROM `players`);
alter table `mail` add constraint `FK_mail` FOREIGN KEY (`mail_recipient_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;

delete FROM `player_life_stats` WHERE `player_id` NOT IN (SELECT `id` FROM `players`);
alter table `player_life_stats` add constraint `FK_player_life_stats` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;

delete FROM `player_pets` WHERE `player_id` NOT IN (SELECT `id` FROM `players`);
alter table `player_pets` add constraint `FK_player_pets` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;

delete FROM `craft_cooldowns` WHERE `player_id` NOT IN (SELECT `id` FROM `players`);
alter table `craft_cooldowns` add constraint `FK_craft_cooldowns` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;

delete FROM `web_reward` WHERE `item_owner` NOT IN (SELECT `id` FROM `players`);
alter table `web_reward` add constraint `FK_web_reward` FOREIGN KEY (`item_owner`) REFERENCES `players` (`id`) ON DELETE CASCADE;

delete FROM `broker` WHERE `seller_id` NOT IN (SELECT `id` FROM `players`);
alter table `broker` add constraint `FK_broker` FOREIGN KEY (`seller_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;

delete FROM `old_names` WHERE `player_id` NOT IN (SELECT `id` FROM `players`);
alter table `old_names` add constraint `FK_old_names` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;
