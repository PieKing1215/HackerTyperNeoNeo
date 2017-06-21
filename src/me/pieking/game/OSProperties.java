package me.pieking.game;

/**
 * Based on <a href=
 * "http://stackoverflow.com/a/24861219">http://stackoverflow.com/a/24861219</a>
 * or <a href=
 * "https://gist.github.com/kiuz/816e24aa787c2d102dd0">https://gist.github.com/kiuz/816e24aa787c2d102dd0</a>
 *
 */
public class OSProperties {

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static void main(String[] args) {

		System.out.println(OS);

		if (isWindows()) {
			System.out.println("This is Windows");
		} else if (isMac()) {
			System.out.println("This is Mac");
		} else if (isUnix()) {
			System.out.println("This is Unix or Linux");
		} else if (isSolaris()) {
			System.out.println("This is Solaris");
		} else {
			System.out.println("Your OS could not be determined!");
		}
	}

	public static boolean isWindows() {
		return (OS.contains("win"));
	}

	public static boolean isMac() {
		return (OS.contains("mac"));
	}

	public static boolean isUnix() {
		return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
	}

	public static boolean isSolaris() {
		return (OS.contains("sunos"));
	}

	public static String getOS() {
		if (isWindows()) return "win";
		if (isMac()) return "osx";
		if (isUnix()) return "uni";
		if (isSolaris()) return "sol";
		return "err";
	}

	public static boolean is64bit() {
		return System.getProperty("sun.arch.data.model").equals("64");
	}

}