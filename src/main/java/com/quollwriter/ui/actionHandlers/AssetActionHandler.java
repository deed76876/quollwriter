package com.quollwriter.ui.actionHandlers;

import java.awt.event.*;
import java.awt.font.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.renderers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class AssetActionHandler extends AbstractFormPopup <ProjectViewer, Asset>
{

    private ObjectNameUserConfigurableObjectFieldViewEditHandler nameHandler = null;
    private ObjectDescriptionUserConfigurableObjectFieldViewEditHandler descHandler = null;
    private int              showAt = -1;
    private boolean          displayAfterSave = false;

    /**
     * Only used for add now.
     */
    public AssetActionHandler (Asset         a,
                               ProjectViewer pv)
    {

        super (a,
               pv,
               ADD,
               true);

        this.setPopupOver (pv);

    }

    public void setDisplayAfterSave (boolean v)
    {

        this.displayAfterSave = v;

    }

    public boolean isDisplayAfterSave ()
    {

        return this.displayAfterSave;

    }

    @Override
    public JComponent getFocussedField ()
    {

        return this.nameHandler.getInputFormItem ().getTextField ();

    }

    @Override
    public Icon getIcon (int iconSizeType)
    {

        return null; // TODO this.object.getUserConfigurableObjectType ().getIcon16x16 ();

    }

    @Override
    public String getTitle ()
    {

        if (this.mode == AssetActionHandler.EDIT)
        {

            return String.format (getUIString (assets,edit,popup,title),
                                  this.object.getObjectTypeName ());
                                  //"Edit " + this.object.getObjectTypeName ();

        }

        return String.format (getUIString (assets,add,popup,title),
                              this.object.getObjectTypeName ());
        //"Add New " + this.object.getObjectTypeName ();

    }

    @Override
    public Set<FormItem> getFormItems (String      selectedText)
    {

        int c = 0;
/*
TODO
        for (UserConfigurableObjectFieldViewEditHandler h : this.object.getViewEditHandlers (this.viewer))
        {

            UserConfigurableObjectTypeField f = h.getTypeField ();

            if (f instanceof ObjectDescriptionUserConfigurableObjectTypeField)
            {

                this.descHandler = (ObjectDescriptionUserConfigurableObjectFieldViewEditHandler) h;

                continue;

            }

            if (f instanceof ObjectNameUserConfigurableObjectTypeField)
            {

                this.nameHandler = (ObjectNameUserConfigurableObjectFieldViewEditHandler) h;

                continue;

            }

            c++;

        }
*/
        Set<FormItem> items = new LinkedHashSet ();

        items.addAll (this.nameHandler.getInputFormItems (selectedText,
                                                          this.getSaveAction ()));

        if (this.descHandler != null)
        {

            items.addAll (this.descHandler.getInputFormItems (null,
                                                              this.getSaveAction ()));

        }

        if ((c > 0)
            &&
            (this.mode == AssetActionHandler.ADD)
           )
        {

            final AssetActionHandler _this = this;

            JLabel l = UIUtils.createClickableLabel (getUIString (assets,add,popup,showdetail,text),
                                                    //"Show all fields",
                                                     Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                          Constants.ICON_MENU),
                                                     new ActionListener ()
                                                     {

                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            try
                                                            {

                                                                _this.object.setName (_this.nameHandler.getInputSaveValue ());

                                                                if (_this.descHandler != null)
                                                                {

                                                                    _this.object.setDescription (_this.descHandler.getInputSaveValue ());

                                                                }

                                                                _this.viewer.showAddAsset (_this.object,
                                                                                           null);

                                                                _this.hidePopup ();

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to init asset fields",
                                                                                      e);

                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                          getUIString (assets,add,popup,errors,showdetail));
                                                                                          //"Unable to show add tab for " +
                                                                                          //_this.object.getObjectTypeName ());

                                                                return;

                                                            }

                                                        }

                                                     });
