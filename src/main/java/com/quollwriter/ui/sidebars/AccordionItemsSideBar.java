package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ScrollableBox;

public abstract class AccordionItemsSideBar<E extends AbstractViewer> extends AbstractSideBar<E>
{

    private List<AccordionItem> items = new ArrayList ();
    private JScrollPane scrollPane = null;
    private Box contentBox = null;

    public AccordionItemsSideBar (E                   v,
                                  List<AccordionItem> items)
    {

        super (v);

        this.items = items;

        this.contentBox = new ScrollableBox (BoxLayout.Y_AXIS)
        {

            public Dimension getMinimumSize ()
            {

                return this.getPreferredSize ();

            }

        };

    }

    public void scrollVerticalTo (final int v)
    {

        final AccordionItemsSideBar _this = this;

        SwingUtilities.invokeLater (new Runnable ()
        {

            public void run ()
            {

                _this.scrollPane.getVerticalScrollBar ().setValue (v);

            }

        });

    }

    @Override
    public abstract String getId ();

    @Override
    public void onShow ()
    {

    }

    @Override
    public void onHide ()
    {

    }

    public void onClose ()
    {


    }

    public boolean removeOnClose ()
    {

        return true;

    }

    public String getTitle ()
    {

        return null;

    }

    public String getIconType ()
    {

        return null;

    }

    @Override
    public List<JComponent> getHeaderControls ()
    {

        return null;

    }

    public boolean canClose ()
    {

        return true;

    }

    public JComponent getContent ()
    {

        Box b = this.contentBox;

        b.setOpaque (false);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        if (this.items == null)
        {

            this.items = this.getItems ();

        }

        this.fillContent ();

        b.setMinimumSize (new Dimension (300,
                                         250));
        /*
        b.setPreferredSize (new Dimension (255,
                                           250));
*/
        this.scrollPane = this.wrapInScrollPane (b);

        return this.scrollPane;

    }

    public JScrollPane getScrollPane ()
    {

        return this.scrollPane;

    }

    private void fillContent ()
    {

        this.contentBox.removeAll ();

        if (this.items != null)
        {

            for (AccordionItem it : this.items)
            {

                it.init ();
                this.contentBox.add (it);

            }

        }

        this.contentBox.add (Box.createVerticalGlue ());

    }

    public void setItems (List<AccordionItem> items)
    {

        this.items = items;
        this.fillContent ();
        this.validate ();
        this.repaint ();

    }

    public List<AccordionItem> getItems ()
    {

        return this.items;

    }

    public void panelShown (MainPanelEvent ev)
    {

    }

    public String getSaveState ()
    {

        return null;

    }

    @Override
    public void init (String saveState)
               throws GeneralException
    {

        super.init (saveState);

        if (this.items != null)
        {

            for (AccordionItem it : this.items)
            {

                it.init ();

            }

        }
        /*
        this.items.get (this.items.size () - 1).setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                               Short.MAX_VALUE));
        */
        this.scrollVerticalTo (0);

    }

}
