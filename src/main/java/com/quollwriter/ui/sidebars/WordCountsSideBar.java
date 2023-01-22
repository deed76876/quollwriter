package com.quollwriter.ui.sidebars;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Set;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.charts.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.Header;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class WordCountsSideBar extends AbstractSideBar<AbstractProjectViewer>
{

    public static final String ID = "wordcounts";

    private final String COL_SPEC = "right:max(80px;p), 6px, p:grow";

    private JLabel projectSessionWordCount = null;
    private JLabel totalSessionWordCount = null;
    private JLabel chapterWordCount = null;
    private JLabel chapterPages = null;
    private JLabel allChaptersWordCount = null;
    private JLabel allChaptersPages = null;
    private Timer timer = null;
    private AccordionItem chapterItem = null;
    private AccordionItem selectedItems = null;
    private JComponent chapterSparkLine = null;
    private JLabel chapterSparkLineLabel = null;
    private JLabel selectedWordCount = null;
    private JLabel chapterFleschKincaid = null;
    private JLabel chapterGunningFog = null;
    private JLabel chapterFleschReadingEase = null;
    private JLabel allChaptersFleschKincaid = null;
    private JLabel allChaptersGunningFog = null;
    private JLabel allChaptersFleschReadingEase = null;
    private JLabel selectedFleschKincaid = null;
    private JLabel selectedFleschReadingEase = null;
    private JLabel selectedGunningFog = null;
    private JComponent selectedReadability = null;
    private JComponent allReadability = null;
    private JComponent chapterReadability = null;
    private JComponent selectedReadabilityHeader = null;
    private JComponent allReadabilityHeader = null;
    private JComponent chapterReadabilityHeader = null;
    private JLabel editPointWordCount = null;
    private Box chapterEditPointBox = null;
    private Box allChaptersEditPointBox = null;
    private JLabel allEditPointWordCount = null;
    private JLabel allChaptersEditCount = null;

    public WordCountsSideBar (AbstractProjectViewer v)
    {

        super (v);

    }

    @Override
    public String getId ()
    {

        return ID;

    }

    @Override
    public boolean canClose ()
    {

        return true;

    }

    /**
     * Start the timer and call {@link update()} later.
     */
    @Override
    public void onShow ()
    {

        final WordCountsSideBar _this = this;

        if (this.timer == null)
        {

            this.timer = new Timer (2 * Constants.SEC_IN_MILLIS,
                                    new ActionListener ()
                                    {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            _this.update ();

                                        }

                                    });

        }

        // Start the timer.
        this.timer.start ();

        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.update ();

            }

        });

    }

    @Override
    public void onHide ()
    {

        this.timer.stop ();

    }

    /**
     * Stop the timer, we don't set the timer to null since {@link removeOnClose()} is false.
     */
    @Override
    public void onClose ()
    {

        // Pause the timer.
        this.timer.stop ();

        this.timer = null;

    }

    /**
     * Always return false, we want to keep the counts around since it's a labour intensive thing.
     *
     * @returns Always false.
     */
    public boolean removeOnClose ()
    {

        return false;

    }

    public String getIconType ()
    {

        return Constants.WORDCOUNT_ICON_NAME;

    }

    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.project,
                                        LanguageStrings.sidebar,
                                        LanguageStrings.wordcount,
                                        LanguageStrings.title);
        //return "Word Counts";

    }

    @Override
    public void panelShown (MainPanelEvent ev)
    {
                /*
        if (ev.getPanel () instanceof AbstractEditorPanel)
        {

            this.chapterItem.setVisible (true);
            this.chapterSparkLine.removeAll ();

        } else {

            this.chapterItem.setVisible (false);

        }
*/
        this.update ();

    }

    private void update ()
    {

        if (!this.isVisible ())
        {

            return;

        }

        if (this.timer == null)
        {

            // Probably closing down.
            return;

        }

        ChapterCounts achc = this.viewer.getAllChapterCounts ();

        // When shutting down we may get a null back, just return.
        if (achc == null)
        {

            this.timer.stop ();

            this.timer = null;

            return;

        }

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.wordcount);

        String valueperc = Environment.getUIString (prefix,
                                                    LanguageStrings.valuepercent);

        this.projectSessionWordCount.setText (Environment.formatNumber (this.viewer.getSessionWordCount ()));
        this.totalSessionWordCount.setText (Environment.formatNumber ((Environment.getSessionWordCount ())));//achc.wordCount - this.viewer.getStartWordCounts ().wordCount)));

        final Chapter c = this.viewer.getChapterCurrentlyEdited ();

        if (c != null)
        {

            AbstractEditorPanel qep = this.viewer.getEditorForChapter (c);

            String sel = qep.getEditor ().getSelectedText ();

            this.selectedItems.setVisible (false);
            this.selectedReadability.setVisible (false);
            this.selectedReadabilityHeader.setVisible (false);

            if (!sel.equals (""))
            {

                ChapterCounts sc = new ChapterCounts (sel);

                this.selectedWordCount.setText (Environment.formatNumber (sc.getWordCount ()));

                this.selectedItems.setVisible (true);

                if ((sc.getWordCount () > Constants.MIN_READABILITY_WORD_COUNT)
                    &&
                    (this.viewer.isLanguageEnglish ())
                   )
                {

                    // Show the readability.
                    this.selectedReadability.setVisible (true);
                    this.selectedReadabilityHeader.setVisible (true);

                    ReadabilityIndices ri = this.viewer.getReadabilityIndices (sel);

                    this.selectedFleschKincaid.setText (Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ())));

                    this.selectedGunningFog.setText (Environment.formatNumber (Math.round (ri.getGunningFogIndex ())));

                    this.selectedFleschReadingEase.setText (Environment.formatNumber (Math.round (ri.getFleschReadingEase ())));

                }

            }

            //int a4Count = 0; //this.viewer.getChapterA4PageCount (c);

            ChapterCounts chc = this.viewer.getChapterCounts (c);

            this.chapterEditPointBox.setVisible (false);

            if ((c.getEditPosition () > 0)
                &&
                (chc != null)
               )
            {

                // Get the text.
                String editText = qep.getEditor ().getText ().substring (0, c.getEditPosition ());

                if (editText.trim ().length () > 0)
                {

                    ChapterCounts sc = new ChapterCounts (editText);

                    this.editPointWordCount.setText (String.format (valueperc,
                                                                    Environment.formatNumber (sc.getWordCount ()),
                                                                    Environment.formatNumber (Utils.getPercent (sc.getWordCount (), chc.getWordCount ()))));

                    this.chapterEditPointBox.setVisible (true);

                }

            }

            if (chc != null)
            {

                this.chapterWordCount.setText (String.format (valueperc,
                                                              Environment.formatNumber (chc.getWordCount ()),
                                                              Environment.formatNumber (Utils.getPercent (chc.getWordCount (), achc.getWordCount ()))));

                this.chapterPages.setText (Environment.formatNumber (chc.getStandardPageCount ()));

                this.chapterReadability.setVisible (false);
                this.chapterReadabilityHeader.setVisible (false);

                if ((this.viewer.isLanguageEnglish ())
                    &&
                    (chc.getWordCount () >= Constants.MIN_READABILITY_WORD_COUNT)
                   )
                {

                    this.chapterReadability.setVisible (true);
                    this.chapterReadabilityHeader.setVisible (true);

                    ReadabilityIndices ri = this.viewer.getReadabilityIndices (c);

                    this.chapterFleschKincaid.setText (Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ())));

                    this.chapterGunningFog.setText (Environment.formatNumber (Math.round (ri.getGunningFogIndex ())));

                    this.chapterFleschReadingEase.setText (Environment.formatNumber (Math.round (ri.getFleschReadingEase ())));

                }

                this.chapterItem.setTitle (qep.getTitle ());

                this.chapterItem.setVisible (true);

                if (this.chapterSparkLine.getComponents ().length == 0)
                {

                    try
                    {

                        ChapterDataHandler dh = (ChapterDataHandler) this.viewer.getDataHandler (Chapter.class);

                        // TODO: Find a better way of handling this.
                        if (dh != null)
                        {

                            org.jfree.data.time.TimeSeries ts = new org.jfree.data.time.TimeSeries (c.getName ());

                            int diff = 0;

                            int min = Integer.MAX_VALUE;
                            int max = Integer.MIN_VALUE;

                            // Get all the word counts for the chapter.
                            java.util.List<WordCount> wordCounts = dh.getWordCounts (c,
                                                                                     -7);

                            if (wordCounts.size () == 0)
                            {

                                wordCounts.add (new WordCount (chc.getWordCount (),
                                                               null,
                                                               Utils.zeroTimeFieldsForDate (new Date ())));

                            }

                            // Expand the dates back if necessary.
                            if (wordCounts.size () == 1)
                            {

                                // Get a date for 7 days ago.
                                Date d = new Date (System.currentTimeMillis () - (7 * 24 * 60 * 60 * 1000));

                                wordCounts.add (0,
                                                new WordCount (chc.getWordCount (),
                                                               null,
                                                               Utils.zeroTimeFieldsForDate (d)));

                            }

                            for (WordCount wc : wordCounts)
                            {

                                int count = wc.getCount ();

                                min = Math.min (min,
                                                count);
                                max = Math.max (max,
                                                count);

                                try
                                {

                                    ts.add (new org.jfree.data.time.Day (wc.getEnd ()),
                                            count);

                                } catch (Exception e) {

                                    // Ignore, trying to add a duplicate day.

                                }

                            }

                            diff = max - min;

                            int wordDiff = diff;

                            if (diff == 0)
                            {

                                diff = 100;

                            }

                            if ((min < Integer.MAX_VALUE) ||
                                (max > Integer.MIN_VALUE))
                            {

                                this.chapterSparkLineLabel.setText (String.format (Environment.getUIString (prefix,
                                                                                                            LanguageStrings.labels,
                                                                                                            LanguageStrings.sparkline),//"past 7 days, <b>%s%s</b> words",
                                                                                   Environment.formatNumber (7),
                                                                                   (wordDiff == 0 ? "" : (wordDiff > 0 ? "+" : "")),
                                                                                   Environment.formatNumber (wordDiff)));

                                org.jfree.chart.ChartPanel cp = new org.jfree.chart.ChartPanel (QuollChartUtils.createSparkLine (ts,
                                                                                                                         max + (diff / 2),
                                                                                                                         min - (diff / 2)));

                                //cp.setToolTipText ("Word count activity for the past 7 days");

                                cp.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                  16));
                                cp.setPreferredSize (new Dimension (60,
                                                                    16));
                                this.chapterSparkLine.add (cp);

                            }

                        }

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to generate 7 day activity sparkline",
                                              e);

                    }

                }

            }

        } else {

            this.chapterItem.setVisible (false);

        }

        this.allReadability.setVisible (false);
        this.allReadabilityHeader.setVisible (false);

        if ((this.viewer.isLanguageEnglish ())
            &&
            (achc.getWordCount () >= Constants.MIN_READABILITY_WORD_COUNT)
           )
        {

            this.allReadability.setVisible (true);
            this.allReadabilityHeader.setVisible (true);

            ReadabilityIndices ri = this.viewer.getAllReadabilityIndices ();

            this.allChaptersFleschKincaid.setText (Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ())));

            this.allChaptersFleschReadingEase.setText (Environment.formatNumber (Math.round (ri.getFleschReadingEase ())));

            this.allChaptersGunningFog.setText (Environment.formatNumber (Math.round (ri.getGunningFogIndex ())));

        }

        this.allChaptersWordCount.setText (Environment.formatNumber (achc.getWordCount ()));

        this.allChaptersPages.setText (Environment.formatNumber (achc.getStandardPageCount ())); //this.viewer.getAllChaptersA4PageCount ()));

        this.allChaptersEditPointBox.setVisible (false);

        final StringBuilder buf = new StringBuilder ();

        Set<NamedObject> chapters = this.viewer.getProject ().getAllNamedChildObjects (Chapter.class);

        int editComplete = 0;

        for (NamedObject n : chapters)
        {

            Chapter nc = (Chapter) n;

            if (nc.getEditPosition () > 0)
            {

                if (buf.length () > 0)
                {

                    buf.append (" ");

                }

                AbstractEditorPanel pan = this.viewer.getEditorForChapter (nc);

                String t = null;

                if (pan != null)
                {

                    t = pan.getEditor ().getText ();

                } else {

                    t = nc.getChapterText ();

                }

                if (nc.getEditPosition () <= t.length ())
                {

                    buf.append (t.substring (0,
                                             nc.getEditPosition ()));

                }

            }

            if (nc.isEditComplete ())
            {

                editComplete++;

            }

        }

        String allEditText = buf.toString ().trim ();

        if (buf.length () > 0)
        {

            ChapterCounts allc = new ChapterCounts (buf.toString ());

            this.allEditPointWordCount.setText (String.format (valueperc,
                                                               Environment.formatNumber (allc.getWordCount ()),
                                                               Environment.formatNumber (Utils.getPercent (allc.getWordCount (), achc.getWordCount ()))));

            this.allChaptersEditCount.setText (String.format (valueperc,
                                                              Environment.formatNumber (editComplete),
                                                              Environment.formatNumber (Utils.getPercent (editComplete, chapters.size ()))));

            this.allChaptersEditPointBox.setVisible (true);

        }

    }

    private JComponent getWords (JLabel     wordCount,
                                 JLabel     pagesCount,
                                 JComponent cp,
                                 JLabel     sparkLineLabel,
                                 int        wordCountDiff,
                                 int        days)
    {

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.wordcount);
        prefix.add (LanguageStrings.labels);

        String cols = COL_SPEC;

        String rows = "p, 6px, p, 6px, p";

        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();

        wordCount.setFont (wordCount.getFont ().deriveFont (Font.BOLD));

        b.add (wordCount,
               cc.xy (1, 1));

        b.addLabel (Environment.getUIString (prefix,
                                             LanguageStrings.words),
                                             //"words",
                    cc.xy (3, 1));

        pagesCount.setFont (pagesCount.getFont ().deriveFont (Font.BOLD));

        b.add (pagesCount,
               cc.xy (1, 3));

        b.addLabel (Environment.getUIString (prefix,
                                             LanguageStrings.a4pages),
                                             //"A4 pages",
                    cc.xy (3, 3));

        String sl = Environment.getUIString (prefix,
                                             LanguageStrings.sparkline);

        sparkLineLabel.setText (String.format (sl,
                                               Environment.formatNumber (days),
                                               (wordCountDiff > 0 ? "+" : (wordCountDiff == 0 ? "" : "-")),
                                               Environment.formatNumber (wordCountDiff)));
                                               //"past " + days + " days" + diff);

        b.add (sparkLineLabel,
               cc.xy (3, 5));

        b.add (cp,
               cc.xywh (1, 5, 1, 1));

        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (new EmptyBorder (10, 10, 10, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;

    }

    private JComponent getReadability (JLabel fleschKincaid,
                                       JLabel fleschReading,
                                       JLabel gunningFog)
    {

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.wordcount);
        prefix.add (LanguageStrings.labels);

        String cols = COL_SPEC;

        String rows = "p, 6px, p, 6px, p";

        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();
        /*
        b.addLabel ("F-K",
                    cc.xy (1,
                           1));
        b.addLabel ("FR",
                    cc.xy (3,
                           1));
        b.addLabel ("GF",
                    cc.xy (5,
                           1));
        */

        fleschKincaid.setFont (fleschKincaid.getFont ().deriveFont (Font.BOLD));

        b.add (fleschKincaid,
               cc.xy (1, 1));

        b.addLabel (Environment.getUIString (prefix,
                                             LanguageStrings.fk),
                                             //"Flesch-Kincaid",
                    cc.xy (3, 1));

        fleschReading.setFont (fleschReading.getFont ().deriveFont (Font.BOLD));

        b.add (fleschReading,
               cc.xy (1, 3));

        b.addLabel (Environment.getUIString (prefix,
                                             LanguageStrings.fr),
                                             //"Flesch Reading",
                    cc.xy (3, 3));

        gunningFog.setFont (gunningFog.getFont ().deriveFont (Font.BOLD));

        b.add (gunningFog,
               cc.xy (1, 5));

        b.addLabel (Environment.getUIString (prefix,
                                             LanguageStrings.gf),
                                             //"Gunning Fog",
                    cc.xy (3, 5));

        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (UIUtils.createPadding (10, 10, 10, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;

    }

    public List<JComponent> getHeaderControls ()
    {

        final WordCountsSideBar _this = this;

        List<JComponent> buts = new ArrayList ();

        JButton b = UIUtils.createButton ("chart",
                                          Constants.ICON_SIDEBAR,
                                          Environment.getUIString (LanguageStrings.project,
                                                                   LanguageStrings.sidebar,
                                                                   LanguageStrings.wordcount,
                                                                   LanguageStrings.headercontrols,
                                                                   LanguageStrings.items,
                                                                   LanguageStrings.statistics,
                                                                   LanguageStrings.tooltip),
                                          //"Click to view the detail",
                                          new ActionListener ()
                                          {

                                              @Override
                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                    _this.viewer.viewStatistics ();

                                              }

                                          });

        buts.add (b);

        return buts;

    }

    @Override
    public Dimension getMinimumSize ()
    {

        return new Dimension (260,
                              250);
    }

    public JComponent getContent ()
    {

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.wordcount);

        final WordCountsSideBar _this = this;

        Box box = new Box (BoxLayout.Y_AXIS);

        final Chapter c = this.viewer.getChapterCurrentlyEdited ();

        this.projectSessionWordCount = UIUtils.createInformationLabel (null);
        this.totalSessionWordCount = UIUtils.createInformationLabel (null);
        this.chapterWordCount = UIUtils.createInformationLabel (null);
        this.chapterPages = UIUtils.createInformationLabel (null);
        this.allChaptersWordCount = UIUtils.createInformationLabel (null);
        this.allChaptersPages = UIUtils.createInformationLabel (null);
        this.selectedWordCount = UIUtils.createInformationLabel (null);
        this.chapterSparkLine = new Box (BoxLayout.X_AXIS);
        this.chapterSparkLine.setOpaque (false);
        this.chapterSparkLineLabel = UIUtils.createInformationLabel (null);
        this.selectedFleschKincaid = UIUtils.createInformationLabel (null);
        this.selectedFleschReadingEase = UIUtils.createInformationLabel (null);
        this.selectedGunningFog = UIUtils.createInformationLabel (null);
        this.editPointWordCount = UIUtils.createInformationLabel (null);
        this.allEditPointWordCount = UIUtils.createInformationLabel (null);
        this.allChaptersEditCount = UIUtils.createInformationLabel (null);

        List<JComponent> items = new ArrayList ();

        items.add (this.getItem ("words",
                                 this.selectedWordCount));

        this.selectedReadabilityHeader = this.createSubHeader (Environment.getUIString (prefix,
                                                                                        LanguageStrings.labels,
                                                                                        LanguageStrings.readability));
                                                                                        //"Readability");

        items.add (this.selectedReadabilityHeader);

        this.selectedReadability = this.getReadability (this.selectedFleschKincaid,
                                                        this.selectedFleschReadingEase,
                                                        this.selectedGunningFog);

        items.add (this.selectedReadability);

        this.selectedItems = this.getItems (Environment.getUIString (prefix,
                                                                     LanguageStrings.sectiontitles,
                                                                     LanguageStrings.selected),
                                            //"Selected text",
                                            Constants.EDIT_ICON_NAME,
                                            items);

        this.selectedItems.setVisible (false);

        box.add (this.selectedItems);

        items = new ArrayList ();

        items.add (this.getItem (Environment.getUIString (prefix,
                                                          LanguageStrings.labels,
                                                          LanguageStrings.projectwords),
                                 this.projectSessionWordCount));

        items.add (this.getItem (Environment.getUIString (prefix,
                                                          LanguageStrings.labels,
                                                          LanguageStrings.totalwords),
                                                          //"words, total",
                                 this.totalSessionWordCount));

        AccordionItem it = this.getItems (getUIString (prefix,sectiontitles,session),
                                        //"This session",
                                          Constants.CLOCK_ICON_NAME,
                                          items);

        box.add (it);

        items = new ArrayList ();

        items.add (this.getWords (this.chapterWordCount,
                                  this.chapterPages,
                                  this.chapterSparkLine,
                                  this.chapterSparkLineLabel,
                                  0,
                                  7));

        this.chapterEditPointBox = new Box (BoxLayout.Y_AXIS);

        items.add (this.chapterEditPointBox);

        this.chapterEditPointBox.add (this.createSubHeader (Environment.getUIString (prefix,
                                                                                     LanguageStrings.labels,
                                                                                     LanguageStrings.edited)));
                                                          //"Edited"));

        this.chapterEditPointBox.add (this.getItem (Environment.getUIString (prefix,
                                                                             LanguageStrings.labels,
                                                                             LanguageStrings.words),
                                                          //"words",
                                                    this.editPointWordCount));

        //this.chapterEditPointBox.setVisible (false);

        UIUtils.setPadding (this.chapterEditPointBox, 0, 0, 10, 0);

        this.chapterFleschKincaid = UIUtils.createInformationLabel (null);
        this.chapterFleschReadingEase = UIUtils.createInformationLabel (null);
        this.chapterGunningFog = UIUtils.createInformationLabel (null);

        this.chapterReadabilityHeader = this.createSubHeader (getUIString (prefix,labels,readability));
        //"Readability");

        items.add (this.chapterReadabilityHeader);

        this.chapterReadability = this.getReadability (this.chapterFleschKincaid,
                                                       this.chapterFleschReadingEase,
                                                       this.chapterGunningFog);

        items.add (this.chapterReadability);

        this.chapterItem = this.getItems ("",
                                          Chapter.OBJECT_TYPE,
                                          items);

        if (c == null)
        {

            this.chapterItem.setVisible (false);

        }

        box.add (this.chapterItem);

        items = new ArrayList ();

        JComponent sparkLine = new JPanel ();
        sparkLine.setBorder (null);
        sparkLine.setOpaque (false);

        int wordCountDiff30 = 0;

        try
        {

            ProjectDataHandler dh = (ProjectDataHandler) this.viewer.getDataHandler (Project.class);

            // TODO: Find a better way of handling this.
            if (dh != null)
            {

                org.jfree.data.time.TimeSeries ts = new org.jfree.data.time.TimeSeries ("All");

                int diff = 0;

                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;

                // Get all the word counts for the project.
                java.util.List<WordCount> wordCounts = dh.getWordCounts (this.viewer.getProject (),
                                                                         -30);

                for (WordCount wc : wordCounts)
                {

                    int count = wc.getCount ();

                    min = Math.min (min,
                                    count);
                    max = Math.max (max,
                                    count);

                    ts.add (new org.jfree.data.time.Day (wc.getEnd ()),
                            count);

                }

                diff = max - min;

                if (diff == 0)
                {

                    diff = 100;

                }

                if ((min < Integer.MAX_VALUE) ||
                    (max > Integer.MIN_VALUE))
                {

                    wordCountDiff30 = max - min;

                    org.jfree.chart.ChartPanel cp = new org.jfree.chart.ChartPanel (QuollChartUtils.createSparkLine (ts,
                                                                                                             max + (diff / 2),
                                                                                                             min - (diff / 2)));

                    //cp.setToolTipText ("Word count activity for the past 30 days");
                    cp.setMaximumSize (new Dimension (60,
                                                      16));
                    cp.setPreferredSize (new Dimension (60,
                                                        16));

                    sparkLine = cp;

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to generate 30 day activity sparkline",
                                  e);

        }

        items.add (this.getWords (this.allChaptersWordCount,
                                  this.allChaptersPages,
                                  sparkLine,
                                  UIUtils.createInformationLabel (null),
                                  wordCountDiff30,
                                  30));

        this.allChaptersFleschKincaid = UIUtils.createInformationLabel (null);
        this.allChaptersFleschReadingEase = UIUtils.createInformationLabel (null);
        this.allChaptersGunningFog = UIUtils.createInformationLabel (null);

        this.allChaptersEditPointBox = new Box (BoxLayout.Y_AXIS);

        items.add (this.allChaptersEditPointBox);

        this.allChaptersEditPointBox.add (this.createSubHeader (Environment.getUIString (prefix,
                                                                                         LanguageStrings.labels,
                                                                                         LanguageStrings.edited)));
                                                          //"Edited"));

        this.allChaptersEditPointBox.add (this.getItem (Environment.getUIString (prefix,
                                                                                 LanguageStrings.labels,
                                                                                 LanguageStrings.words),
                                                          //"words",
                                                        this.allEditPointWordCount));

        this.allChaptersEditPointBox.add (this.getItem (Environment.getUIString (prefix,
                                                                                 LanguageStrings.labels,
                                                                                 LanguageStrings.chapters),
                                                          //"{chapters}",
                                                        this.allChaptersEditCount));

        this.allChaptersEditPointBox.setVisible (false);

        this.allChaptersEditPointBox.setBorder (new EmptyBorder (0, 0, 10, 0));

        this.allReadabilityHeader = this.createSubHeader (Environment.getUIString (prefix,
                                                                                   LanguageStrings.labels,
                                                                                   LanguageStrings.readability));
                                                          //"Readability");

        items.add (this.allReadabilityHeader);

        this.allReadability = this.getReadability (this.allChaptersFleschKincaid,
                                                   this.allChaptersFleschReadingEase,
                                                   this.allChaptersGunningFog);

        items.add (this.allReadability);

        box.add (this.getItems (Environment.getUIString (prefix,
                                                         LanguageStrings.sectiontitles,
                                                         LanguageStrings.allchapters),
                                                          //"All {Chapters}",
                                Book.OBJECT_TYPE,
                                items));

        final JLabel history = UIUtils.createClickableLabel (Environment.getUIString (prefix,
                                                                                      LanguageStrings.viewdetaillink),
                                                          //"View Detail",
                                                             Environment.getIcon ("chart",
                                                                                  Constants.ICON_MENU));

        UIUtils.setPadding (history, 0, 5, 0, 0);

        box.add (history);
        box.add (Box.createVerticalStrut (10));

        history.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                _this.viewer.viewStatistics ();

            }

        });

        JLabel l = UIUtils.createClickableLabel (Environment.getUIString (prefix,
                                                                          LanguageStrings.helplink),
                                                          //"Click to find out more about<br />the Readability indices",
                                                 Environment.getIcon ("help",
                                                                      Constants.ICON_MENU));

        UIUtils.setPadding (l, 0, 5, 0, 0);

        l.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                // Open the url.
                UIUtils.openURL (_this,
                                 "help://chapters/readability");

            }

        });

        box.add (l);

        box.add (Box.createVerticalGlue ());

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.update ();

            }

        });

        return this.wrapInScrollPane (box);

    }

    private JComponent getItem (String     label,
                                JComponent value)
    {

        String cols = COL_SPEC;

        String rows = "p";

        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();

        value.setFont (value.getFont ().deriveFont (Font.BOLD));

        b.add (value,
               cc.xy (1, 1));

        b.addLabel (Environment.replaceObjectNames (label),
                    cc.xy (3, 1));

        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (new EmptyBorder (6, 10, 0, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;

    }

    private AccordionItem getItems (String title,
                                    String iconType,
                                    List<JComponent> items)
    {

        final Box b = new Box (BoxLayout.Y_AXIS);

        for (JComponent c : items)
        {

            c.setAlignmentY (Component.TOP_ALIGNMENT);

            b.add (c);

        }

        b.setOpaque (false);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.add (Box.createVerticalGlue ());

        AccordionItem it = new AccordionItem (title,
                                              iconType)
        {

            @Override
            public JComponent getContent ()
            {

                return b;

            }

        };

        Header h = it.getHeader ();

        //h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (12d)).deriveFont (java.awt.Font.PLAIN));

        h.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0, 0, 3, 0)),
                                         h.getBorder ()));

        it.init ();

        it.revalidate ();

        it.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         it.getPreferredSize ().height));
        return it;

    }

    private JLabel createSubHeader (String title)
    {

        JLabel ll = UIUtils.createInformationLabel (String.format ("<i>%s</i>",
                                                                   title));

        UIUtils.setPadding (ll, 0, 10, 0, 0);

        return ll;

    }

    @Override
    public void init (String saveState)
               throws GeneralException
    {

        super.init (saveState);

    }

}
