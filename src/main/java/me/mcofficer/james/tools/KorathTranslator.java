package me.mcofficer.james.tools;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import static java.util.Map.entry;
import java.util.StringTokenizer;
import javax.annotation.Nullable;
import java.io.IOException;

import me.mcofficer.james.tools.Translator;
import net.dv8tion.jda.api.EmbedBuilder;

public class KorathTranslator extends Translator {

    private static Map<Character, Character> toExile = createExileMap();
    private static Map<Character, Character> toEfreti = createEfretiMap();
    private Locale indonesia = new Locale("id");

    public KorathTranslator(OkHttpClient okHttpClient) {
	super(okHttpClient);
    }

    /**
     * @param query The text to "translate" into Korath
     * @return The translated text. Will need massaging to be usable in game.
     */
    public void korath(String query, EmbedBuilder embed) throws IOException{
	StringBuilder builder = new StringBuilder();

	embed.setFooter("This tool only aids translation. You must massage the words for readability. Sometimes the cipher will produce obscene or offensive terms. Words with standard translations, like human/Humani, won't be correct. ");
	embed.addField("English", query, false);

	String indonesian = translate("en", "id", query).toLowerCase(indonesia);
	embed.addField("Indonesian", indonesian, false);

	StringTokenizer tokenizer = new StringTokenizer(indonesian);
	int words = tokenizer.countTokens();
	char[][] reverse = new char[words][];
	for(int i = 0; tokenizer.hasMoreTokens(); i++) {
	    char[] chars = tokenizer.nextToken().toCharArray();
	    if(i != 0) {
		builder.append(' ');
	    }
	    builder.append(chars);
	    char swapper;
	    int left = 0, right = chars.length - 1;
	    for(; left < right && !toExile.containsKey(chars[right]); right--) {}
	    for(; left < right && !toExile.containsKey(chars[left]); left++) {}
	    for(; left < right; left++, right--) {
		swapper = chars[left];
		chars[left] = chars[right];
		chars[right] = swapper;
	    }
	    reverse[i] = chars;
	}
	embed.addField("Reversed", join(reverse), false);

	char[][] korath = new char[words][];
	for(int i = 0; i < words; i++) {
	    char[] from = reverse[i];
	    char[] exile = new char[from.length];
	    for(int j = 0; j < exile.length; j++) {
		exile[j] = toExile.getOrDefault(from[j], from[j]);
	    }
	    korath[i] = exile;
	}
	embed.addField("Exile", join(korath), false);

	for(int i = 0; i < words; i++) {
	    char[] from = reverse[i];
	    char[] efreti = korath[i];
	    for(int j = 0; j < efreti.length; j++) {
		efreti[j] = toEfreti.getOrDefault(from[j], from[j]);
	    }
	}
	embed.addField("Efreti", join(korath), false);
    }

    private String join(char[][] words) {
	StringBuilder builder = new StringBuilder();
	for(int i = 0; i < words.length; i++) {
	    if(i > 0) {
		builder.append(' ');
	    }
	    builder.append(words[i]);
	}
	return builder.toString();
    }

    private static Map<Character, Character> createExileMap() {
	return new HashMap<Character, Character>() {{
	    put('A', 'A'); put('E', 'E'); put('I', 'I'); put('O', 'U');
	    put('U', 'O'); put('B', 'H'); put('C', 'D'); put('D', 'S');
	    put('F', 'J'); put('G', 'N'); put('H', 'P'); put('J', 'V');
	    put('K', 'T'); put('L', 'M'); put('M', 'F'); put('N', 'R');
	    put('P', 'B'); put('Q', 'Z'); put('R', 'L'); put('S', '\'');
	    put('T', 'K'); put('V', 'Q'); put('W', 'C'); put('X', 'Y');
	    put('Y', 'G'); put('Z', 'W');

	    put('a', 'a'); put('e', 'e'); put('i', 'i'); put('o', 'u');
	    put('u', 'o'); put('b', 'h'); put('c', 'd'); put('d', 's');
	    put('f', 'j'); put('g', 'n'); put('h', 'p'); put('j', 'v');
	    put('k', 't'); put('l', 'm'); put('m', 'f'); put('n', 'r');
	    put('p', 'b'); put('q', 'z'); put('r', 'l'); put('s', '\'');
	    put('t', 'k'); put('v', 'q'); put('w', 'c'); put('x', 'y');
	    put('y', 'g'); put('z', 'w'); }};
    }

    private static Map<Character, Character> createEfretiMap() {
	return new HashMap<Character, Character>() {{
	    put('A', 'A'); put('E', 'E'); put('I', 'I'); put('O', 'U');
	    put('U', 'O'); put('B', 'B'); put('C', 'V'); put('D', 'T');
	    put('F', 'Y'); put('G', 'L'); put('H', 'H'); put('J', 'W');
	    put('K', 'S'); put('L', 'N'); put('M', 'F'); put('N', 'R');
	    put('P', 'C'); put('Q', 'T'); put('R', 'P'); put('S', 'M');
	    put('T', 'K'); put('V', 'R'); put('W', 'G'); put('X', 'S');
	    put('Y', 'D'); put('Z', 'K');
	    
	    put('a', 'a'); put('e', 'e'); put('i', 'i'); put('o', 'u');
	    put('u', 'o'); put('b', 'b'); put('c', 'v'); put('d', 't');
	    put('f', 'y'); put('g', 'l'); put('h', 'h'); put('j', 'w');
	    put('k', 's'); put('l', 'n'); put('m', 'f'); put('n', 'r');
	    put('p', 'c'); put('q', 't'); put('r', 'p'); put('s', 'm');
	    put('t', 'k'); put('v', 'r'); put('w', 'g'); put('x', 's');
	    put('y', 'd'); put('z', 'k'); }};
    }
}
