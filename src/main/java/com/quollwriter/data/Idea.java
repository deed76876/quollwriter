package com.quollwriter.data;

import java.util.*;

import org.dom4j.*;

public class Idea extends NamedObject
{

    public static final String OBJECT_TYPE = "idea";

    private IdeaType type = null;
    private int      rating = -1;

    public Idea()
    {

        super (Idea.OBJECT_TYPE);

    }

    public String getName ()
    {

        return this.getDescriptionText ();

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "type",
                                    this.type);

    }

    public Date getLastModified ()
    {

        Date d = super.getLastModified ();

        if (d != null)
        {

            return d;

        }

        return this.getDateCreated ();

    }

    public IdeaType getType ()
    {

        return this.type;

    }

    public void setType (IdeaType t)
    {

        this.type = t;

        this.setParent (t);

    }

    public int getRating ()
    {

        return this.rating;

    }

    public void setRating (int r)
    {

        this.rating = r;

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet ();

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }


}
