package me.mcofficer.james.commands.misc;

import me.mcofficer.james.James;
import me.mcofficer.james.phrases.PhraseDatabase;
import me.mcofficer.james.phrases.NewsDatabase;
import me.mcofficer.james.phrases.NewsStory;
import me.mcofficer.james.phrases.PhraseLimits;

import net.dv8tion.jda.api.EmbedBuilder;

public class Say extends Parse {
    public Say(PhraseDatabase gameDataPhrases, NewsDatabase gameDataNews) {
        super(gameDataPhrases, gameDataNews, false);
        name = "say";
        help = "Expands ${phrase} strings in text. Uses phrases from game data files and attached files.";
        arguments = "[<count>] <multi-line text> [<attached txt files>]";
        category = James.misc;
    }

    @Override
    protected boolean processInput(int count, PhraseDatabase phrases, NewsDatabase news, String text, EmbedBuilder embed, PhraseLimits limits) {
        StringBuilder builder = new StringBuilder();
        for(int repeat = 0; repeat < count && builder.length() < MAX_STRING_LENGTH; repeat++) {
            phrases.expandText(text, limits, builder);
            builder.append('\n');
        }

        // "very long string" becomes "very long s..."
        if(builder.length() > MAX_STRING_LENGTH) {
            builder.delete(MAX_STRING_LENGTH - 3, builder.length());
            builder.append("...");
        }

        String title = "Result";
        if(count > 1 && builder.length() > 0)
            title += " with " + count + " repititions";

        String result;
        if(builder.length() > 0)
            result = '`' + builder.toString().replace("`", "'") + '`';
        else
            result = "*Empty string*";
        embed.addField(title, result, false);
        return true;
    }
}
