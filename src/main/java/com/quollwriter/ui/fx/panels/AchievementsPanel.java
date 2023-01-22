package com.quollwriter.ui.fx.panels;

import java.util.*;
import java.util.stream.*;

import javafx.animation.*;
import javafx.scene.shape.*;
import javafx.util.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;
import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

public class AchievementsPanel extends PanelContent<AbstractViewer>
{

    public static final String PANEL_ID = "achievements";
    private CheckBox hide = null;

    public AchievementsPanel (AbstractViewer viewer,
                              Set<Node>      headerControls)
    {

        super (viewer);

        final AchievementsManager man = Environment.getAchievementsManager ();

        // Compiler needs help here.
        this.addSetChangeListener (man.userAchievedRules (),
                                   ev ->
        {

            AchievementRule add = ev.getElementAdded ();

            if (add == null)
            {

                return;

            }

            Node n = this.lookup ("#achievement-" + add.getId ());

            if (n != null)
            {

                n.pseudoClassStateChanged (StyleClassNames.ACHIEVED_PSEUDO_CLASS, true);

            }

        });

        VBox content = new VBox ();

        UIUtils.runLater (() ->
        {

            this.getChildren ().add (content);

        });

        Header h = Header.builder ()
            .title (achievementspanel,title)
            .controls (headerControls)
            .iconClassName (StyleClassNames.ACHIEVEMENTS)
            .build ();

        QuollTextView desc = QuollTextView.builder ()
            .text (getUILanguageStringProperty (achievementspanel,text))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (this.viewer)
            .build ();

        this.hide = QuollCheckBox.builder ()
            .styleClassName (StyleClassNames.HIDE)
            .label (getUILanguageStringProperty (achievementspanel,hideachieved))
            .onAction (ev ->
            {

                this.lookupAll (".achievement").stream ()
                    .forEach (el ->
                    {

/*
TODO?
                        Animation anim = new Transition ()
                        {

                            {
                                setCycleDuration (Duration.millis (10000));
                            }

                            @Override
                            protected void interpolate (double f)
                            {

                                Region r = (Region) el;

                                r.setPrefHeight (r.getPrefHeight () * (float) f);
                                r.setClip (new Rectangle (r.getWidth (), r.getPrefHeight ()));

                            }

                        };

                        anim.play ();
*/
                        if (el.getPseudoClassStates ().contains (StyleClassNames.ACHIEVED_PSEUDO_CLASS))
                        {

                            el.setVisible (!this.hide.isSelected ());

                        }

                    });

            })
            .build ();

        Set<AchievementRule> userRules = man.getUserRules ();

        Set<String> userAchievedIds = man.getUserAchievedRules ().stream ()
            .map (r -> r.getId ())
            .collect (Collectors.toSet ());

        //Set<AchievementRule> userAchieved = man.getUserAchievedRules ();

        VBox all = new VBox ();

        ScrollPane sp = new ScrollPane (all);

        sp.vvalueProperty ().addListener ((pr, oldv, newv) ->
        {

            sp.pseudoClassStateChanged (StyleClassNames.SCROLLING_PSEUDO_CLASS, newv.doubleValue () > 0);

        });

        content.getChildren ().addAll (h, desc, hide, sp);

        VBox genItems = new VBox ();

        userRules.stream ()
            .forEach (r ->
            {

                genItems.getChildren ().add (new AchievementView (r,
                                                                  userAchievedIds.contains (r.getId ()),
                                                                  this.getBinder ()));

            });

        StringProperty titleProp = new SimpleStringProperty ();
        titleProp.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            return String.format (getUIString (achievementspanel,sectiontitles,user),
                                  man.getUserAchievedRules ().size (),
                                  Environment.formatNumber (userRules.size ()));

        },
        man.userAchievedRules ()));

        AccordionItem gen = AccordionItem.builder ()
            .styleClassName (StyleClassNames.GENERAL)
            .headerIconClassName (StyleClassNames.ACHIEVEMENTS)
            .title (titleProp)
            .openContent (genItems)
            .build ();

        all.getChildren ().add (gen);

        if (viewer instanceof AbstractProjectViewer)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) viewer;

            Set<AchievementRule> projRules = man.getPerProjectRules ();

            Set<AchievementRule> achieved = man.getProjectAchievedRules (pv);

            Set<String> projAchievedIds = new HashSet<> ();

            if (achieved != null)
            {

                projAchievedIds = achieved.stream ()
                    .map (r -> r.getId ())
                    .collect (Collectors.toSet ());

            }

            VBox projItems = new VBox ();

            for (AchievementRule r : projRules)
            {

                projItems.getChildren ().add (new AchievementView (r,
                                                                   projAchievedIds.contains (r.getId ()),
                                                                   this.getBinder ()));

            }

            StringProperty ptitleProp = new SimpleStringProperty ();
            ptitleProp.bind (Bindings.createStringBinding (() ->
            {

                Set<AchievementRule> ids = man.getProjectAchievedRules (pv);

                return String.format (getUIString (achievementspanel,sectiontitles,project),
                                      Environment.formatNumber ((ids != null ? ids.size () : 0)),
                                      Environment.formatNumber (projRules.size ()));

            },
            man.projectAchievedProperty (pv),
            UILanguageStringsManager.uilangProperty ()));

            AccordionItem proj = AccordionItem.builder ()
                .styleClassName (StyleClassNames.PROJECT)
                .title (ptitleProp)
                .openContent (projItems)
                .build ();

            all.getChildren ().add (proj);

        }

