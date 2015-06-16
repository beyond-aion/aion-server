package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * @author MrPoke
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TradeinItem")
public class TradeinItem {

    @XmlAttribute
    protected int id;
    @XmlAttribute
    protected int price;

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }
}
