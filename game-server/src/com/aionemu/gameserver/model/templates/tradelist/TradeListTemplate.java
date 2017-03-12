package com.aionemu.gameserver.model.templates.tradelist;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author orz
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "tradelist_template")
public class TradeListTemplate {

	/**
	 * Npc Id.
	 */
	@XmlAttribute(name = "npc_id", required = true)
	private int npcId;
	@XmlAttribute(name = "npc_type")
	private TradeNpcType tradeNpcType = TradeNpcType.NORMAL;
	@XmlAttribute(name = "sell_price_rate")
	private int sellPriceRate = 100;
	@XmlAttribute(name = "sell_price_rate2")
	private int sellPriceRate2 = 100;
	@XmlAttribute(name = "ap_sell_price_rate2")
	private int apSellPriceRate2 = 100;
	@XmlAttribute(name = "buy_price_rate")
	private int buyPriceRate;
	@XmlAttribute(name = "save_count")
	private Boolean saveCount;
	@XmlElement(name = "tradelist")
	protected List<TradeTab> tradeTablist;

	/**
	 * @return List<TradeTab>
	 */
	public List<TradeTab> getTradeTablist() {
		if (tradeTablist == null)
			tradeTablist = new ArrayList<>();
		return this.tradeTablist;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getCount() {
		return tradeTablist.size();
	}

	/**
	 * @return the Npc Type
	 */
	public TradeNpcType getTradeNpcType() {
		return tradeNpcType;
	}

	/**
	 * @return the sellPriceRate
	 */
	public int getSellPriceRate() {
		return sellPriceRate;
	}

	/**
	 * @return the sellPriceRate2 //new 4.7
	 */
	public int getSellPriceRate2() {
		return sellPriceRate2;
	}

	/**
	 * @return the apSellPriceRate2 //new 4.7
	 */
	public int getApSellPriceRate2() {
		return apSellPriceRate2;
	}

	/**
	 * @return the buyPriceRate
	 */
	public int getBuyPriceRate() {
		return buyPriceRate;
	}

	public Boolean isSaveCount() {
		return saveCount;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * <p>
	 * The following schema fragment specifies the expected content contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Tradelist")
	public static class TradeTab {

		@XmlAttribute
		protected int id;

		/**
		 * Gets the value of the id property.
		 */
		public int getId() {
			return id;
		}
	}
}
