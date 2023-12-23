package me.mcofficer.james.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import net.dv8tion.jda.api.EmbedBuilder;

public class Ping extends Command {

    public Ping() {
        name = "ping";
        help = "Displays the time of the bot's last heartbeat.";
        category = James.info;
    }

    @Override
    protected void execute(CommandEvent event) {
        float ping = event.getJDA().getGatewayPing();
        long BPM = Math.round(60.0 / Math.max(ping / 1000, 1e-9));
        event.reply(new EmbedBuilder()
                .setTitle("EndlessSky-Discord-Bot", James.GITHUB_URL)
                .setDescription("Last heartbeat took " + ping + " ms (" + BPM + " BPM).")
                .setColor(event.getGuild().getSelfMember().getColor())
                .setFooter(James.pingText.generate())
                .build()
        );
    }
}
