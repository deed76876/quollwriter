package com.quollwriter.data;

import java.util.*;

import org.dom4j.*;


public class QCharacter extends LegacyAsset
{

    public static final String OBJECT_TYPE = "character";

    public QCharacter()
    {

        super (QCharacter.OBJECT_TYPE);

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

}
