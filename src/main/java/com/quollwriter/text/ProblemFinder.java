package com.quollwriter.text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.text.rules.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.TextUnderlinePainter;
import com.quollwriter.ui.components.ActionAdapter;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class ProblemFinder extends Box
{

    private QuollEditorPanel     editor = null;
    private int                  lastCaret = -1;
    private List<IgnoreCheckbox>  ignores = new ArrayList ();
    private ActionListener       showIgnores = null;
    private boolean              endReached = false;
    private boolean              startReached = false;
    private boolean              doSentences = false;
    private boolean              donePara = false;
    private String               currPara = null;
    private Paragraph            paragraph = null;
    private Sentence             sentence = null;
    private TextIterator         textIter = null;
    private TextBlockIterator    iter = null;
    private int                  start = 0;
    private int                  end = -1;
    private JLabel               limitLabel = null;
    private JLabel               noProblemsLabel = null;
    private int origStart = -1;
    private int origEnd = -1;
    private Box issuesBox = null;
    private boolean inited = false;
    private BlockPainter lineHighlight = null;
    private TextUnderlinePainter issueHighlight = null;
    private ProjectViewer viewer = null;

    public ProblemFinder (QuollEditorPanel editor,
                          ProjectViewer    viewer)
    {

        super (BoxLayout.Y_AXIS);

        this.lineHighlight = ProblemFinder.getTextBlockHighlighter ();
        this.issueHighlight = ProblemFinder.getIssueHighlighter ();

        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));

        this.editor = editor;
        this.viewer = viewer;

        // Load the ignores.
        try
        {

            this.editor.getChapter ().initProblemFinderIgnoreDocumentPositions (this.editor.getEditor ().getDocument ());

/*
            ((ProjectViewer) this.editor.getViewer ()).getProblemFinderIgnores (this.editor.getChapter (),
                                                                                                             this.editor.getEditor ().getDocument ());
*/
        } catch (Exception e)
        {

            Environment.logError ("Unable to load problem finder ignores for chapter: " +
                                  this.editor.getChapter (),
                                  e);

        }

        final ProblemFinder _this = this;

        this.editor.getEditor ().getCaret ().addChangeListener (new javax.swing.event.ChangeListener ()
        {

            public void stateChanged (javax.swing.event.ChangeEvent ev)
            {

                if (!_this.isShowing ())
                {

                    return;

                }

                _this.inited = false;

                _this.start = _this.editor.getEditor ().getSelectionStart ();
                _this.end = _this.editor.getEditor ().getSelectionEnd ();

            }

        });

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.actions);
        prefix.add (LanguageStrings.problemfinder);

        this.limitLabel = new JLabel (Environment.getUIString (prefix,
                                                               LanguageStrings.limit),
                                    //"Limiting find to selected text.",
                                      Environment.getIcon ("information",
                                                           Constants.ICON_MENU),
                                      SwingConstants.TRAILING);

        this.limitLabel.setVisible (false);

        this.limitLabel.setBorder (UIUtils.createPadding (5, 10, 3, 3));

        this.noProblemsLabel = new JLabel (Environment.getUIString (prefix,
                                                                    LanguageStrings.noproblemsfound),
                                        //"No problems found.",
                                           Environment.getIcon ("information",
                                                                Constants.ICON_MENU),
                                           SwingConstants.TRAILING);

        this.noProblemsLabel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.noProblemsLabel.setOpaque (false);
        this.noProblemsLabel.setBorder (UIUtils.createPadding (5, 10, 3, 3));
        this.noProblemsLabel.setVisible (false);

        this.issuesBox = new Box (BoxLayout.Y_AXIS);
        this.issuesBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (this.limitLabel);
        this.add (this.noProblemsLabel);
        this.add (this.issuesBox);

    }

    public void removeAllIgnores ()
    {

        this.editor.getChapter ().getProblemFinderIgnores ().clear ();

        try
        {

            this.viewer.saveProblemFinderIgnores (this.editor.getChapter ());

        } catch (Exception e) {

            Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                  this.editor.getChapter (),
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.project,
                                                               LanguageStrings.editorpanel,
                                                               LanguageStrings.actions,
                                                               LanguageStrings.problemfinder,
                                                               LanguageStrings.unignoreall,
                                                               LanguageStrings.actionerror));
                                      //"Unable to remove all ignores.");

            return;

        }

        if (this.showIgnores != null)
        {

            this.showIgnores.actionPerformed (new ActionEvent (this,
                                                               0,
                                                               ""));

        }

        this.viewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                      ProjectEvent.UNIGNORE);

    }

    public Set<Issue> getIgnoredIssues ()
    {

        return this.editor.getChapter ().getProblemFinderIgnores ();

    }

    public void saveIgnores ()
    {

        // Check to see if they are still valid.
        try
        {

            this.viewer.saveProblemFinderIgnores (this.editor.getChapter ());

        } catch (GeneralException e)
        {

            Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                  this.editor.getChapter (),
                                  e);

        }

    }

    public void reset ()
    {

        this.getIgnores ();

        this.clearHighlights ();

    }

    private void init (int start,
                       int end)
    {

        this.start = start;
        this.end = end;

        this.getIgnores ();
        this.clearHighlights ();

        if (this.end < this.start)
        {

            this.end = -1;

        }

        if (this.start == this.end)
        {

            this.end = -1;

        }

        this.noProblemsLabel.setVisible (false);
        this.clearIssuesBox ();

        this.limitLabel.setVisible (this.end > -1);

        this.iter = new TextBlockIterator (this.editor.getEditor ().getText (),
                                           this.start,
                                           this.end);

        this.inited = true;

    }

    public int start ()
                      throws Exception
    {

        this.origStart = this.editor.getEditor ().getSelectionStart ();
        this.origEnd = this.editor.getEditor ().getSelectionEnd ();

        this.init (this.origStart,
                   this.origEnd);

        return this.next ();

    }

    private void clearHighlights ()
    {

        this.editor.getEditor ().removeAllHighlights (this.lineHighlight);
        this.editor.getEditor ().removeAllHighlights (this.issueHighlight);

    }

    private int processTextBlock (TextBlock b)
                           throws Exception
    {

        if (b == null)
        {

            return 0;

        }

        if (b instanceof Paragraph)
        {

            return this.handleParagraph ((Paragraph) b);

        }

        if (b instanceof Sentence)
        {

            return this.handleSentence ((Sentence) b);

        }

        throw new GeneralException ("Type: " +
                                    b.getClass ().getName () +
                                    " not supported.");

    }

    private int handleParagraph (Paragraph p)
                                 throws    Exception
    {

        List<Issue> issues = RuleFactory.getParagraphIssues (p,
                                                             this.viewer.getProject ().getProperties ());

        this.setPositions (issues);

        if ((issues.size () != 0)
            &&
            (!this.allIgnores (issues))
           )
        {

            this.handleIssues (issues,
                               p);

            return issues.size ();

        }

        return 0;

    }

    private int handleSentence (Sentence s)
                                throws   Exception
    {

        List<Issue> issues = RuleFactory.getSentenceIssues (s,
                                                            this.viewer.getProject ().getProperties ());

        this.setPositions (issues);

        if ((issues.size () != 0)
            &&
            (!this.allIgnores (issues))
           )
        {

            this.handleIssues (issues,
                               s);

            return issues.size ();

        }

        return 0;

    }

    public int next ()
              throws Exception
    {

        if (!this.inited)
        {

            this.init (this.start,
                       this.end);

        }

        this.noProblemsLabel.setVisible (false);

        // Prevent the block being highlighted but ensure that inited is still true since the caret
        // change will set it to false.
        this.editor.getEditor ().setSelectionEnd (this.start);

        this.inited = true;

        final ProblemFinder _this = this;

        this.getIgnores ();

        this.clearHighlights ();

        TextBlock b = null;

        while ((b = this.iter.next ()) != null)
        {

            Environment.logDebugMessage ("Looking for problems in: " + b);

            int c = this.processTextBlock (b);

            if (c > 0)
            {

                Environment.logDebugMessage ("Got: " + c + " problems for text: " + b);

                return c;

            }

        }

        this.addNoProblems ();

        if (this.end > -1)
        {

            java.util.List<String> prefix = Arrays.asList (project,editorpanel,actions,problemfinder,nomoreproblems,selected,LanguageStrings.end);

            UIUtils.showMessage (this.viewer,
                                 getUIString (prefix,title),
                                 //"No more problems found",
                                 getUIString (prefix,text),
                                 //"No more problems found in selected text.",
                                 getUIString (buttons,finish),
                                 //"Finish",
                                 new ActionListener ()
                                 {

                                    public void actionPerformed (ActionEvent ev)
                                    {

                                        _this.editor.getEditor ().select (_this.start,
                                                                          _this.end);

                                        _this.inited = false;

                                    }

                                 });

            return 0;

        }

        ActionListener onCancel = new ActionListener ()
        {

           public void actionPerformed (ActionEvent ev)
           {

               _this.reset ();

           }

        };

        final java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.actions);
        prefix.add (LanguageStrings.problemfinder);
        prefix.add (LanguageStrings.nomoreproblems);
        prefix.add (LanguageStrings.end);

        UIUtils.createQuestionPopup (this.viewer,
                                     Environment.getUIString (prefix,
                                                              LanguageStrings.title),
                                     //"No more problems found",
                                     Constants.INFO_ICON_NAME,
                                     Environment.getUIString (prefix,
                                                              LanguageStrings.text),
                                    //"No more problems found.  Return to the start of the {chapter}?",
                                     Environment.getUIString (prefix,
                                                              LanguageStrings.buttons,
                                                              LanguageStrings.confirm),
                                    //"Yes, return to the start",
                                     Environment.getUIString (prefix,
                                                              LanguageStrings.buttons,
                                                              LanguageStrings.cancel),
                                    //null,
                                     new ActionListener ()
                                     {

                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            UIUtils.doLater (new ActionListener ()
                                            {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    try
                                                    {

                                                        _this.init (0,
                                                                    -1);

                                                        _this.next ();

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to move back to start",
                                                                              e);

                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  Environment.getUIString (prefix,
                                                                                                           LanguageStrings.actionerror));
                                                                                  //"Unable to move back to start of {chapter}");

                                                    }

                                                }

                                            });

                                        }

                                     },
                                     onCancel,
                                     onCancel,
                                     null);

        return 0;

    }

    public int previous ()
                  throws Exception
    {

        if (!this.inited)
        {

            this.init (this.start,
                       this.end);

        }

        this.noProblemsLabel.setVisible (false);

        final ProblemFinder _this = this;

        this.getIgnores ();

        this.clearHighlights ();

        TextBlock b = null;

        while ((b = this.iter.previous ()) != null)
        {

            Environment.logDebugMessage ("Looking for problems in: " + b);

            int c = this.processTextBlock (b);

            if (c > 0)
            {

                Environment.logDebugMessage ("Got: " + c + " problems for text: " + b);

                return c;

            }

        }

        this.addNoProblems ();

        if (this.end > -1)
        {

            java.util.List<String> prefix = Arrays.asList (project,editorpanel,actions,problemfinder,nomoreproblems,selected,LanguageStrings.end);

            UIUtils.showMessage (this.viewer,
                                 getUIString (prefix,title),
                                 //"No more problems found",
                                 getUIString (prefix,text),
                                 //"No more problems found in selected text.",
                                 getUIString (buttons,finish),
                                 //"Finish",
                                 null);

            return 0;

        }

        final java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.actions);
        prefix.add (LanguageStrings.problemfinder);
        prefix.add (LanguageStrings.nomoreproblems);
        prefix.add (LanguageStrings.start);

        UIUtils.showMessage ((PopupsSupported) this.editor,
                             Environment.getUIString (prefix,
                                                      LanguageStrings.title),
                            //"No more problems",
                            Environment.getUIString (prefix,
                                                     LanguageStrings.text));
                             //"No more problems found");

        return 0;

    }

    private void addNoProblems ()
    {

        this.clearIssuesBox ();

        this.noProblemsLabel.setVisible (true);

        this.getParent ().getParent ().validate ();
        this.getParent ().getParent ().repaint ();

    }

    public void removeCheckboxesForRule (Rule r)
    {

        for (IgnoreCheckbox b : this.ignores)
        {

            if (r.getId ().equals (b.issue.getRule ().getId ()))
            {

                this.remove (b);

            }

        }

        this.getParent ().getParent ().validate ();
        this.getParent ().getParent ().repaint ();

    }

    private void getIgnores ()
    {

        for (IgnoreCheckbox b : this.ignores)
        {

            if (b.isSelected ())
            {

                this.editor.getChapter ().getProblemFinderIgnores ().add (b.issue);

                this.viewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                              ProjectEvent.IGNORE);

            } else {

                if (this.editor.getChapter ().getProblemFinderIgnores ().remove (b.issue))
                {

                    this.viewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                  ProjectEvent.UNIGNORE);

                }

            }

        }

        this.ignores = new ArrayList ();

    }

    private IgnoreCheckbox createIssueItem (Issue iss)
    {

        IgnoreCheckbox cb = new IgnoreCheckbox (iss.getDescription (),
                                                iss,
                                                this.editor,
                                                this.viewer);
        cb.setAlignmentX (Component.LEFT_ALIGNMENT);
        cb.setOpaque (false);
        cb.setBorder (new EmptyBorder (5,
                                       10,
                                       3,
                                       3));

        final ProblemFinder _this = this;

        final QTextEditor ed = this.editor.getEditor ();

        final Issue issue = iss;

        cb.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void mouseEntered (MouseEvent ev)
            {

                try
                {

                    ed.addHighlight (iss.getStartIssuePosition (),
                                     iss.getEndIssuePosition (),
                                     _this.issueHighlight,
                                     false);

                } catch (Exception e)
                {

                    // Ignore.

                }

            }

            @Override
            public void mouseExited (MouseEvent ev)
            {

                ed.removeAllHighlights (_this.issueHighlight);

            }

        });

        this.ignores.add (cb);

        return cb;

    }

    private void setPositions (List<Issue> issues)
    {

        try
        {

            for (Issue i : issues)
            {

                i.setStartPosition (this.editor.getEditor ().getDocument ().createPosition (i.getStartIssuePosition ()));

                // Set the end position of the sentence.
                i.setEndPosition (this.editor.getEditor ().getDocument ().createPosition (i.getEndIssuePosition ()));

            }

        } catch (Exception e)
        {

            // Ignore.

        }

    }

    private void clearIssuesBox ()
    {

        this.issuesBox.removeAll ();

    }

    private void handleIssues (final List<Issue> issues,
                               final TextBlock   textBlock)
                        throws Exception
    {

        Collections.sort (issues,
                          new IssueSorter ());

        List<Issue> ignored = this.removeIgnores (issues);

        if (issues.size () > 0)
        {

            this.clearIssuesBox ();

            final ProblemFinder _this = this;

            this.lastCaret = this.editor.getEditor ().getSelectionStart ();

            // Convert to the view.
            int height = -1;

            UIUtils.doLater (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        _this.editor.scrollToPosition (textBlock.getAllTextStartOffset ());//selStart);//_this.sentIter.getOffset ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to scroll to: " +
                                              textBlock.getAllTextStartOffset (), //selStart, //_this.sentIter.getOffset (),
                                              e);

                    }

                }

            });

            final QTextEditor ed = this.editor.getEditor ();

            for (Issue i : issues)
            {

                this.issuesBox.add (this.createIssueItem (i));
/*
                if (i.getRule () instanceof SentenceRule)
                {

                    this.issuesBox.add (this.createSentenceIssueItem (i,
                                                                      (Sentence) textBlock));

                } else {

                    this.issuesBox.add (this.createIssueItem (i));

                }
*/
            }

            if (ignored.size () > 0)
            {

                java.util.List<String> prefix = new ArrayList<> ();
                prefix.add (LanguageStrings.project);
                prefix.add (LanguageStrings.editorpanel);
                prefix.add (LanguageStrings.actions);
                prefix.add (LanguageStrings.problemfinder);
                prefix.add (LanguageStrings.unignoreissues);

                String t = null;

                if (ignored.size () == 1)
                {

                    t = Environment.getUIString (prefix,
                                                 LanguageStrings.single);

                } else {

                    t = String.format (Environment.getUIString (prefix,
                                                                LanguageStrings.plural),
                                       Environment.formatNumber (ignored.size ()));

                }

                final JLabel l = UIUtils.createClickableLabel (t,
                                                               Environment.getIcon ("warning",
                                                                                    Constants.ICON_MENU));

                l.setBorder (new EmptyBorder (5,
                                              10,
                                              3,
                                              3));

                this.issuesBox.add (l);

                final java.util.List<IgnoreCheckbox> ignoredItems = new ArrayList ();

                for (Issue i : ignored)
                {

                    IgnoreCheckbox icb = this.createIssueItem (i);

                    ignoredItems.add (icb);
                    icb.setVisible (false);
                    icb.setSelected (true);
                    this.issuesBox.add (icb);

                }

                this.showIgnores = new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        for (IgnoreCheckbox icb : ignoredItems)
                        {

                            icb.setVisible (true);

                            icb.setSelected ("selected".equals (ev.getActionCommand ()));

                            //_this.issuesToIgnore.remove (icb.issue);

                        }

                        l.setVisible (false);

                        _this.viewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                       ProjectEvent.UNIGNORE);

                        _this.getParent ().getParent ().validate ();
                        _this.getParent ().getParent ().repaint ();

                    }

                };

                l.addMouseListener (new MouseAdapter ()
                {

                    public void mousePressed (MouseEvent ev)
                    {

                        _this.showIgnores.actionPerformed (new ActionEvent (l,
                                                                            0,
                                                                            "selected"));

                        _this.viewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                       ProjectEvent.UNIGNORE);

                    }

                });

            }

            int end = textBlock.getAllTextEndOffset () + 1;

            if (textBlock instanceof Sentence)
            {

                end = ((Sentence) textBlock).getLastWord ().getAllTextEndOffset ();

            }

            ed.addHighlight (textBlock.getAllTextStartOffset (),
                             end,
                             this.lineHighlight,
                             false);

            this.getParent ().getParent ().validate ();
            this.getParent ().getParent ().repaint ();

        }

    }

    private boolean allIgnores (List<Issue> issues)
    {

        for (Issue i : issues)
        {

            if (!this.shouldIgnore (i))
            {

                return false;

            }

        }

        return true;

    }

    private List<Issue> removeIgnores (List<Issue> issues)
    {

        Iterator<Issue> iter = issues.iterator ();

        List<Issue> ignored = new ArrayList ();

        while (iter.hasNext ())
        {

            Issue i = iter.next ();

            if (this.shouldIgnore (i))
            {

                ignored.add (i);
                iter.remove ();

            }

        }

        return ignored;

    }

    private boolean shouldIgnore (Issue iss)
    {

        for (Issue i : this.editor.getChapter ().getProblemFinderIgnores ())
        {

            if (i.equals (iss))
            {

                return true;

            }

        }

        return false;

    }

    private class TextBlockIterator
    {

        private TextIterator iter = null;
        private Paragraph para = null;
        private Sentence sent = null;
        private int endAt = -1;
        private int startAt = -1;
        private TextBlock current = null;

        public TextBlockIterator (String text,
                                  int    startAt,
                                  int    endAt)
        {

            this.iter = new TextIterator (text);
            this.startAt = startAt;

            this.endAt = endAt;

        }

        public TextBlock next ()
        {

            if (this.para == null)
            {

                this.para = this.iter.getNextClosestParagraphTo (this.startAt);

                if (this.para == null)
                {

                    // At the end.
                    return null;

                }

                if (this.startAt > this.para.getStart ())
                {

                    this.sent = this.para.getNextClosestSentenceTo (this.startAt - this.para.getStart ());

                    return this.sent;

                }

            } else {

                if (this.sent == null)
                {

                    this.sent = this.para.getFirstSentence ();

                } else {

                    this.sent = this.sent.getNext ();

                }

                if (this.sent == null)
                {

                    // Get the next paragraph.
                    this.para = this.para.getNext ();

                } else {

                    if ((this.sent.getAllTextStartOffset () > this.endAt)
                        &&
                        (this.endAt > -1)
                       )
                    {

                        return null;

                    }

                    return this.sent;

                }

                if (this.para == null)
                {

                    // Reached the end;
                    return null;

                }

            }

            if ((this.para.getAllTextStartOffset () > this.endAt)
                &&
                (this.endAt > -1)
               )
            {

                return null;

            }

            return this.para;

        }

        public TextBlock previous ()
        {

            if (this.para == null)
            {

                this.para = this.iter.getPreviousClosestParagraphTo (this.startAt);

                if (this.para == null)
                {

                    return null;

                }

                if (this.startAt > this.para.getEnd ())
                {

                    this.sent = this.para.getLastSentence ();

                } else {

                    this.sent = this.para.getPreviousClosestSentenceTo (this.startAt - this.para.getStart ());

                }

            } else {

                if (this.sent == null)
                {

                    this.para = this.para.getPrevious ();

                    if (this.para == null)
                    {

                        return null;

                    }

                    this.sent = this.para.getLastSentence ();

                } else {

                    this.sent = this.sent.getPrevious ();

                }

                if (this.sent == null)
                {

                    if ((this.startAt > this.para.getAllTextStartOffset ())
                        &&
                        (this.endAt > -1)
                       )
                    {

                        return null;

                    }

                    return this.para;

                }

            }

            if ((this.startAt > this.sent.getAllTextStartOffset ())
                &&
                (this.endAt > 1)
               )
            {

                return null;

            }

            return this.sent;

        }

    }

    public static BlockPainter getTextBlockHighlighter ()
    {

        return new BlockPainter (UIUtils.getHighlightColor ());

    }

    public static TextUnderlinePainter getIssueHighlighter ()
    {

        return new TextUnderlinePainter (Color.RED);

    }

}
