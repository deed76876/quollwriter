package com.quollwriter.ui.fx;

import java.util.*;

import com.quollwriter.text.*;

public interface SpellChecker
{

    public boolean isCorrect (Word word);

    public boolean isIgnored (Word word);

    public List<String> getSuggestions (Word word);

}
