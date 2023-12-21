package me.mcofficer.james.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.phrases.PhraseDatabase;
import me.mcofficer.james.phrases.NewsDatabase;
import me.mcofficer.james.phrases.Phrase;
import me.mcofficer.james.phrases.PhraseLimits;
import me.mcofficer.esparser.DataFile;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.esparser.DataNodeStringLogger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

abstract class Parse extends Command {

    protected static final long FILE_TIMEOUT = 30;
    protected static final int MAX_INDIVIDUAL_FILE_SIZE = 300*1024;
    protected static final int MAX_TOTAL_FILE_SIZE = 300*1024;
    protected static final Pattern REPITITION = Pattern.compile("\\A(\\d+)\\s+");
    protected static final int MAX_STRING_LENGTH = 1000;
    protected static final int MAX_ENTRIES = 10;
    protected static final int MAX_REPITITIONS = 20;
    protected static final int MAX_OUTPUT_LINES = 20;
    protected static final int MAX_PHRASE_BUFFER_SIZE = 10000;
    protected static final int MAX_PHRASE_RECURSION_DEPTH = 7;

    protected PhraseDatabase gameDataPhrases;
    protected NewsDatabase gameDataNews;
    private boolean splitLines;

    public Parse(PhraseDatabase gameDataPhrases, NewsDatabase gameDataNews, boolean splitLines) {
        this.gameDataPhrases = gameDataPhrases;
        this.gameDataNews = gameDataNews;
        this.splitLines = splitLines;
    }

    abstract protected boolean processInput(int count, PhraseDatabase phrases, NewsDatabase news, String entry, EmbedBuilder embed, PhraseLimits limits);

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs().trim(); // .replaceAll("[@#<>|*_ \t]+"," ");

        EmbedBuilder embed = new EmbedBuilder();
        PhraseLimits limits = new PhraseLimits(MAX_PHRASE_BUFFER_SIZE, MAX_PHRASE_RECURSION_DEPTH);
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        int status = 0;

        PhraseDatabase phrases;
        NewsDatabase news;
        if(attachments.size() > 0) {
            news = new NewsDatabase(gameDataNews);
            phrases = new PhraseDatabase(gameDataPhrases);
            status = readAttachments(attachments, phrases, news, embed, limits);
        } else {
            phrases = gameDataPhrases;
            news = gameDataNews;
        }

        if(status > 0)
            event.reply(embed.build());
        if(status != 0)
            return;

