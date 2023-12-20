package me.mcofficer.james.phrases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import me.mcofficer.esparser.DataNode;

public class PhraseDatabase implements PhraseProvider {

    HashMap<String, PhraseExpander> expanders;

    public PhraseDatabase() {
        expanders = new HashMap<String, PhraseExpander>();
    }

    public Map<String, PhraseExpander> getExpanders() {
        return expanders;
    }

    public void addPhrases(ArrayList<DataNode> data) {
        for(DataNode node : data)
            addPhrase(node);
    }

    public void addPhrase(DataNode node) {
        if(node.size() > 1 && node.getTokens().get(0).equals("phrase"))
            new Phrase(node).addPhrasesTo(this);
        else
            node.printTrace("not a valid phrase node");
    }

    public String expand(String phrase) {
        PhraseExpander expander = getExpander(phrase);
        if(expander == null)
            return "";
        StringBuilder builder = new StringBuilder();
        Set<String> touched = new HashSet<String>();
        expander.expand(builder, this, touched);
        return builder.toString();
    }

    @Override
    public PhraseExpander getExpander(String phrase) {
        return expanders.getOrDefault(phrase, null);
    }

    @Override
    public void setExpander(String phrase, PhraseExpander expander) {
        expanders.put(phrase, expander);
    }
}
