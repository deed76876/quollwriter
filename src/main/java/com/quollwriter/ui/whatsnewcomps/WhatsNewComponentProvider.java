package com.quollwriter.ui.whatsnewcomps;

import javax.swing.*;

import com.quollwriter.ui.*;

public interface WhatsNewComponentProvider
{
    
    public JComponent getComponent (AbstractViewer pv,
                                    String         id);    
    
}