/*
            String df = "field is ";

            if (this.descHandler != null)
            {

                df = String.format ("and %s fields are ",
                                    this.descHandler.getTypeField ().getFormName ());

            }
*/
            l.setToolTipText (String.format (getUIString (assets,add,popup,showdetail,tooltip),
            //"<html>Only the %s %sshown here, to see all the fields (%s more) for the %s click the link.<br />Note: the %s add form will be shown in a new tab.</html>",
            //                                 this.nameHandler.getTypeField ().getFormName (),
            //                                 df,
            //                                 Environment.formatNumber (c),
            //                                 this.object.getObjectTypeName (),
                                             this.object.getObjectTypeName ()));

            // Add open in full tab link...
            items.add (new AnyFormItem (null,
                                        l));

        }

        return items;

    }

    @Override
    public void handleCancel ()
    {

        // Nothing to do.

    }

    @Override
    public Set<String> getFormErrors ()
    {

        Set<String> errs = new LinkedHashSet ();

        Object _name = this.nameHandler.getInputSaveValue ();

        if (_name == null)
        {

            errs.add (String.format (getUIString (assets,add,popup,errors,noname),
                                     this.nameHandler.getTypeField ().getFormName ()));
                                     //"Please select a " + this.nameHandler.getTypeField ().getFormName ());

            return errs;

        } else {

            String name = null;

            // Bit of a cheat here.
            if (_name instanceof StringWithMarkup)
            {

                name = ((StringWithMarkup) _name).getText ();

            }

            if (_name instanceof String)
            {

                name = (String) _name;

            }

            Set<String> nerrs = this.nameHandler.getInputFormItemErrors ();

            if (nerrs != null)
            {

                errs.addAll (nerrs);

            }

        }

        if (this.descHandler != null)
        {

            Set<String> derrs = this.descHandler.getInputFormItemErrors ();

            if (derrs != null)
            {

                errs.addAll (derrs);

            }

        }

        return errs;

    }

    @Override
    public boolean handleSave ()
    {

        try
        {

            this.nameHandler.updateFieldFromInput ();

        } catch (Exception e) {

            Environment.logError ("Unable to get name value from: " +
                                  this.nameHandler,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      String.format (getUIString (assets,add,actionerror),
                                                     this.object.getObjectTypeName ()));
                                      //"Unable to add new " + this.object.getObjectTypeName () + ".");

            return false;

        }

        if (this.descHandler != null)
        {

            try
            {

                this.descHandler.updateFieldFromInput ();

            } catch (Exception e) {

                Environment.logError ("Unable to get description value from: " +
                                      this.descHandler,
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          String.format (getUIString (assets,add,actionerror),
                                                         this.object.getObjectTypeName ()));
                                          //"Unable to add new " + this.object.getObjectTypeName () + ".");

                return false;

            }

        }

        Set<String> oldNames = this.object.getAllNames ();

        this.object.setProject (this.viewer.getProject ());

        if (mode == AssetActionHandler.ADD)
        {

            try
            {

                this.viewer.saveObject (this.object,
                                        true);

                this.viewer.getProject ().addAsset (this.object);

                this.viewer.openObjectSection (this.object.getObjectType ());

                this.viewer.fireProjectEvent (this.object.getObjectType (),
                                              ProjectEvent.NEW,
                                              this.object);

            } catch (Exception e)
            {

                Environment.logError ("Unable to add new: " +
                                      this.object,
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          String.format (getUIString (assets,add,actionerror),
                                                         this.object.getObjectTypeName ()));
                                          //"Unable to add new " + this.object.getObjectTypeName () + ".");

                return false;

            }

        } else {

            try
            {

                this.viewer.saveObject (this.object,
                                        true);

                this.viewer.fireProjectEvent (this.object.getObjectType (),
                                              ProjectEvent.EDIT,
                                              this.object);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save asset: " +
                                      this.object,
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          getUIString (assets,save,actionerror));
                                          //"Unable to save " + this.object.getObjectTypeName () + ".");

                return false;

            }

        }

        this.viewer.updateProjectDictionaryForNames (oldNames,
                                                     this.object);

        if (this.displayAfterSave)
        {

            this.viewer.viewObject (this.object);

        }

        this.viewer.reloadTreeForObjectType (this.object.getUserConfigurableObjectType ().getObjectTypeId ());

        return true;

    }

}
