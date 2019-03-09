package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ReportBugPopup extends PopupContent
{

    public static final String POPUP_ID = "reportbug";

    private QuollTextArea desc = null;
    private QuollTextField email = null;
    private QuollCheckBox sendLogFiles = null;
    private QuollCheckBox sendScreenshot = null;

    public ReportBugPopup (AbstractViewer viewer)
    {

        super (viewer);

        final ReportBugPopup _this = this;

        this.setPrefSize (javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);

        List<String> prefix = Arrays.asList (project,actions,reportproblem);

        this.desc = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,description,tooltip)))
            .maxChars (10000)
            //.autoGrabFocus (true)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        javafx.scene.layout.VBox.setVgrow (this.desc,
        javafx.scene.layout.Priority.ALWAYS);

        this.email = QuollTextField.builder ()
            .styleClassName (StyleClassNames.EMAIL)
            .build ();

        this.sendLogFiles = QuollCheckBox.builder ()
            .selected (true)
            .label (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,sendlogfiles,text)))
            .build ();

        this.sendScreenshot = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,sendscreenshot,text)))
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,sendscreenshot,tooltip)))
            .build ();

        Form f = Form.builder ()
            .description (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,text)))
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,buttons,send)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,buttons,cancel)))
            .item (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,LanguageStrings.description,text)),
                   this.desc)
            .item (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,LanguageStrings.email,text)),
                   this.email)
            .item (this.sendLogFiles)
            .item (this.sendScreenshot)
            .withViewer (this.viewer)
            .build ();

        f.setOnCancel (ev -> _this.getPopup ().close ());

        f.setOnConfirm (ev ->
        {

            ev.getForm ().hideError ();

            StringProperty emErr = Utils.checkEmail (email.getText ());

            if (emErr != null)
            {

                ev.getForm ().showError (emErr);

                return;

            }

            if (desc.getText ().trim ().equals (""))
            {

                ev.getForm ().showError (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,errorlabel)));

                return;

            }

            _this.getPopup ().close ();

            StringBuilder dets = new StringBuilder ("Email: " + email.getText () + "\nDetails: " + desc.getText ());

            // TODO: Fix this, have a toString on project viewer instead.
            if (_this.getViewer () instanceof AbstractProjectViewer)
            {

                Project proj = ((AbstractProjectViewer) _this.getViewer ()).getProject ();

                dets.append ("\nCurrent project id: " + proj.getId ());

            }

            // Send the message.
            Map details = new HashMap ();
            details.put ("details",
                         dets.toString ());

            details.put ("email",
                         email.getText ());

            try
            {

                // Get the log files?
                if (sendLogFiles.isSelected ())
                {

                    details.put ("errorLog",
                                 new String (Files.readAllBytes (Environment.getErrorLogPath ()),
                                             StandardCharsets.UTF_8));
                    details.put ("editorsMessageLog",
                                 new String (Files.readAllBytes (EditorsEnvironment.getEditorsMessageLogFile ().toPath ()),
                                             StandardCharsets.UTF_8));

                }

                if (sendScreenshot.isSelected ())
                {
                    details.put ("screenshot",
                                 com.quollwriter.Base64.encodeBytes (UIUtils.getImageBytes (UIUtils.getImageOfNode (_this.getViewer ().getViewer ().getScene ().getRoot ()))));

                }

                Environment.sendMessageToSupport ("bug",
                                                  details,
                                                  () ->
                {

                    desc.setText ("");

                    ComponentUtils.showMessage (_this.getViewer (),
                                                getUILanguageStringProperty (Utils.newList (prefix,confirmpopup, LanguageStrings.title)),
                                                //"Problem/Bug reported",
                                                getUILanguageStringProperty (Utils.newList (prefix,confirmpopup,text)));
                                                //"Thank you, the problem has been logged with Quoll Writer support.  If you provided an email address then you should get a response within 1-2 days.  If not feel then free to send the message again.");

                    _this.getViewer ().fireProjectEvent (ProjectEvent.Type.bugreport,
                                                         ProjectEvent.Action.submit);

                });

            } catch (Exception e)
            {

                Environment.logError ("Unable to send message to support",
                                      e);

                ComponentUtils.showErrorMessage (_this.viewer,
                                                 getUILanguageStringProperty (Utils.newList (prefix,actionerror)));
                                          //"Unable to send message.");

            }

        });

        this.getChildren ().add (f);

    }

    @Override
    public QuollPopup createPopup ()
    {

        List<String> prefix = Arrays.asList (project,actions,reportproblem);

        QuollPopup p = QuollPopup.builder ()
            .title (Utils.newList (prefix,LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.REPORTBUG)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
