package com.quollwriter.data;

import java.io.*;
import java.nio.file.*;

import java.util.*;

import com.quollwriter.*;

import org.dom4j.*;


public class Prompts
{

    private static Map<String, String> excludes = new HashMap<> ();
    private static List<String>        promptIds = new ArrayList<> ();
    private static int                 pos = 0;

    public class XMLConstants
    {

        public static final String root = "prompts";

    }

    private static Path getExcludesFilePath ()
    {

        return Environment.getUserPath (Constants.PROMPTS_EXCLUDE_FILE);

    }

    private static Path getUserPromptsDirPath ()
    {

        return Environment.getUserPath (Constants.USER_PROMPTS_DIR);

    }

    private static Path getUserPromptFilePath (Prompt p)
    {

        return Prompts.getUserPromptFilePath (p.getId ());

    }

    private static Path getUserPromptFilePath (String id)
    {

        return Prompts.getUserPromptsDirPath ().resolve (id + ".txt");

    }

    public static void init ()
                      throws Exception
    {

        // Get the exclude list.
        // TODO Change to use paths.
        File f = Prompts.getExcludesFilePath ().toFile ();

        if (f.exists ())
        {

            BufferedReader b = new BufferedReader (new FileReader (f));

            try
            {

                String l = b.readLine ();

                while (l != null)
                {

                    Prompts.excludes.put (l.trim (),
                                          "");

                    l = b.readLine ();

                }

            } finally
            {

                b.close ();

            }

        }

        // Get the default prompts file.
        BufferedReader b = new BufferedReader (new InputStreamReader (Utils.getResourceStream (Constants.DEFAULT_PROMPT_IDS_FILE)));

        try
        {

            String l = null;

            while ((l = b.readLine ()) != null)
            {

                String id = l.trim ();

                if (Prompts.excludes.containsKey (id))
                {

                    continue;

                }

                Prompts.promptIds.add (id);

            }

        } finally
        {

            b.close ();

        }

        // Load the user prompts.
        Path p = Prompts.getUserPromptsDirPath ();

        if ((Files.exists (p))
            &&
            (Files.isDirectory (p))
           )
        {

            // TODO Use a walk/stream.
            File[] files = p.toFile ().listFiles ();

            if (files != null)
            {

                for (int i = 0; i < files.length; i++)
                {

                    if ((files[i].isFile ()) &&
                        (files[i].getName ().endsWith (".txt")))
                    {

                        String id = files[i].getName ().substring (0,
                                                                   files[i].getName ().length () - 4);

                        if (Prompts.excludes.containsKey (id))
                        {

                            continue;

                        }

                        // This is one, use the name as the id.
                        Prompts.promptIds.add (id);

                    }

                }

            }

        }

    }

    public static void shuffle ()
    {

        Collections.shuffle (Prompts.promptIds);

    }

    public static Prompt getPromptById (String id)
                                 throws Exception
    {

        if (id == null)
        {

            return null;

        }

        Element root = null;

        if (Prompt.isUserPrompt (id))
        {

            Path f = Prompts.getUserPromptFilePath (id);

            root = DOM4JUtils.fileAsElement (f);

        } else
        {

            String xml = Utils.getResourceFileAsString (Constants.PROMPTS_DIR + id + ".txt");

            if (xml == null)
            {

                Environment.logError ("Unable to get prompt from resource file: " +
                                      Constants.PROMPTS_DIR + id + ".txt");

                return null;

            }

            root = DOM4JUtils.stringAsElement (xml);

        }

        Prompt p = new Prompt (root);

        return p;

    }

    public static Prompt addUserPrompt (String text)
                                 throws Exception
    {

        // Add a new one.
        Prompt p = new Prompt (text);

        // Get the prompts file.
        Path f = Prompts.getUserPromptFilePath (p);

        Files.createDirectories (f.getParent ());

        if (Files.exists (f))
        {

            throw new GeneralException ("A user prompt with id: " +
                                        p.getId () +
                                        " already exists at: " +
                                        f);

        }

        DOM4JUtils.writeToFile (p.getAsElement (),
                                f,
                                true);

        return p;

    }

    public static Prompt previous ()
    {

        try
        {

            Prompts.pos--;

            if (Prompts.pos < 0)
            {

                Prompts.pos = Prompts.promptIds.size () - 1;

            }

            return Prompts.current ();

        } catch (Exception e)
        {

            return null;

        }

    }

    public static Prompt next ()
    {

        try
        {

            Prompts.pos++;

            if (Prompts.pos >= Prompts.promptIds.size ())
            {

                Prompts.pos = 0;

            }

            return Prompts.current ();

        } catch (Exception e)
        {
            Environment.logError ("Unable to get prompt: " + Prompts.getCurrentId (),
                                  e);

            return null;

        }

    }

    public static Prompt current ()
                           throws Exception
    {

        String id = Prompts.getCurrentId ();

        return Prompts.getPromptById (id);

    }

    public static String getCurrentId ()
    {

        return Prompts.promptIds.get (Prompts.pos);

    }

    public static void excludeCurrent ()
    {

        Prompts.excludes.put (Prompts.getCurrentId (),
                              "");

        Prompts.promptIds.remove (Prompts.getCurrentId ());

        // Write out the exclude list.
        // TODO Change to use a path.
        File f = Prompts.getExcludesFilePath ().toFile ();

        try
        {

            PrintWriter pw = new PrintWriter (new FileWriter (f));

            Iterator iter = Prompts.excludes.keySet ().iterator ();

            while (iter.hasNext ())
            {

                String id = (String) iter.next ();

                pw.println (id);

            }

            pw.flush ();
            pw.close ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save prompts exclude file to: " +
                                  f,
                                  e);

        }

    }

    public static List<PromptWebsite> getPromptWebsites ()
    {

        List<PromptWebsite> ws = new ArrayList<> ();

        try
        {

            String xml = Utils.getResourceFileAsString (Constants.PROMPT_WEBSITES_FILE);

            Element root = DOM4JUtils.stringAsElement (xml);

            for (Element el : root.elements (PromptWebsite.XMLConstants.root))
            {

                try
                {

                    PromptWebsite w = new PromptWebsite (el);

                    ws.add (w);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to load prompt website: " +
                                          DOM4JUtils.getPath (el),
                                          e);

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to load prompt websites",
                                  e);

        }

        return ws;

    }
}
