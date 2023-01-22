package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

import javafx.event.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class Panel extends BaseVBox implements Stateful
{

    private PanelContent content = null;
    private String panelId = null;
    private String styleName = null;
    private StringProperty titleProp = null;
    private Supplier<Set<Node>> toolbarItemSupplier = null;
    private ContextMenu contextMenu = null;
    private Map<KeyCombination, Runnable> actionMappings = null;

    private Panel (Builder b)
    {

        this._init (b);

    }

    public Map<KeyCombination, Runnable> getActionMappings ()
    {

        if (this.actionMappings == null)
        {

            this.actionMappings = new HashMap<> ();

        }

        return this.actionMappings;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        this.content.init (s);

    }

    @Override
    public State getState ()
    {

        return this.content.getState ();

    }

    /**
     * Get a builder to create a new menu item.
     *
     * Usage: QuollMenuItem.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Builder builder ()
    {

        return new Builder ();

    }

    private void _init (Builder b)
    {

        if (b.content == null)
        {

            throw new IllegalArgumentException ("Content must be provided.");

        }

        if (b.panelId == null)
        {

            throw new IllegalArgumentException ("Panel id must be provided.");

        }

        if (b.title == null)
        {

            throw new IllegalArgumentException ("Title must be provided.");

        }

        final Panel _this = this;

        this.actionMappings = b.actionMappings;
        this.content = b.content;
        this.titleProp = b.title;
        this.panelId = b.panelId;
        this.setId (this.panelId);
        VBox.setVgrow (b.content,
                       Priority.ALWAYS);
        this.setFillWidth (true);
        this.getChildren ().add (b.content);

        this.getStyleClass ().add (StyleClassNames.PANEL);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);
            this.styleName = b.styleName;

        }

        UIUtils.addStyleSheet (this,
                               Constants.PANEL_STYLESHEET_TYPE,
                               StyleClassNames.PANEL);

        if (b.styleSheet != null)
        {

            UIUtils.addStyleSheet (this,
                                   Constants.PANEL_STYLESHEET_TYPE,
                                   b.styleSheet);

        }

        this.addEventHandler (MouseEvent.MOUSE_CLICKED,
                              ev ->
        {

            if (_this.contextMenu != null)
            {

                _this.contextMenu.hide ();
                _this.contextMenu = null;

            }

        });

        this.setOnContextMenuRequested (ev ->
        {

            if (ev.getSource () != _this)
            {

                return;

            }

            if (b.contextMenuItemSupplier != null)
            {

                ContextMenu cm = new ContextMenu ();
                cm.setAutoHide (true);

                if (_this.contextMenu != null)
                {

                    _this.contextMenu.hide ();

                }

                ev.consume ();

                cm.getItems ().addAll (b.contextMenuItemSupplier.get ());

                cm.show (_this, ev.getScreenX (), ev.getScreenY ());

                _this.contextMenu = cm;

            }

        });

    }

    public static class Builder implements IBuilder<Builder, Panel>
    {

        private PanelContent content = null;
        private String styleName = null;
        private String styleSheet = null;
        private String panelId = null;
        private Map<KeyCombination, Runnable> actionMappings = null;
        private StringProperty title = null;
        private Supplier<Set<Node>> toolbarItemSupplier = null;
        private Supplier<Set<MenuItem>> contextMenuItemSupplier = null;

        public Builder actionMappings (Map<KeyCombination, Runnable> am)
        {

            this.actionMappings = am;
            return this;

        }

        public Builder styleSheet (String s)
        {

            this.styleSheet = s;
            return this;

        }

        public Builder toolbar (final Set<Node> items)
        {

            this.toolbarItemSupplier = new Supplier<Set<Node>> ()
            {

                @Override
                public Set<Node> get ()
                {

                    return items;

                }

            };

            return this;

        }

        public Builder toolbar (Supplier<Set<Node>> items)
        {

            this.toolbarItemSupplier = items;
            return this;

        }

        public Builder contextMenu (final Set<MenuItem> items)
        {

            this.contextMenuItemSupplier = new Supplier<Set<MenuItem>> ()
            {

                @Override
                public Set<MenuItem> get ()
                {

                    return items;

                }

            };

            return this;

        }

        public Builder contextMenu (Supplier<Set<MenuItem>> items)
        {

            this.contextMenuItemSupplier = items;
            return this;

        }

        public Builder panelId (String id)
        {

            this.panelId = id;
            return this;

        }

        public Builder content (PanelContent c)
        {

            this.content = c;
            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

        public Builder title (List<String> prefix,
                              String... ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder title (StringProperty prop)
        {

            this.title = prop;
            return this;

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        @Override
        public Panel build ()
        {

            return new Panel (this);

        }

    }

    public PanelContent getContent ()
    {

        return this.content;

    }

    public String getStyleClassName ()
    {

        return this.styleName;

    }

    public StringProperty titleProperty ()
    {

        return this.titleProp;

    }

    public String getPanelId ()
    {

        return this.panelId;

    }

    public Set<Node> getToolBarItems (boolean fullScreen)
    {

        if (this.toolbarItemSupplier != null)
        {

            return this.toolbarItemSupplier.get ();

        }

        return null;

    }

    public static class PanelEvent extends Event
    {

        public static final EventType<PanelEvent> READY_FOR_USE_EVENT = new EventType<> ("panel.readyforuse");
        public static final EventType<PanelEvent> CLOSE_EVENT = new EventType<> ("panel.close");
        public static final EventType<PanelEvent> SAVED_EVENT = new EventType<> ("panel.saved");
        public static final EventType<PanelEvent> SHOW_EVENT = new EventType<> ("panel.show");

        private Panel panel = null;

        public PanelEvent (Panel                 panel,
                           EventType<PanelEvent> type)
        {

            super (type);

            this.panel = panel;

        }

        public Panel getPanel ()
        {

            return this.panel;

        }

    }

}
