package me.mcofficer.james;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.mcofficer.esparser.DataFile;
import me.mcofficer.james.phrases.PhraseDatabase;
import me.mcofficer.james.phrases.NewsDatabase;
import me.mcofficer.james.commands.*;
import me.mcofficer.james.commands.creatortools.*;
import me.mcofficer.james.commands.info.*;
import me.mcofficer.james.commands.lookup.*;
import me.mcofficer.james.commands.misc.*;
import me.mcofficer.james.tools.Lookups;
import me.mcofficer.james.tools.TextGenerator;
import me.mcofficer.james.tools.KorathTranslator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class James {

    public final static String GITHUB_URL = "https://github.com/HumanikaRafeki/EndlessSky-Discord-Bot/";
    public final static String GITHUB_RAW_URL = "https://raw.githubusercontent.com/EndlessSkyCommunity/EndlessSky-Discord-Bot/master/";
    public final static String ES_GITHUB_URL = "https://github.com/endless-sky/endless-sky/";

    public final static EventWaiter eventWaiter = new EventWaiter();
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private static Properties cfg = new Properties();

    public static Command.Category audio = new Command.Category("Audio");
    public static Command.Category misc = new Command.Category("Language");
    public static Command.Category info = new Command.Category("Info");
    public static Command.Category creatorTools = new Command.Category("Creator Tools");
    public static Command.Category lookup = new Command.Category("Lookup");
    public static TextGenerator whining;
    public static TextGenerator activity;
    public static TextGenerator mood;
    private Logger log = LoggerFactory.getLogger(James.class);

    public static void main(String[] args) {
        try {
            cfg.load(new FileReader("james.properties"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            new James(cfg);
        }
        catch (LoginException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private James(Properties cfg) throws LoginException, InterruptedException, IOException {
        String prefix = cfg.getProperty("prefix", "-");
        String owner = cfg.getProperty("owner");
        CommandClientBuilder clientBuilder = new CommandClientBuilder()
                .setPrefix(prefix)
                .setActivity(net.dv8tion.jda.api.entities.Activity.listening(prefix + "help"))
                .setOwnerId(owner);
        addCommands(clientBuilder, cfg.getProperty("github"));

        clientBuilder.setHelpConsumer(new Help(clientBuilder.build())); // this HAS to be done after adding all Commands!

        JDA jda = JDABuilder.createDefault(cfg.getProperty("token"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(clientBuilder.build(), eventWaiter)
                .build()
                .awaitReady();
    }

    private void addCommands(CommandClientBuilder builder, String githubToken) throws IOException {
        log.info("Downloading game data...");
        ArrayList<File> paths = Util.fetchGameData(githubToken);
        ArrayList<DataFile> dataFiles = new ArrayList<>();
        for (File path : paths)
            dataFiles.add(new DataFile(path.getAbsolutePath()));

        log.info("Fetching image paths...");
        ArrayList<String> imagePaths = Util.get1xImagePaths(githubToken);
        Lookups lookups = new Lookups(okHttpClient, dataFiles, imagePaths);
        log.info("Lookups instantiated");

        log.info("Parsing phrases...");
        PhraseDatabase phrases = new PhraseDatabase();
        for(DataFile file : dataFiles)
            phrases.addPhrases(file.getNodes());

        log.info("Parsing news...");
        NewsDatabase news = new NewsDatabase();
        for(DataFile file : dataFiles)
            news.addNews(file.getNodes());

        log.info("Initializing canned responses...");
        whining = new TextGenerator("${JAMES::whining}", phrases);
        mood = new TextGenerator("${JAMES::mood}", phrases);
        activity = new TextGenerator("${JAMES::activity}", phrases);
	DataFile jamesTxt = new DataFile("james.txt");
        whining.load(jamesTxt.getNodes());
        mood.load(jamesTxt.getNodes());
        activity.load(jamesTxt.getNodes());

        log.info("Starting background thread to fetch hdpi image paths...");
        new Thread(() -> {
            lookups.setImagePaths(Util.get2xImagePaths(imagePaths));
            log.info("Hdpi image paths fetched successfully.");
        }).start();

        builder.addCommands(
                new SwizzleImage(), new Template(), new CRConvert(),
                new Korath(new KorathTranslator(okHttpClient)),
                new IndoKorath(new KorathTranslator(okHttpClient)),
                new Phrases(phrases, news), new News(phrases, news), new Say(phrases, news),
                new Info(githubToken), new Ping(),
                new Mood("activity", "Stop being a narc.", info, activity),
                new Issue(), new Commit(), new Showdata(lookups), new Showimage(lookups), new Show(lookups), new Lookup(lookups), new Swizzle(lookups)
        );
    }
}
