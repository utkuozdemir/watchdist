package org.thehecklers.monologfx;

import javafx.beans.property.*;
import javafx.scene.control.ControlBuilder;
import javafx.util.Builder;

import java.util.HashMap;

/**
 * @author Mark Heckler (mark.heckler@gmail.com, @MkHeck)
 */
public class MonologFXButtonBuilder<B extends org.thehecklers.monologfx.MonologFXButtonBuilder<B>> extends ControlBuilder<B> implements Builder<org.thehecklers.monologfx.MonologFXButton> {
    private HashMap<String, Property> properties = new HashMap<>();

    protected MonologFXButtonBuilder() {
    }

    /**
     * Creates and returns a MonologFXButton builder object upon which
     * to set properties and eventually, create a MonologFXButton for use with
     * a MonologFX dialog.
     */
    public static org.thehecklers.monologfx.MonologFXButtonBuilder create() {
        return new org.thehecklers.monologfx.MonologFXButtonBuilder();
    }

    /**
     * Sets the type of this button.
     *
     * @param TYPE MonologFXButton.Type designation.
     * @see org.thehecklers.monologfx.MonologFXButton.Type
     */
    public final org.thehecklers.monologfx.MonologFXButtonBuilder type(final org.thehecklers.monologfx.MonologFXButton.Type TYPE) {
        properties.put("type", new SimpleObjectProperty<>(TYPE));
        return this;
    }

    /**
     * Sets the label text for the button.
     * <p/>
     * To assign a shortcut key, simply place an underscore character ("_")
     * in front of the desired shortcut character.
     *
     * @param LABEL String consisting of the desired button text.
     */
    public final org.thehecklers.monologfx.MonologFXButtonBuilder label(final String LABEL) {
        properties.put("label", new SimpleStringProperty(LABEL));
        return this;
    }

    /**
     * Sets the graphic for use on the button, either alone or with text.
     * Graphic format must be .png, .jpg (others?) supported by ImageView.
     *
     * @param ICON String containing the location and name of a graphic file
     *             (.png, .jpg) for use as an icon on the button face.
     * @see ImageView
     */
    public final org.thehecklers.monologfx.MonologFXButtonBuilder icon(final String ICON) {
        properties.put("icon", new SimpleStringProperty(ICON));
        return this;
    }

    /**
     * Designates this button as the "default" button - or not.
     *
     * @param DEFAULTBUTTON Boolean.
     */
    public final org.thehecklers.monologfx.MonologFXButtonBuilder defaultButton(final boolean DEFAULTBUTTON) {
        properties.put("defaultButton", new SimpleBooleanProperty(DEFAULTBUTTON));
        return this;
    }

    /**
     * Designates this button as the "cancel" button - or not.
     *
     * @param CANCELBUTTON Boolean.
     */
    public final org.thehecklers.monologfx.MonologFXButtonBuilder cancelButton(final boolean CANCELBUTTON) {
        properties.put("cancelButton", new SimpleBooleanProperty(CANCELBUTTON));
        return this;
    }

    /**
     * This is where the button is created/assembled. Returns a MonologFXButton
     * object, ready to add to a MonologFX dialog.
     *
     * @return MonologFXButton
     */
    @Override
    public org.thehecklers.monologfx.MonologFXButton build() {
        final org.thehecklers.monologfx.MonologFXButton CONTROL = new org.thehecklers.monologfx.MonologFXButton();

        for (String key : properties.keySet()) {
            switch (key) {
                case "type":
                    CONTROL.setType(((ObjectProperty<MonologFXButton.Type>) properties.get(key)).get());
                    break;
                case "label":
                    CONTROL.setLabel(((StringProperty) properties.get(key)).get());
                    break;
                case "icon":
                    CONTROL.setIcon(((StringProperty) properties.get(key)).get());
                    break;
                case "defaultButton":
                    CONTROL.setDefaultButton(((BooleanProperty) properties.get(key)).get());
                    break;
                case "cancelButton":
                    CONTROL.setCancelButton(((BooleanProperty) properties.get(key)).get());
                    break;
            }
        }

        return CONTROL;
    }
}
