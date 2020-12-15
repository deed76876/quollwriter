package com.quollwriter.text.rules;

import java.util.*;

import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.*;

import java.text.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import org.dom4j.*;

import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class SentenceComplexityRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String ratio = "ratio";
        public static final String wordCount = "wordCount";

    }

    private float      ratio = 0;
    private int wordCount = 0;
    private javax.swing.JSpinner ratioF = null;
    private javax.swing.JSpinner wordCountF = null;

    private Spinner<Double> ratioF2 = null;
    private Spinner<Integer> wordCountF2 = null;

    public SentenceComplexityRule ()
    {

    }

    public SentenceComplexityRule (float   syllableWordRatio,
                                   int     wordCount,
                                   boolean user)
    {

        this.ratio = syllableWordRatio;
        this.wordCount = wordCount;
        this.setUserRule (user);

    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        d = Utils.replaceString (d,
                                       "[RATIO]",
                                       Environment.formatNumber (this.ratio) + "");

        d = Utils.replaceString (d,
                                       "[COUNT]",
                                       this.wordCount + "");

        return d;

    }

    @Override
    public String getSummary ()
    {

        String t = Utils.replaceString (super.getSummary (),
                                              "[RATIO]",
                                              Environment.formatNumber (this.ratio) + "");

        t = Utils.replaceString (t,
                                       "[COUNT]",
                                       this.wordCount + "");

        return t;

    }

    @Override
    public void init (Element root)
               throws GeneralException
    {

        super.init (root);

        this.ratio = DOM4JUtils.attributeValueAsFloat (root,
                                                         XMLConstants.ratio);
        this.wordCount = DOM4JUtils.attributeValueAsInt (root,
                                                       XMLConstants.wordCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.addAttribute (XMLConstants.ratio,
                           this.ratio + "");
        root.addAttribute (XMLConstants.wordCount,
                           this.wordCount + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        List<Issue> issues = new ArrayList<> ();

        float wordC = (float) sentence.getWordCount ();

        if (wordC <= this.wordCount)
        {

            return issues;

        }

        float syllC = (float) sentence.getSyllableCount ();

        float r = syllC / wordC;

        r = (float) Math.round (r * 10) / 10;

        if (r > this.ratio)
        {

            DecimalFormat df = new DecimalFormat ("##.#");

            String n = df.format (r);

            Issue iss = new Issue (String.format (Environment.getUIString (LanguageStrings.problemfinder,
                                                                           LanguageStrings.issues,
                                                                           LanguageStrings.sentencecomplexity,
                                                                           LanguageStrings.text),
                                                  //"Sentence syllable/word ratio is: <b>%s</b>.  (Max is: %s)",
                                                  n,
                                                  Environment.formatNumber (this.ratio)),
                                   sentence,
                                   sentence.getAllTextStartOffset () + "-sentencetoocomplex-" + r,
                                   this);

            issues.add (iss);

        }

        return issues;

    }

    @Override
    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    @Override
    public Set<com.quollwriter.ui.forms.FormItem> getFormItems ()
    {

        List<String> pref = new ArrayList<> ();
        pref.add (LanguageStrings.problemfinder);
        pref.add (LanguageStrings.config);
        pref.add (LanguageStrings.rules);
        pref.add (LanguageStrings.sentencecomplexity);
        pref.add (LanguageStrings.labels);

        Set<com.quollwriter.ui.forms.FormItem> items = new LinkedHashSet<> ();

        this.ratioF = new javax.swing.JSpinner (new javax.swing.SpinnerNumberModel (this.ratio,
                                                            0.1f,
                                                            3.0f,
                                                            0.1));

        javax.swing.Box b = new javax.swing.Box (javax.swing.BoxLayout.X_AXIS);
        b.add (this.ratioF);
        b.add (javax.swing.Box.createHorizontalGlue ());

        this.ratioF.setMaximumSize (this.ratioF.getPreferredSize ());

        items.add (new com.quollwriter.ui.forms.AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.ratio),
                                    b));

        this.wordCountF = new javax.swing.JSpinner (new javax.swing.SpinnerNumberModel (this.wordCount,
                                                            1,
                                                            500,
                                                            1));

        b = new javax.swing.Box (javax.swing.BoxLayout.X_AXIS);

        b.add (this.wordCountF);
        b.add (javax.swing.Box.createHorizontalGlue ());

        this.wordCountF.setMaximumSize (this.wordCountF.getPreferredSize ());

        items.add (new com.quollwriter.ui.forms.AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.sentencelength),
                                    //"Sentence length (words)",
                                    b));

        return items;

    }

    @Override
    public Set<Form.Item> getFormItems2 ()
    {

        List<String> pref = Arrays.asList (problemfinder,config,rules,sentencecomplexity,labels);

        Set<Form.Item> items = new LinkedHashSet<> ();

        this.ratioF2 = new Spinner<> (new DoubleSpinnerValueFactory (0.1d, 3.0d, this.ratio, 0.1d));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,LanguageStrings.ratio)),
                                  this.ratioF2));

        this.wordCountF2 = new Spinner<> (new IntegerSpinnerValueFactory (1, 500, this.wordCount, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,sentencelength)),
                                    //"Sentence length (words)",
                                  this.wordCountF2));

        return items;

    }

    public String getFormError ()
    {

        return null;

    }

    public void updateFromForm ()
    {

        this.ratio = ((javax.swing.SpinnerNumberModel) this.ratioF.getModel ()).getNumber ().floatValue ();
        this.wordCount = ((javax.swing.SpinnerNumberModel) this.wordCountF.getModel ()).getNumber ().intValue ();

    }

    @Override
    public void updateFromForm2 ()
    {

        this.ratio = this.ratioF2.getValue ().floatValue ();
        this.wordCount = this.wordCountF2.getValue ();

    }

}
