package me.mcofficer.james.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.commands.misc.Korath;
import me.mcofficer.james.tools.KorathTranslator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class IndoKorath extends Korath {

    public IndoKorath(KorathTranslator translator) {
        super(translator);
        name = "indokorath";
        help = "Applies the Korath cipher to <text>. The <text> should be in the Indonesian language.";
        arguments = "<text>\n    <more text>...";
    }

    @Override
    protected void runTranslator(String query, EmbedBuilder builder, User author) throws IOException {
        builder.setTitle("Korath Cipher");
        translator.indokorath(query, builder, author);
    }
}
