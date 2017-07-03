package me.pieking.game.command;

import java.util.List;

import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.net.Client;
import me.pieking.game.net.Client.SocketStatus;
import me.pieking.game.sound.Sound;

public class CommandIpInfo extends Command{

	public CommandIpInfo() {
		super("ipconfig");
		
		desc  = "Print information about your connection.";
		usage = "ipconfig";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		console.write("");
		console.write("\\Y\\uIP Configuration");
		console.write("");
		console.write("Connection state . : " + Client.getStatus());
		console.write("IPv4 Address . . . : " + (Client.getStatus() == SocketStatus.CONNECTED ? Client.myIp() : "N/A"));
		console.write("");
		return true;
	}

}
