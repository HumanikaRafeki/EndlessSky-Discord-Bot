package me.mcofficer.james.phrases;

interface PhraseProvider {
    public PhraseExpander getExpander(String phrase);
    void setExpander(String phrase, PhraseExpander expander);
};
