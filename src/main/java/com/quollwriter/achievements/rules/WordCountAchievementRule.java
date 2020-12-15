package com.quollwriter.achievements.rules;

import java.util.*;

import org.dom4j.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

public class WordCountAchievementRule extends AbstractAchievementRule
{

    public static final String RULE_TYPE = "wordcount";

    public class XMLConstants
    {

        public static final String wordCount = "wordCount";
        public static final String count = "count";
        public static final String chapter = "chapter";

    }

    private int count = -1;
    private int wordCount = 0;
    private boolean chaptersOnly = false;

    public WordCountAchievementRule (Element root)
                                     throws  GeneralException
    {

        super (root);

        this.count = DOM4JUtils.attributeValueAsInt (root,
                                                     XMLConstants.count,
                                                     false);

        this.wordCount = DOM4JUtils.attributeValueAsInt (root,
                                                         XMLConstants.wordCount,
                                                         true);

        this.chaptersOnly = DOM4JUtils.attributeValueAsBoolean (root,
                                                                XMLConstants.chapter,
                                                                false);

        if ((this.count < 1)
            &&
            (this.chaptersOnly)
           )
        {

            this.count = 1;

        }

    }

    public String toString ()
    {

        return super.toString () + "(count: " + this.count + ", wordCount: " + this.wordCount + ", chapters only: " + this.chaptersOnly + ")";

    }

    public boolean shouldPersistState ()
    {

        return false;

    }

    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
                             throws                Exception
    {

        return this.achieved (viewer);

    }

    public boolean achieved (AbstractProjectViewer viewer)
    {

        if (viewer == null)
        {

            return false;

        }

        if (this.chaptersOnly)
        {

            int c = 0;

            Set<ChapterCounts> counts = viewer.getAllChapterCountsAsSet ();

            for (ChapterCounts cc : counts)
            {

                if (cc.getWordCount () >= this.wordCount)
                {

                    c++;

                }

            }

            if (c >= this.count)
            {

                return true;

            }

        }

        if (viewer.getAllChapterCounts ().getWordCount () >= this.wordCount)
        {

            return true;

        }

        return false;

    }

    @Override
    public void init (Element root)
    {

    }

    @Override
    public void fillState (Element root)
    {

    }

}
