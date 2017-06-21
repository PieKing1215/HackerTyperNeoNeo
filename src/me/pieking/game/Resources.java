package me.pieking.game;

import java.net.URL;

public class Resources {

	public static URL getSound(String name) {
		URL u = Resources.class.getClassLoader().getResource("sounds/" + name);
		//System.out.println(name + " " + u);
		return u;
	}

}
