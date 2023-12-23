package me.mcofficer.james.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.tools.KorathTranslator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class Korath extends Command {

    protected final KorathTranslator translator;

    public Korath(KorathTranslator translator) {
        this.translator = translator;
        name = "korath";
        help = "Applies the Korath encoding on <english>. Translate <english> from English to Indonesian, reverses letters in words, and passes it through a cipher.";
        arguments = "<english>\n    <more english>...";
        category = James.misc;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            String query = event.getArgs();
            if(query.length() > 1000)
                embedBuilder.setDescription("Text is too long. Maximum length is 1000 characters.");
            else
                runTranslator(query, embedBuilder, event.getAuthor());
            event.reply(embedBuilder.build());
        }
        catch (IOException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            event.reply("An error occured:\n```" + stringWriter.toString() + "```");
        }
    }

    protected void runTranslator(String query, EmbedBuilder builder, User author) throws IOException {
        builder.setTitle("Korath Encoding");
        translator.korath(query, builder, author);
    }
}
