package com.aionemu.gameserver.configs.main;

import java.util.Map;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Properties;
import com.aionemu.commons.configuration.Property;
import com.aionemu.gameserver.model.templates.housing.HouseType;

/**
 * @author Rolandas
 */
public class HousingConfig {

	/**
	 * Distance Visibility
	 */
	@Property(key = "gameserver.housing.visibility.distance", defaultValue = "200")
	public static float VISIBILITY_DISTANCE = 200f;

	@Property(key = "gameserver.housing.auction.enable", defaultValue = "true")
	public static boolean ENABLE_HOUSE_AUCTIONS;

	@Property(key = "gameserver.housing.pay.enable", defaultValue = "true")
	public static boolean ENABLE_HOUSE_PAY;

	@Property(key = "gameserver.housing.auction.end_time", defaultValue = "0 0 12 ? * SUN")
	public static CronExpression HOUSE_AUCTION_END_TIME;

	@Property(key = "gameserver.housing.auction.register_days", defaultValue = "1, 5")
	public static int[] HOUSE_AUCTION_REGISTER_DAYS;

	@Property(key = "gameserver.housing.maintain.time", defaultValue = "0 0 0 ? * MON")
	public static CronExpression HOUSE_MAINTENANCE_TIME;

	/** Auction default bid prices **/
	@Property(key = "gameserver.housing.auction.default_bid.house", defaultValue = "0")
	public static int HOUSE_MIN_BID;
	@Property(key = "gameserver.housing.auction.default_bid.mansion", defaultValue = "0")
	public static int MANSION_MIN_BID;
	@Property(key = "gameserver.housing.auction.default_bid.estate", defaultValue = "0")
	public static int ESTATE_MIN_BID;
	@Property(key = "gameserver.housing.auction.default_bid.palace", defaultValue = "0")
	public static int PALACE_MIN_BID;

	/** Auction minimal level required for bidding **/
	@Property(key = "gameserver.housing.auction.bidding.min_level.house", defaultValue = "0")
	public static int HOUSE_MIN_BID_LEVEL;
	@Property(key = "gameserver.housing.auction.bidding.min_level.mansion", defaultValue = "0")
	public static int MANSION_MIN_BID_LEVEL;
	@Property(key = "gameserver.housing.auction.bidding.min_level.estate", defaultValue = "0")
	public static int ESTATE_MIN_BID_LEVEL;
	@Property(key = "gameserver.housing.auction.bidding.min_level.palace", defaultValue = "0")
	public static int PALACE_MIN_BID_LEVEL;

	@Property(key = "gameserver.housing.auction.registration_fee", defaultValue = "0.3")
	public static float AUCTION_REGISTRATION_FEE_PERCENT;
	@Property(key = "gameserver.housing.auction.sales_commission", defaultValue = "0.1")
	public static float AUCTION_SALES_COMMISION_PERCENT;
	@Property(key = "gameserver.housing.auction.grace_end_refund", defaultValue = "0.5")
	public static float AUCTION_GRACE_END_REFUND_PERCENT;

	@Property(key = "gameserver.housing.auction.steplimit", defaultValue = "100")
	public static float AUCTION_BID_STEP_LIMIT;

	@Property(key = "gameserver.housing.auction.auto_fill.time", defaultValue = "0 0 0 ? * MON")
	public static CronExpression AUCTION_AUTO_FILL_TIME;
	@Properties(keyPattern = "^gameserver\\.housing\\.auction\\.auto_fill\\.limit\\.(.+)")
	public static Map<HouseType, Integer> AUCTION_AUTO_FILL_LIMITS;
}
