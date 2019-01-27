package bdltz;

public class Salvataggi {
	private static boolean audio = false, click = false;
	
    public static boolean getAudio() {
        return audio;
    }

    public static boolean getClickProcedere() {
        return click;
    }

    public static void setAudio(boolean value) {
        audio = value;
    }

    public static void setClickProcedere(boolean value) {
        click = value;
    }
}
