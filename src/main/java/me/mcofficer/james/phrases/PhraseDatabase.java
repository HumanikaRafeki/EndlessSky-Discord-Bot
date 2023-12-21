package me.mcofficer.james.phrases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import me.mcofficer.esparser.DataNode;

public class PhraseDatabase implements PhraseProvider {

    PhraseDatabase parent;
    HashMap<String, Phrase> expanders;

    public PhraseDatabase(PhraseDatabase parent) {
        expanders = new HashMap<String, Phrase>();
        this.parent = parent;
    }

    public PhraseDatabase() {
        expanders = new HashMap<String, Phrase>();
        this.parent = null;
    }

    public void addPhrases(ArrayList<DataNode> data) {
        for(DataNode node : data)
            if(node.size() > 1 && node.getTokens().get(0).equals("phrase"))
                addPhrase(node);
    }

    public void addPhrase(DataNode node) {
        Phrase phrase = new Phrase(node);
        String name = phrase.getName();
        if(name != null && name.length() > 0)
            expanders.put(name, phrase);
        else
            node.printTrace("not a valid phrase node");
    }

    public String expand(String phrase, PhraseLimits limits) {
        PhraseExpander expander = getExpander(phrase);
        if(expander == null)
            return "";
        StringBuilder builder = new StringBuilder();
        Set<String> touched = new HashSet<String>();
        expander.expand(builder, this, touched, limits);
        return builder.toString();
    }

    public Phrase get(String phrase) {
        Phrase got = expanders.getOrDefault(phrase, null);
        if(got != null)
            return got;
        if(parent == null)
            return null;
        return parent.get(phrase);
    }

    @Override
    public PhraseExpander getExpander(String phrase) {
        return get(phrase);
    }
}
