package me.mcofficer.james.commands.misc;

import me.mcofficer.james.James;
import me.mcofficer.james.phrases.PhraseDatabase;
import me.mcofficer.james.phrases.NewsDatabase;
import me.mcofficer.james.phrases.Phrase;
import me.mcofficer.james.phrases.PhraseLimits;

import net.dv8tion.jda.api.EmbedBuilder;

public class Phrases extends Parse {

    public Phrases(PhraseDatabase gameDataPhrases, NewsDatabase gameDataNews) {
        super(gameDataPhrases, gameDataNews, true);
        name = "phrases";
        help = "Generates phrases from game data files and attached files.";
        arguments = "[<count>] name\n    [<count>] name ...\n    <attached txt files>]";
        category = James.misc;
    }

    @Override
    protected boolean processInput(int count, PhraseDatabase phrases, NewsDatabase news, String entry, EmbedBuilder embed, PhraseLimits limits) {
        String expanded = expandPhrases(count, phrases, entry, limits);
        if(count < 1) {
            embed.addField("Phrase \"" + entry + '"', "*Too many inputs! Use fewer phrases or repititions.*", false);
            return false;
        }
        if(expanded == null)
            embed.addField("Phrase \"" + entry + '"', "*Phrase not found!*", false);
        else {
            String title = "Phrase \"" + entry + '"';
            if(count > 1)
                title += " Repeated " + count + " Times";
            embed.addField(title, "`" + expanded.replace("`","'") + "`", false);
        }
        return true;
    }

    private String expandPhrases(int count, PhraseDatabase phrases, String phrase, PhraseLimits limits) {
        StringBuilder builder = new StringBuilder();

        Phrase gotten = phrases.get(phrase);
        if(gotten == null)
            return null;

        for(int repeat = 0; repeat < count && builder.length() < MAX_STRING_LENGTH; repeat++)
            builder.append(gotten.expand(phrases, limits)).append('\n');

        // "very long string" becomes "very long s..."
        if(builder.length() > MAX_STRING_LENGTH) {
            builder.delete(MAX_STRING_LENGTH - 3, builder.length());
            builder.append("...");
        }
        return builder.toString().replaceAll("[@#<>|*_ \t]+"," ");
    }
};