        int linesRemaining = MAX_OUTPUT_LINES;
        String[] lines;
        if(splitLines)
            lines = args.split("\\R+");
        else {
            lines = new String[1];
            lines[0] = args.replace("\\R+", "\n").replace("[ \t]+", " ");
        }
        if(lines.length > MAX_ENTRIES) {
            embed.addField("Too Many Queries!", "Provide no more than "+MAX_ENTRIES+". You provided "+lines.length+'.', false);
            event.reply(embed.build());
            return;
        }
        for(String line : lines) {
            String entry = line.strip().replace("\\s+"," ");
            Matcher matcher = REPITITION.matcher(entry);
            int count = 1;
            if(matcher.find() && matcher.end() < entry.length()) {
                count = Integer.parseInt(matcher.group(1), 10);
                if(count < 1)
                    count = 1;
                if(count > MAX_REPITITIONS)
                    count = MAX_REPITITIONS;
                if(count > linesRemaining)
                    count = linesRemaining;
                entry = entry.substring(matcher.end());
            }
            if(!processInput(count, phrases, news, entry, embed, limits))
                break;
            linesRemaining -= count;
        }
        event.reply(embed.build());
    }

    private int readAttachments(List<Message.Attachment> attachments, PhraseDatabase phrases, NewsDatabase news, EmbedBuilder embed, PhraseLimits limits) {
        String expanded = null;
        int status = 0;
        if(attachments.size() > 0) {
            String attachmentErrors = validateAttachmentList(attachments);
            if(attachmentErrors.length() > 0) {
                embed.addField(null, attachmentErrors, false).setTitle("Invalid Attachments");
                return 1;
            }
            status = readFromAttachments(attachments, embed, phrases, news);
            if(status < 0) {
                // Thread was interrupted and should exit as soon as possible.
                System.out.println("thread interrupted");
                return -1;
            }
        }
        return status;
    }

    private int readFromAttachments(List<Message.Attachment> attachments, EmbedBuilder embed, PhraseDatabase phrases, NewsDatabase news) {
        DataNodeStringLogger logger = new DataNodeStringLogger();

        for(Message.Attachment a : attachments) {
            try {
                DataFile file = readAttachment(a, logger);
                if(file == null) {
                    embed.addField(a.getFileName(), ": could not read", false);
                    continue;
                }
                phrases.addPhrases(file.getNodes());
                news.addNews(file.getNodes());
                logger.stopLogging();
                logger.freeResources();
            } catch(IOException exc) {
                embed.addField(a.getFileName(), ": could not read: " + exc, false);
                return 1;
            } catch(ExecutionException ee) {
                embed.addField(a.getFileName(), ": could not read: " + ee, false);
                return 1;
            } catch(InterruptedException iexc) {
                // embed.addField(a.getFileName(), ": operation was interrupted", false);
                return -1;
            } catch(TimeoutException texc) {
                embed.addField(a.getFileName(), ": timed out waiting for Discord to provide the file", false);
                return 1;
            }
        }

        String parserErrors = logger.toString();
        if(parserErrors.length() > 1000)
            parserErrors = parserErrors.substring(0,1000);
        if(parserErrors.length() > 0)
            embed.addField("Parser Errors", parserErrors, false);
        return 0;
    }

    private String validateAttachmentList(List<Message.Attachment> attachments) {
        int acceptable = 0;
        StringBuilder errors = new StringBuilder();
        errors.append(String.format("Please attach one or more text files. They must be less than %.1f kiB total.\n", MAX_TOTAL_FILE_SIZE/1024.0));
        int start = errors.length();
        long size = 0;
        for(Message.Attachment a : attachments)
            if(validateAttachment(a, errors)) {
                acceptable++;
                size += a.getSize();
            }
        
        if(size > MAX_TOTAL_FILE_SIZE)
            errors.append("Total file size is too large: ")
                  .append(String.format("%.1f", size/1024.0)).append(" > ")
                  .append(String.format("%.1f", MAX_TOTAL_FILE_SIZE/1024.0)).append("kiB");
        
        if(errors.length() > start || acceptable < attachments.size())
            return errors.toString();

        return "";
    }

    private boolean validateAttachment(Message.Attachment a, StringBuilder errors) {
        if(a.isImage())
            errors.append(a.getFileName()).append(": is an image, not a text file\n");
        else if(a.isVideo())
            errors.append(a.getFileName()).append(": is a video, not a text file\n");
        else if(!a.getContentType().startsWith("text/plain"))
            errors.append(a.getFileName()).append(": is not a text file (content type \"")
                .append(a.getContentType()).append("\" expected \"text/plain charset=utf-8\"\n");
        else if(a.getContentType().indexOf("charset=utf-8") < 0)
            errors.append(a.getFileName()).append(": is not a text file (content type \"")
                .append(a.getContentType()).append("\" expected \"text/plain charset=utf-8\"\n");
        else if(a.getSize() > MAX_INDIVIDUAL_FILE_SIZE)
            errors.append(a.getFileName()).append(": is too large: ")
                .append(String.format("%.1f", a.getSize()/1024.0)).append(" > ")
                .append(String.format("%.1f", MAX_INDIVIDUAL_FILE_SIZE/1024.0)).append(" kiB\n");
        else if(a.getSize() == 0)
            errors.append(a.getFileName()).append(": is empty\n");
        else
            return true;
        return false;
    }

    private static byte[] readWholeStream(int size, InputStream stream, long timeout) throws IOException {
        long startTime = System.nanoTime();
        byte[] b = new byte[size];
        int count = 0;
        int offset = 0;
        while(count >= 0 && offset < size && System.nanoTime() - startTime < timeout) {
            count = stream.read(b, offset, size - offset);
            if(count > 0)
                offset += count;
        }
        if(offset <= 0)
            return null;
        if(offset < b.length)
            b = Arrays.copyOf(b, offset);
        return b;
    }

    private DataFile readAttachment(Message.Attachment a, DataNodeStringLogger logger) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        InputStream stream = null;
        byte[] b = null;
        int fileSize = a.getSize();
        try {
            stream = a.retrieveInputStream().get(FILE_TIMEOUT, TimeUnit.SECONDS);
            b = readWholeStream(fileSize, stream, FILE_TIMEOUT * 1000000000l);
        } finally {
            if(stream != null)
                stream.close();
        }
        if(b == null || b.length < fileSize)
            return null;

        String result = new String(b, StandardCharsets.UTF_8);
        String[] lines = result.split("(?<=\\R)");

        return new DataFile(Arrays.asList(lines), logger);
    }
}
