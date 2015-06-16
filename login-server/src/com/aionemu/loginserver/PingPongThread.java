package com.aionemu.loginserver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_PING;

/**
 * @author KID
 */
public class PingPongThread implements Runnable {

	private final Logger log = LoggerFactory.getLogger(PingPongThread.class);
	private GsConnection connection;
	public volatile boolean uptime = true;
	private SM_PING ping;
	private byte requests = 0;
	private int serverPID = -1;
	private boolean killProcess = false;
	
	public PingPongThread(GsConnection connection)
	{
		this.uptime = true;	
		this.connection = connection;
		this.ping = new SM_PING();
	}
	
	@Override
	public void run()
	{
		log.info("PingPong for gameserver #"+this.connection.getGameServerInfo().getId()+" has started.");
		while(uptime)
		{
			try {
				Thread.sleep(Config.PINGPONG_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(!uptime || validateResponse())
				return;
			
			try {
				connection.sendPacket(ping);
				requests++;
			}
			catch(Exception ex) {
				log.error("PingThread#"+connection.getGameServerInfo().getId(), ex);
			}
		}
	}
	
	public void onResponse(int pid) {
		requests--;
		this.serverPID = pid;
	}
	
	public boolean validateResponse() {
		if(requests >= 2) {
			uptime = false;
			log.info("Gameserver #"+connection.getGameServerInfo().getId()+" [PID="+this.serverPID+"] died, closing.");
			connection.close(false);
			if(killProcess && serverPID != -1) {
				if(System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
					try {
						Runtime.getRuntime().exec("taskkill /pid " + serverPID + " /f");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		}
		else
			return false;
	}
	
	public void closeMe() {
		uptime = false;
	}
}
