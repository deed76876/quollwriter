package com.quollwriter.ui;

import java.awt.Dimension;
import java.awt.Component;

import java.io.File;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.events.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class FileFinder extends Box
{

    private JTextField text = null;
    private JFileChooser chooser = null;
    private File file = null;
    private ActionListener onSelect = null;
    private ActionListener onCancel = null;
    private JButton cancelButton = null;
    private JButton findButton = null;
    private FileFilter fileFilter = null;

    private String finderTitle = null;
    private int finderSelectionMode = -1;
    private String approveButtonText = null;
    private String findButtonToolTip = null;
    private boolean allowUserEntry = false;
    private boolean clearOnCancel = false;
    private boolean showCancel = false;

    public FileFinder ()
    {

        super (BoxLayout.X_AXIS);

        this.text = UIUtils.createTextField ();

        this.text.setToolTipText (getUIString (filefinder, LanguageStrings.text,tooltip));

        this.findButton = UIUtils.createButton (Constants.FIND_ICON_NAME,
                                                Constants.ICON_MENU,
                                                getUIString (filefinder,find,tooltip),
                                                //"Click to find the file/directory.")),
                                                null);

        this.cancelButton = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                                //"cancel",
                                                  Constants.ICON_MENU,
                                                  getUIString (filefinder,cancel,tooltip),
                                                  //"Click to cancel.",
                                                  null);

        this.chooser = new JFileChooser ();

    }

    @Override
    public void setEnabled (boolean v)
    {

        this.text.setEnabled (v);
        this.findButton.setEnabled (v);
        this.cancelButton.setEnabled (v);

    }

    public void setClearOnCancel (boolean v)
    {

        this.clearOnCancel = v;

    }

    public void setFileFilter (FileFilter f)
    {

        this.fileFilter = f;

    }

    public void setAllowUserEntry (boolean v)
    {

        this.allowUserEntry = v;

    }

    public void showCancel (boolean        v,
                            ActionListener onCancel)
    {

        this.showCancel = true;

        this.onCancel = onCancel;

    }

    public void setFindButtonToolTip (String v)
    {

        this.findButtonToolTip = v;

        this.findButton.setToolTipText (v);

    }

    public void setOnSelectHandler (ActionListener l)
    {

        this.onSelect = l;

    }

    public void setApproveButtonText (String v)
    {

        this.approveButtonText = v;

        this.chooser.setApproveButtonText (this.approveButtonText);

    }

    public void setFinderSelectionMode (int v)
    {

        this.finderSelectionMode = v;

        this.chooser.setFileSelectionMode (this.finderSelectionMode);

    }
/*
    public void setFileChooser (JFileChooser f)
    {

        this.chooser = f;

    }
*/
    public void setFile (File f)
    {

        this.file = f;

        if (this.text != null)
        {

            if (f != null)
            {

                this.text.setText (f.getPath ());

                if (this.text.getText ().length () > 0)
                {

                    this.text.setCaretPosition (this.text.getText ().length () - 1);

                }

            }

        }

        if (this.chooser != null)
        {

            this.chooser.setSelectedFile (f);

        }

    }

    public void setFinderTitle (String t)
    {

        this.finderTitle = t;

        this.chooser.setDialogTitle (this.finderTitle);

    }

    public void init ()
    {

        final FileFinder _this = this;

        if (this.file != null)
        {

            this.text.setText (this.file.getPath ());

            if (this.text.getText ().length () > 0)
            {

                this.text.setCaretPosition (this.text.getText ().length () - 1);

            }

        }

        this.text.setEditable (this.allowUserEntry);
        this.text.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.chooser.setDialogTitle (this.finderTitle);
        this.chooser.setFileSelectionMode (this.finderSelectionMode);
        this.chooser.setApproveButtonText (this.approveButtonText);
        //this.chooser.setCurrentDirectory (new File (this.text.getText ()));

        final ActionListener aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                // Need to run: attrib -r "%USERPROFILE%\My Documents" on XP to allow a new directory
                // to be created in My Documents.

                if (_this.fileFilter != null)
                {

                    _this.chooser.setFileFilter (_this.fileFilter);

                }

                if (_this.chooser.showOpenDialog (text) == JFileChooser.APPROVE_OPTION)
                {

                    if (_this.onSelect != null)
                    {

                        VetoableActionEvent ac = new VetoableActionEvent (_this,
                                                                          0,
                                                                          "fileSelected");

                        _this.onSelect.actionPerformed (ac);

                        if (ac.isCancelled ())
                        {

                            return;

                        }

                    }

                    _this.text.setText (_this.chooser.getSelectedFile ().getPath ());
                    _this.text.setCaretPosition (_this.text.getText ().length () - 1);

                }

            }

        };

        this.findButton.addActionListener (aa);

        this.text.addMouseListener (new MouseAdapter ()
        {

            public void mousePressed (MouseEvent ev)
            {

                if (!_this.findButton.isEnabled ())
                {

                    return;

                }

                aa.actionPerformed (new ActionEvent (text,
                                                     0,
                                                     "mousepressed"));

            }

        });

        if (this.showCancel)
        {

            if (this.onCancel != null)
            {

                this.cancelButton.addActionListener (this.onCancel);

                if (this.clearOnCancel)
                {

                    this.cancelButton.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.chooser.setSelectedFile (null);
                            _this.text.setText ("");

                        }

                    });

                }

            }

        }

        FormLayout fl = new FormLayout ("fill:50px:grow, 2px, p" + (this.showCancel ? ", 2px, p" : ""),
                                        "p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.add (this.text,
                     cc.xy (1,
                            1));

        builder.add (this.findButton,
                     cc.xy (3,
                            1));

        if (this.showCancel)
        {

            builder.add (this.cancelButton,
                         cc.xy (5,
                                1));

        }

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        this.setOpaque (false);
        this.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        p.setAlignmentY (JComponent.TOP_ALIGNMENT);
        this.add (p);

    }

    public File getSelectedFile ()
    {

        if (this.chooser == null)
        {

            return this.file;

        }

        File f = this.chooser.getSelectedFile ();

        if (f != null)
        {

            return f;

        }

        return this.file;

    }

}
