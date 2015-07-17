package com.aionemu.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author PenguinJoe
 * ServerCommandProcessor implements a background thread to process commands from the console - either OS shell or a java-based launcher  
 */
public class ServerCommandProcessor extends Thread
{
	private static final Logger log = LoggerFactory.getLogger(ServerCommandProcessor.class);
	@Override
	public void run()
	{
		// commands are only accepted from stdin when <enter> is hit. Otherwise we will waste no time reading char by char.
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String command = null;
		log.info("Server command processor thread started");
		try
		{
			while ((command = br.readLine()) != null)
			{
				/*
				 * read command and process. For simplicity this is a set of 'if' statements. 
				 * A better command processor may be used in future but at this time not needed.
				 */
				if ( command.equalsIgnoreCase("shutdown") ||
					 command.equalsIgnoreCase("quit")     ||
					 command.equalsIgnoreCase("exit"))
					System.exit(0); 		// this will run finalizers and shutdown hooks for a clean shutdown.
			}
		} 
		catch (IOException e)
		{
			// an IOException here indicates the console (or other launcher) is closing. The server needs to shut down too.
			System.exit(0); // exit using shutdown handlers.
		}
	}
}