/*
      this.headers.put ("user",
                        gen.getHeader ());

      main.add (gen);

      if (this.viewer instanceof AbstractProjectViewer)
      {

          final JComponent projBox = _this.getAchievementsBox (projRules,
                                                               achieved.get ("project"),
                                                               "project");

          gen.setBorder (UIUtils.createPadding (0, 5, 0, 0));
          AccordionItem proj = new AccordionItem (String.format (getUIString (achievementspanel,sectiontitles,project),
                                                                 Environment.formatNumber (achieved.get ("project").size ()),
                                                                 Environment.formatNumber (projRules.size ())))
                                                  //Environment.replaceObjectNames ("{Project} - " + achieved.get ("project").size () + " / " + projRules.size ()))
          {

              @Override
              public JComponent getContent ()
              {

                  return projBox;

              }

          };

          proj.init ();

          this.headers.put ("project",
                            proj.getHeader ());

          proj.setBorder (UIUtils.createPadding (0, 5, 0, 0));

          main.add (proj);

      }
*/
    }

    public void achievementReached (AchievementReachedEvent ev)
    {

        AchievementRule ar = ev.getRule ();

/*
        String t = ar.getCategory ();

        Header h = this.headers.get (t);

        if (h == null)
        {

            return;

        }

        AchievementsManager man = Environment.getAchievementsManager ();

        Map<String, Set<String>> achieved = man.getAchievedAchievementIds (this.viewer);

        Box b = this.boxes.get (t);

        if (t.equals ("user"))
        {

            Set<AchievementRule> userRules = man.getUserRules ();

            h.setTitle (String.format (getUIString (achievementspanel,sectiontitles,user),
                                       Environment.formatNumber (achieved.get (t).size ()),
                                       Environment.formatNumber (userRules.size ())));
                        //"General - " + achieved.get (t).size () + " / " + userRules.size ());

            for (int i = 0; i < b.getComponentCount (); i++)
            {

                Component c = b.getComponent (i);

                if (c instanceof AchievementBox)
                {

                    AchievementBox ab = (AchievementBox) c;

                    if (ab.getRule ().getId ().equals (ar.getId ()))
                    {

                        ab.setAchieved (true);

                        ab.setVisible (true);

                        b.remove (ab);

                        b.add (ab,
                               0);

                    }

                }

            }

        }

        if (t.equals ("project"))
        {

            Set<AchievementRule> projRules = man.getPerProjectRules ();

            h.setTitle (String.format (getUIString (achievementspanel,sectiontitles,project),
                                       Environment.formatNumber (achieved.get (t).size ()),
                                       Environment.formatNumber (projRules.size ())));
            //"Project - " + achieved.get (t).size () + " / " + projRules.size ());

            for (int i = 0; i < b.getComponentCount (); i++)
            {

                Component c = b.getComponent (i);

                if (c instanceof AchievementBox)
                {

                    AchievementBox ab = (AchievementBox) c;

                    if (ab.getRule ().getId ().equals (ar.getId ()))
                    {

                        ab.setAchieved (true);

                        ab.setVisible (true);

                        b.remove (ab);

                        b.add (ab,
                               0);

                    }

                }

            }

        }
*/
    }

    @Override
    public Panel createPanel ()
    {

        Panel panel = Panel.builder ()
            .title (achievementspanel,title)
            .content (this)
            .styleClassName (StyleClassNames.ACHIEVEMENTS)
            .styleSheet (StyleClassNames.ACHIEVEMENTS)
            .panelId (PANEL_ID)
            .build ();
        panel.setId (StyleClassNames.ACHIEVEMENTS);
        return panel;

    }

}
