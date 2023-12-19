package me.mcofficer.james.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Help implements Consumer<CommandEvent> {

    private final List<Command> commands;
    private final String prefix;
    private EmbedBuilder helpEmbedBuilder;

    public Help(CommandClient client) {
        commands = client.getCommands();
        prefix = client.getPrefix();
        helpEmbedBuilder = createHelpEmbedBuilder();
    }

    @Override
    public void accept(CommandEvent e) {
        if (e.getArgs().isEmpty())
            e.reply(helpEmbedBuilder
                    .setColor(e.getGuild().getSelfMember().getColor())
                    .build());
        else {
            String args = e.getArgs().startsWith(prefix) ? e.getArgs().substring(1) : e.getArgs();
            // TODO: Check for aliases
            for (Command c : commands)
                if (c.getName().equalsIgnoreCase(args)) {
                    e.reply(createHelpEmbedBuilder(c)
                            .setColor(e.getGuild().getSelfMember().getColor())
                            .build());
                    break;
                }
        }
    }

    /**
     * @return an EmbedBuilder containing a help text for all Commands defined in {@link #commands}.
     */
    private EmbedBuilder createHelpEmbedBuilder() {
        List<Command.Category> categories = new ArrayList<>();
        for (Command c : commands)
            if (c.getCategory() != null && !categories.contains(c.getCategory()))
                categories.add(c.getCategory());

        EmbedBuilder embedBuilder = new EmbedBuilder();

        for (Command.Category category : categories) {
            StringBuilder sb = new StringBuilder();
            for (Command c : commands) {
                if (c.isHidden() || !c.getCategory().equals(category))
                    continue;

                sb.append(String.format("`%s%s %s`", prefix, c.getName(), c.getArguments() == null ? "" : c.getArguments()));
                if (c.getAliases().length > 0) {
                    sb.append(" (");
                    for (String alias : c.getAliases())
                        sb.append(String.format("`%s%s`, ", prefix, alias));
                    sb.delete(sb.length() - 2, sb.length());
                    sb.append(")");
                }
                sb.append("\n");
            }
            embedBuilder.addField(category.getName(), sb.toString(), true);
        }

        embedBuilder.setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL);

        return embedBuilder;
    }

    /**
     * @param c A Command.
     * @return An EmbedBuilder containing the help text for c.
     */
    private EmbedBuilder createHelpEmbedBuilder(Command c)  {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                .setDescription(String.format("`%s %s`\n", c.getName(), c.getArguments() == null ? "" : c.getArguments()))
                .appendDescription(c.getHelp() + "\n");

        if (c.getAliases().length > 0) {
            embedBuilder.appendDescription("**Aliases: **");
            StringBuilder sb = new StringBuilder();
            for (String alias : c.getAliases())
                sb.append(String.format("`%s`, ", alias));
            sb.delete(sb.length() - 2, sb.length());
            embedBuilder.appendDescription(sb);
        }

        return embedBuilder;
    }
}
