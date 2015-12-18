package net.radai.configlib.core.runtime;

import net.radai.configlib.core.util.FormatUtil;

import java.lang.reflect.Type;

/**
 * @author Radai Rosenblatt
 */
public class MapSection implements Section {
    private String name;
    private Type keyType;
    private Type valueType;

    public MapSection(String name, Type keyType, Type valueType) {
        this.name = name;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": Map<");
        if (keyType != null) {
            sb.append(FormatUtil.prettyPrint(keyType));
        } else {
            sb.append("?");
        }
        sb.append(", ");
        if (valueType != null) {
            sb.append(FormatUtil.prettyPrint(valueType));
        } else {
            sb.append("?");
        }
        sb.append(">");
        return sb.toString();
    }
}
