package com.aionemu.gameserver.utils.stats;

/**
 * @author Yeats
 */
public enum CalculationType {
    SKILL,
    APPLY_POWER_SHARD_DAMAGE, // adds power shard damage to calculations without consuming power shards
    REMOVE_POWER_SHARD, // consumes power shards, can only be used in combination with APPLY_POWER_SHARD_DAMAGE
    DISPLAY, // calculations that are displayed in players profile etc.
    DUAL_WIELD,
    MAIN_HAND,
    OFF_HAND
}
