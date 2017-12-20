package ui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

@Plugin(name=TextAreaAppender.PLUGIN_NAME, category= Core.CATEGORY_NAME,
        elementType= Appender.ELEMENT_TYPE, printObject=true)
public class TextAreaAppender extends AbstractAppender {
    public static final String PLUGIN_NAME = "TextArea";

    private static TextArea textArea;

    private TextAreaAppender(String name, Filter filter,
                               Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    @PluginFactory
    public static TextAreaAppender createAppender(@PluginAttribute("name") String name,
                                              @PluginElement("Layout") Layout<? extends Serializable> layout,
                                              @PluginElement("Filters") Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for StubAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new TextAreaAppender(name, filter, layout, true);
    }

    @Override
    public void append(LogEvent event) {
        try {
            final String message = new String(getLayout().toByteArray(event));
            Platform.runLater(() -> {
                if (textArea != null) {
                    textArea.appendText(message);
                }
            });
        } catch (Exception e) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(e);
            }
        }
    }

    public static void setTextArea(TextArea textArea) {
        TextAreaAppender.textArea = textArea;
    }
}
