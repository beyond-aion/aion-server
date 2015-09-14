package com.aionemu.chatserver.utils.guice;

import com.aionemu.chatserver.network.aion.ClientPacketHandler;
import com.aionemu.chatserver.network.netty.NettyServer;
import com.aionemu.chatserver.network.netty.pipeline.LoginToClientPipeLineFactory;
import com.aionemu.chatserver.service.BroadcastService;
import com.aionemu.chatserver.service.ChatService;
import com.aionemu.chatserver.service.GameServerService;
import com.aionemu.chatserver.utils.IdFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author ATracer
 */
public class ServiceInjectionModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IdFactory.class).asEagerSingleton();
		bind(ClientPacketHandler.class).in(Scopes.SINGLETON);
		bind(LoginToClientPipeLineFactory.class).in(Scopes.SINGLETON);
		bind(NettyServer.class).asEagerSingleton();
		bind(GameServerService.class).in(Scopes.SINGLETON);
		bind(BroadcastService.class).in(Scopes.SINGLETON);
		bind(ChatService.class).in(Scopes.SINGLETON);
	}
}
