package me.mcofficer.james.phrases;

import java.util.Set;

public interface PhraseExpander {
    void addPhrasesTo(PhraseProvider phrases);
    public void expand(StringBuilder result, PhraseProvider phrases, Set<String> touched);
};
