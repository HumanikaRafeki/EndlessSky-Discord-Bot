package me.mcofficer.james.tools;

import me.mcofficer.james.phrases.PhraseDatabase;
import me.mcofficer.james.phrases.PhraseLimits;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.esparser.DataFile;

import java.util.ArrayList;

public class TextGenerator {
    private PhraseDatabase phrases;
    private final String text;
    private final PhraseLimits limits;

    private final static int RESPONSE_LIMIT = 1000;
    private final static int RECURSION_LIMIT = 10;

    public TextGenerator(String text, PhraseDatabase parent) {
        this.phrases = new PhraseDatabase(parent);
        this.text = text;
        this.limits = new PhraseLimits(RESPONSE_LIMIT, RECURSION_LIMIT);
    }

    public void load(ArrayList<DataNode> data) {
	phrases.addPhrases(data);
    }

    public String generate() {
        if(this.phrases == null || this.text == null)
            return "";
        StringBuilder result = new StringBuilder();
        phrases.expandText(text, limits, result);
        return result.toString();
    }
};
