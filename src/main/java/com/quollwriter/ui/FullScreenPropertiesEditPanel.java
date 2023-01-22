package com.quollwriter.ui;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.Component;
import java.awt.Image;
import java.awt.RenderingHints;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.*;

import java.awt.event.*;

import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.TextProperties;
import com.quollwriter.ui.components.Accordion;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class FullScreenPropertiesEditPanel extends Box implements UserPropertyListener
{

    private FullScreenFrame fsf = null;
    private int                  sizeBoxMaxWidth = 150;
    private ImagePanel           sizeBox = null;
    private JComponent           sizeBoxWrapper = null;
    private Image                transparentImage = null;

    private JSlider              backgroundOpacity = null;
    private JCheckBox            hideBorder = null;

    private int                  sblastX = -1;
    private int                  sblastY = -1;

    public FullScreenPropertiesEditPanel ()
    {

        super (BoxLayout.Y_AXIS);

        this.add (this.createPropertiesPanel ());

    }

    public void setBackgroundColor (Color c)
    {

        this.sizeBoxWrapper.setBackground (c);

    }

    public void setFullScreenFrame (FullScreenFrame fsf)
    {

        this.fsf = fsf;

        this.backgroundOpacityChanged ();

        this.displayAreaSizeChanged ();

    }

    @Override
    public void propertyChanged (UserPropertyEvent ev)
    {

        this.backgroundOpacityChanged ();

        this.displayAreaSizeChanged ();

    }


    private JComponent createPropertiesPanel ()
    {

        final FullScreenPropertiesEditPanel _this = this;

        Accordion acc = new Accordion (BoxLayout.Y_AXIS);

        // Create a box that will be the container for the chapters/properties.
        Box b = new Box (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setBorder (UIUtils.createPadding (5, 0, 0, 0));

        JLabel l = UIUtils.createLabel (getUIString (project,sidebar,fullscreenproperties,selectbackground,text));
        //"Background image/color");
        l.setBorder (UIUtils.createPadding (5, 10, 0, 0));

        l.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        b.add (l);
        b.add (Box.createVerticalStrut (10));

        final Box imgP = new Box (BoxLayout.Y_AXIS)
        {

            private BufferedImage imgThumb = null;
            private BackgroundImage bgImg = null;

            private Map<Object, Paint> thumbs = new HashMap ();

            public void paintComponent (Graphics g)
            {

                Graphics2D g2d = (Graphics2D) g;

                Paint p = null;

                Object bg = (_this.fsf != null ? _this.fsf.getBackgroundObject () : null);

                if (bg != null)
                {

                    if (bg instanceof BackgroundImage)
                    {

                        BackgroundImage bi = (BackgroundImage) bg;

                        p = this.thumbs.get (bi);

                        if (p == null)
                        {

                            BufferedImage im = (BufferedImage) _this.createImage (75,
                                                                                  75);

                            Graphics gr = im.getGraphics ();
                            gr.drawImage (bi.getThumb (),
                                          0,
                                          0,
                                          Color.black,
                                          this);

                            p = new TexturePaint (im,
                                                  new Rectangle (0,
                                                                 0,
                                                                 75,
                                                                 75));

                            this.thumbs.put (bi,
                                             p);

                        }

                    }

                    if (bg instanceof File)
                    {

                        File f = (File) bg;

                        p = this.thumbs.get (bg);

                        if (p == null)
                        {

                            p = new TexturePaint (UIUtils.getScaledImage (f,
                                                                          75,
                                                                          -1),
                                                  new Rectangle (0,
                                                                 0,
                                                                 75,
                                                                 75));

                            this.thumbs.put (bg,
                                             p);

                        }

                    }

                    if (bg instanceof Color)
                    {

                        p = (Color) bg;

                    }

                }

                if (p != null)
                {

                    g2d.setPaint (p);
                    g2d.fill (g2d.getClip ());

                }

            }

        };

        imgP.setOpaque (true);
        imgP.setBackground (UIManager.getColor ("Control"));
        imgP.setBorder (UIUtils.createLineBorder ());
        imgP.setPreferredSize (new Dimension (75, 75));
        imgP.setMaximumSize (new Dimension (75, 75));
        imgP.setToolTipText (getUIString (project,sidebar,fullscreenproperties,selectbackground,tooltip));
        //"Click to set the image/color used for the background");
        UIUtils.setAsButton (imgP);

        imgP.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                Point p = ev.getPoint ();
                SwingUtilities.convertPointToScreen (p,
                                                     imgP);

                _this.fsf.showBackgroundSelector (p);

            }

        });

        Box imgB = new Box (BoxLayout.X_AXIS);
        imgB.add (Box.createHorizontalStrut (20));
        imgB.add (imgP);
        imgB.add (Box.createHorizontalGlue ());
        imgB.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        imgB.setMaximumSize (imgB.getPreferredSize ());
        b.add (imgB);

        b.add (Box.createVerticalStrut (5));

        l = UIUtils.createClickableLabel (getUIString (project,sidebar,fullscreenproperties,bgimagewebsites,text),
        //"List of background image websites",
                                          Environment.getIcon ("bg-select",
                                                               Constants.ICON_MENU));

        l.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                // Open the url.
                UIUtils.openURL (_this,
                                 Constants.QUOLLWRITER_PROTOCOL + ":resources.html#bgimages");

            }

        });

        l.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        l.setBorder (new EmptyBorder (5, 20, 0, 0));

        b.add (l);

        b.add (Box.createVerticalStrut (10));

        l = new JLabel (getUIString (project,sidebar,fullscreenproperties,bgopacity,text));
        //"Background darkness");
        l.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        l.setBorder (new EmptyBorder (5, 10, 0, 0));

        b.add (l);

        //b.add (Box.createVerticalStrut (10));

        this.backgroundOpacity = new JSlider (0,
                                              100);
        this.backgroundOpacity.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        this.backgroundOpacity.setToolTipText (getUIString (project,sidebar,fullscreenproperties,bgopacity,tooltip));
        //"Drag to change the background darkness");
        this.backgroundOpacity.addChangeListener (new ChangeAdapter ()
        {

            public void stateChanged (ChangeEvent ev)
            {

                _this.fsf.setBorderOpacity ((float) (_this.backgroundOpacity.getValue () / 100f));

            }

        });

        this.backgroundOpacity.setOpaque (false);
        this.backgroundOpacity.setMaximumSize (new Dimension (150, 20));
        this.backgroundOpacity.setBorder (new EmptyBorder (5, 20, 0, 0));

        b.add (this.backgroundOpacity);

        b.add (Box.createVerticalStrut (10));

        l = new JLabel (getUIString (project,sidebar,fullscreenproperties,areasize,text));
        //"Area size");
        l.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        l.setBorder (new EmptyBorder (5, 10, 0, 0));

        b.add (l);

        b.add (Box.createVerticalStrut (10));

        this.transparentImage = Environment.getTransparentImage ();

        this.sizeBox = new ImagePanel (this.transparentImage,
                                       null);

        //this.setSizeBoxSize ();
        this.sizeBox.setToolTipText (getUIString (project,sidebar,fullscreenproperties,areasize,tooltip));
        //"Drag the sides of the box to change the display area size.  Double click to maximize or minimize.");
        this.sizeBox.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        this.sizeBoxWrapper = new Box (BoxLayout.X_AXIS);
        this.sizeBoxWrapper.setOpaque (true);
        //this.sizeBoxWrapper.setBackground (this.fullScreenTextProperties.getBackgroundColor ());
        this.sizeBoxWrapper.setBorder (UIUtils.createLineBorder ());
        this.sizeBoxWrapper.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        this.sizeBoxWrapper.add (this.sizeBox);

        MouseAdapter sizeBoxMouseAdapter = new MouseAdapter ()
        {

            public void mouseClicked (MouseEvent ev)
            {

                if (ev.getClickCount () == 2)
                {

                    _this.fsf.toggleMaximizeDisplayArea ();

                    _this.setSizeBoxSize ();

                }

            }

            public void mouseReleased (MouseEvent ev)
            {

                _this.sblastX = -1;
                _this.sblastY = -1;

            }

            public void mouseMoved (MouseEvent ev)
            {

                Rectangle bounds = _this.sizeBox.getBounds ();

                int x = ev.getX ();
                int y = ev.getY ();

                if ((x >= (bounds.x)) &&
                    (x <= (bounds.x + 10)))
                {

                    _this.sizeBox.setCursor (Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR));

                    return;

                }

                if ((x >= (bounds.x + bounds.width - 10)) &&
                    (x <= (bounds.x + bounds.width)))
                {

                    _this.sizeBox.setCursor (Cursor.getPredefinedCursor (Cursor.E_RESIZE_CURSOR));

                    return;

                }

                if ((y >= (bounds.y)) &&
                    (y <= (bounds.y + 10)))
                {

                    _this.sizeBox.setCursor (Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR));

                    return;

                }

                if ((y >= (bounds.y + bounds.height - 10)) &&
                    (y <= (bounds.y + bounds.height)))
                {

                    _this.sizeBox.setCursor (Cursor.getPredefinedCursor (Cursor.S_RESIZE_CURSOR));

                    return;

                }

                _this.sizeBox.setCursor (Cursor.getDefaultCursor ());

            }

            public void mouseDragged (MouseEvent ev)
            {

                int x = ev.getX ();
                int y = ev.getY ();

                boolean left = false;
                boolean top = false;

                Rectangle bounds = _this.sizeBox.getBounds ();

                Cursor w = Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR);
                Cursor e = Cursor.getPredefinedCursor (Cursor.E_RESIZE_CURSOR);
                Cursor n = Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR);
                Cursor s = Cursor.getPredefinedCursor (Cursor.S_RESIZE_CURSOR);

                if ((x >= (bounds.x)) &&
                    (x <= (bounds.x + 10)))
                {

                    left = true;

                    _this.sizeBox.setCursor (w);

                }

                if ((x <= (bounds.x + bounds.width)) &&
                    (x >= (bounds.x + bounds.width - 10)))
                {

                    _this.sizeBox.setCursor (e);

                }

                if ((y >= (bounds.y)) &&
                    (y <= (bounds.y + 10)))
                {

                    top = true;

                    _this.sizeBox.setCursor (n);

                }

                if ((y <= (bounds.y + bounds.height)) &&
                    (y >= (bounds.y + bounds.height - 10)))
                {

                    _this.sizeBox.setCursor (s);

                }

                Cursor c = _this.sizeBox.getCursor ();

                if ((c == w) ||
                    (c == e))
                {

                    _this.sblastY = -1;

                    if (_this.sblastX == -1)
                    {

                        _this.sblastX = x;

                    } else
                    {

                        int diff = x - _this.sblastX;

                        if (diff < 0)
                        {

                            diff = 1;

                            if (_this.sizeBox.getCursor () == w)
                            {

                                diff = -1;

                            }

                        } else
                        {

                            diff = -1;

                            if (_this.sizeBox.getCursor () == Cursor.getPredefinedCursor (Cursor.W_RESIZE_CURSOR))
                            {

                                diff = 1;

                            }

                        }

                        // Make the border 1% wider.
                        _this.fsf.incrementXBorderWidth (diff * 0.01f);

                        _this.setSizeBoxSize ();

                    }

                }

                if ((c == n) ||
                    (c == s))
                {

                    _this.sblastX = -1;

                    if (_this.sblastY == -1)
                    {

                        _this.sblastY = y;

                    } else
                    {

                        int diff = y - _this.sblastY;

                        if (diff < 0)
                        {

                            diff = 1;

                            if (_this.sizeBox.getCursor () == Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR))
                            {

                                diff = -1;

                            }

                        } else
                        {

                            diff = -1;

                            if (_this.sizeBox.getCursor () == Cursor.getPredefinedCursor (Cursor.N_RESIZE_CURSOR))
                            {

                                diff = 1;

                            }

                        }

                        // Make the border 1% wider.
                        _this.fsf.incrementYBorderWidth (diff * 0.01f);

                        _this.setSizeBoxSize ();

                    }

                }

            }

        };

        this.sizeBox.addMouseMotionListener (sizeBoxMouseAdapter);
        this.sizeBox.addMouseListener (sizeBoxMouseAdapter);

        Box sizeBoxW2 = new Box (BoxLayout.X_AXIS);
        sizeBoxW2.add (Box.createHorizontalGlue ());
        sizeBoxW2.add (this.sizeBoxWrapper);
        sizeBoxW2.add (Box.createHorizontalGlue ());
        sizeBoxW2.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        Box sizeBoxW = new Box (BoxLayout.Y_AXIS);

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        // Work out the size of the width.
        float pixelWidth = (float) ((float) d.width / (float) this.sizeBoxMaxWidth);

        int maxBoxHeight = (int) (d.height / pixelWidth);

        sizeBoxW.setPreferredSize (new Dimension (this.sizeBoxMaxWidth,
                                                  maxBoxHeight));
        sizeBoxW.add (Box.createVerticalGlue ());
        sizeBoxW.add (sizeBoxW2);
        sizeBoxW.add (Box.createVerticalGlue ());
        sizeBoxW.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        sizeBoxW.setBorder (UIUtils.createPadding (0, 20, 0, 0));
        sizeBoxW.setMaximumSize (new Dimension (sizeBoxW.getPreferredSize ().width + 20,
                                                sizeBoxW.getPreferredSize ().height));
        b.add (sizeBoxW);

        b.add (Box.createVerticalStrut (10));

        final JCheckBox s = UIUtils.createCheckBox (getUIString (project,sidebar,fullscreenproperties,showtimewordcount,text),
        //"Always show the time and word count",
                                                    null);

        s.setBorder (UIUtils.createPadding (5, 10, 0, 0));

        s.setSelected (UserProperties.getAsBoolean (Constants.FULL_SCREEN_SHOW_TIME_WORD_COUNT_PROPERTY_NAME));

        s.addActionListener (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                UserProperties.set (Constants.FULL_SCREEN_SHOW_TIME_WORD_COUNT_PROPERTY_NAME,
                                    s.isSelected ());

            }

        });

        b.add (s);

        return b;

    }

    private void setSizeBoxSize ()
    {

        final Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();

        // Work out the size of the width.
        float pixelWidth = (float) ((float) d.width / (float) sizeBoxMaxWidth);

        int maxBoxHeight = (int) (d.height / pixelWidth);

        int xh = (int) (this.sizeBoxMaxWidth * (1 - (2 * this.fsf.getXBorderWidth ())));
        int yh = (int) (maxBoxHeight * (1 - (2 * this.fsf.getYBorderWidth ())));

        BufferedImage b = new BufferedImage (xh,
                                             yh,
                                             BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = b.createGraphics ();

        g2.setRenderingHint (RenderingHints.KEY_INTERPOLATION,
                             RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage (this.transparentImage,
                      0,
                      0,
                      xh,
                      yh,
                      null);

        this.sizeBox.setImage (b);

        this.sizeBox.setPreferredSize (new Dimension (xh,
                                                      yh));

        this.sizeBox.setMinimumSize (this.sizeBox.getPreferredSize ());

        this.validate ();
        this.repaint ();

    }

    public void backgroundOpacityChanged ()
    {

        this.backgroundOpacity.setValue ((int) (this.fsf.getBorderOpacity () * 100));

    }

    public void displayAreaSizeChanged ()
    {

        this.setSizeBoxSize ();

    }

}
