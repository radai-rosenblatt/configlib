package net.radai.configlib.core.runtime;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Radai Rosenblatt
 */
public class BeanSection implements Section {
    private String name;
    private Type type;
    private Map<String, Property> properties = new HashMap<>();

    public BeanSection(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
