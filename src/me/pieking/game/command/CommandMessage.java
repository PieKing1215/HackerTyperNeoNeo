package me.pieking.game.command;

import java.util.List;

import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.net.Client;
import me.pieking.game.sound.Sound;

public class CommandMessage extends Command{

	public CommandMessage() {
		super("msg");
		
		desc  = "Send a message over the network";
		usage = "msg <ip> <message>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		if(args.size() >= 2){
			String ip = args.get(0);
			String msg = "";
			
			System.out.println(args.size());
			
			for(int i = 1; i < args.size(); i++){
				msg += args.get(i).replace("|", "\\|") + " ";
			}
			
			System.out.println(msg);
			
			Client.write("msg|" + ip + "|" + msg);
			
		}else{
			console.write("\\RNot enough arguments.");
			return false;
		}
		return true;
	}

}
