package com.quollwriter.achievements.rules;

import java.util.*;

import javafx.beans.property.*;

import org.dom4j.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public abstract class AbstractAchievementRule implements AchievementRule
{

    public class XMLConstants
    {

        public static final String id = "id";
        public static final String category = "category";
        //public static final String name = "name";
        public static final String icon = "icon";
        //public static final String description = "description";
        public static final String event = "event";
        public static final String hidden = "hidden";

    }

    private String id = null;
    private String desc = null;
    private String name = null;
    private String icon = null;
    private String cat = null;
    private boolean hidden = false;
    private StringProperty nameProp = null;
    private StringProperty descProp = null;

    protected Set<String> eventIds = new HashSet<> ();

    public String toString ()
    {

        return this.id + "[" + this.name + ", event ids: " + this.eventIds + ", hidden: " + this.hidden + "]";

    }

    public AbstractAchievementRule (Element root)
                                    throws  GeneralException
    {

        this.id = DOM4JUtils.attributeValue (root,
                                             XMLConstants.id);
        this.cat = DOM4JUtils.attributeValue (root,
                                                XMLConstants.category);
        this.hidden = DOM4JUtils.attributeValueAsBoolean (root,
                                                            XMLConstants.hidden,
                                                            false);

        // Get the name from the language string.
        this.nameProp = getUILanguageStringProperty (LanguageStrings.achievements,this.id,LanguageStrings.name);

/*
        if (this.name == null)
        {

            throw new JDOMException ("Unable to find name string for achievement: " +
                                     this.id);

        }
*/
        this.descProp = getUILanguageStringProperty (achievements,this.id,description);

/*
        if (this.desc == null)
        {

            throw new JDOMException ("Unable to find description string for achievement: " +
                                     this.id);

        }
*/
        this.icon = DOM4JUtils.attributeValue (root,
                                                 XMLConstants.icon,
                                                 false);

        String ev = DOM4JUtils.attributeValue (root,
                                                 XMLConstants.event,
                                                 false);

        if (ev != null)
        {

            StringTokenizer t = new StringTokenizer (ev,
                                                     ":;");

            while (t.hasMoreTokens ())
            {

                String part = t.nextToken ();

                // Split on .
                StringTokenizer tt = new StringTokenizer (part,
                                                          ".");

                String type = tt.nextToken ();

                if (tt.hasMoreTokens ())
                {

                    StringTokenizer ttt = new StringTokenizer (tt.nextToken (),
                                                               ",");

                    while (ttt.hasMoreTokens ())
                    {

                        this.eventIds.add (type + "." + ttt.nextToken ().toLowerCase ());

                    }

                } else {

                    this.eventIds.add (type + "." + ProjectEvent.Type.any);

                }

            }

        }

    }

    public boolean isHidden ()
    {

        return this.hidden;

    }

    public Set<String> getEventIds ()
    {

        return this.eventIds;

    }

    public boolean isEventTrigger ()
    {

        return this.eventIds.size () > 0;

    }

    public String getCategory ()
    {

        return this.cat;

    }
/*
TODO Remove
    public String getDescription ()
    {

        return this.desc;

    }

    public String getName ()
    {

        return this.name;

    }
*/

    public StringProperty descriptionProperty ()
    {

        return this.descProp;

    }

    public StringProperty nameProperty ()
    {

        return this.nameProp;

    }

    public String getIcon ()
    {

        return this.icon;

    }

    public String getId ()
    {

        return this.id;

    }

    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
                             throws                Exception
    {

        return false;

    }

    public boolean achieved (ProjectEvent ev)
                             throws       Exception
    {

        return false;

    }

    public boolean achieved (AbstractProjectViewer viewer)
                             throws                Exception
    {

        return false;

    }

}
