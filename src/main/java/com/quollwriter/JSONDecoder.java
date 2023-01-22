package com.quollwriter;

import java.lang.reflect.*;
import java.math.*;

import java.text.*;

import java.util.*;


public class JSONDecoder
{

    private static final Object OBJECT_END = new Object ();
    private static final Object ARRAY_END = new Object ();
    private static final Object COLON = new Object ();
    private static final Object COMMA = new Object ();
    public static final int     FIRST = 0;
    public static final int     CURRENT = 1;
    public static final int     NEXT = 2;

    private static Map escapes = new HashMap ();

    static
    {
        escapes.put (Character.valueOf ('"'),
                     Character.valueOf ('"'));
        escapes.put (Character.valueOf ('\\'),
                     Character.valueOf ('\\'));
        escapes.put (Character.valueOf ('/'),
                     Character.valueOf ('/'));
        escapes.put (Character.valueOf ('b'),
                     Character.valueOf ('\b'));
        escapes.put (Character.valueOf ('f'),
                     Character.valueOf ('\f'));
        escapes.put (Character.valueOf ('n'),
                     Character.valueOf ('\n'));
        escapes.put (Character.valueOf ('r'),
                     Character.valueOf ('\r'));
        escapes.put (Character.valueOf ('t'),
                     Character.valueOf ('\t'));
    }

    private CharacterIterator it;
    private char              c;
    private Object            token;
    private StringBuffer      buf = new StringBuffer ();

    public static StringWithMarkup decodeToStringWithMarkup (Object v)
    {

        if (v == null)
        {

            return null;

        }

        String s = v.toString ();

        if (s.trim ().length () == 0)
        {

            return null;

        }

        if (s.equals ("null"))
        {

            return null;

        }

        Map m = (Map) JSONDecoder.decode (s);
        String markup = (String) m.get ("markup");
        String text = (String) m.get ("text");

        return new StringWithMarkup (text,
                                     markup);

    }

    public static Object decode (String v)
    {

        if (v == null)
        {

            return null;

        }

        if (v.startsWith (Constants.JSON_RETURN_PREFIX))
        {

            v = v.substring (Constants.JSON_RETURN_PREFIX.length ());

        }

        return new JSONDecoder ().read (v);

    }

    private char next ()
    {
        c = it.next ();

        return c;
    }

    private void skipWhiteSpace ()
    {

        while (Character.isWhitespace (c))
        {
            next ();
        }
    }

    private Object read (CharacterIterator ci,
                         int               start)
    {

        it = ci;

        switch (start)
        {

        case FIRST:
            c = it.first ();

            break;

        case CURRENT:
            c = it.current ();

            break;

        case NEXT:
            c = it.next ();

            break;

        }

        return this.read ();

    }

    public Object read (String string)
    {

        return this.read (new StringCharacterIterator (string),
                          FIRST);

    }

    private Object read ()
    {

        this.skipWhiteSpace ();

        char ch = c;
        this.next ();

        switch (ch)
        {

        case '"':
            token = string ();

            break;

        case '[':
            token = array ();

            break;

        case ']':
            token = ARRAY_END;

            break;

        case ',':
            token = COMMA;

            break;

        case '{':
            token = object ();

            break;

        case '}':
            token = OBJECT_END;

            break;

        case ':':
            token = COLON;

            break;

        case 'N' :
            next ();
            next ();
            token = Double.valueOf (Double.NaN);
            break;

        case 't':
            next ();
            next ();
            next (); // assumed r-u-e
            token = Boolean.TRUE;

            break;

        case 'f':
            next ();
            next ();
            next ();
            next (); // assumed a-l-s-e
            token = Boolean.FALSE;

            break;

        case 'n':
            next ();
            next ();
            next (); // assumed u-l-l
            token = null;

            break;

        default:
            c = it.previous ();

            if (Character.isDigit (c) || (c == '-'))
            {
                token = number ();
            }
        }

        return token;

    }

    private Object object ()
    {
        Map    ret = new LinkedHashMap ();
        Object key = read ();

        while (token != OBJECT_END)
        {

            read (); // should be a colon

            if (token != OBJECT_END)
            {
                Object val = read ();

                ret.put (key,
                         val);

                if (read () == COMMA)
                {
                    key = read ();
                }
            }
        }

        return ret;
    }

    private Object array ()
    {
        List   ret = new ArrayList ();
        Object value = read ();

        while (token != ARRAY_END)
        {
            ret.add (value);

            if (read () == COMMA)
            {
                value = read ();
            }
        }

        return ret;
    }

    private Object number ()
    {
        int     length = 0;
        boolean isFloatingPoint = false;
        buf.setLength (0);

        if (c == '-')
        {
            add ();
        }

        length += addDigits ();

        if (c == '.')
        {
            add ();
            length += addDigits ();
            isFloatingPoint = true;
        }

        if ((c == 'e') || (c == 'E'))
        {
            add ();

            if ((c == '+') || (c == '-'))
            {
                add ();
            }

            addDigits ();
            isFloatingPoint = true;
        }

        String s = buf.toString ();

        return isFloatingPoint ? ((length < 17) ? (Object) Double.valueOf (s) : new BigDecimal (s)) : ((length < 19) ? (Object) Long.valueOf (s) : new BigInteger (s));
    }

    private int addDigits ()
    {
        int ret;

        for (ret = 0; Character.isDigit (c); ++ret)
        {
            add ();
        }

        return ret;
    }

    private Object string ()
    {
        buf.setLength (0);

        while (c != '"')
        {

            if (c == '\\')
            {
                next ();

                if (c == 'u')
                {
                    add (unicode ());
                } else
                {
                    Object value = escapes.get (Character.valueOf (c));

                    if (value != null)
                    {
                        add (((Character) value).charValue ());
                    }
                }
            } else
            {
                add ();
            }
        }

        next ();

        String s = buf.toString ();

        if (s.startsWith ("@["))
        {

            int ind = s.indexOf ("]",
                                 2);

            if (ind > 2)
            {

                try
                {

                    String cn = s.substring (2,
                                             ind);

                    Class cl = Class.forName (cn);

                    Constructor con = cl.getDeclaredConstructor (String.class);

                    if (con == null)
                    {

                        throw new IllegalArgumentException ("Expected class: " + cn + " to have a constructor with a single String arg.");

                    }

                    Object o = con.newInstance (s.substring (ind + 1));

                    return o;

                } catch (Exception e) {

                    throw new IllegalArgumentException ("Unable to create object and init from: " + s,
                                                        e);

                }

            }

        }

        return s;

    }

    private void add (char cc)
    {
        buf.append (cc);
        next ();
    }

    private void add ()
    {
        add (c);
    }

    private char unicode ()
    {
        int value = 0;

        for (int i = 0; i < 4; ++i)
        {

            switch (next ())
            {

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                value = (value << 4) + c - '0';

                break;

            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                value = (value << 4) + c - 'k';

                break;

            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                value = (value << 4) + c - 'K';

                break;
            }
        }

        return (char) value;
    }
}
