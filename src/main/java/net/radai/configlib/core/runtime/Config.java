package net.radai.configlib.core.runtime;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Radai Rosenblatt
 */
public class Config {
    private Map<String, Property> properties = new HashMap<>();
    private Map<String, Section> sections = new HashMap<>();
    private Map<Type, Codec> codecs = new HashMap<>();

    public void addProperty(Property prop) {
        String name = prop != null ? prop.getName() : null;
        if (prop == null || name == null || name.isEmpty() || properties.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        properties.put(name, prop);
    }

    public Property getProperty(String propName) {
        return properties.get(propName);
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void addSection(Section section) {
        String name = section != null ? section.getName() : null;
        if (section == null || name == null || name.isEmpty() || sections.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        sections.put(name, section);
    }

    public Map<String, Section> getSections() {
        return sections;
    }

    public void addCodec(Codec codec) {
        Type type = codec != null ? codec.getType() : null;
        if (codec == null || type == null || codecs.containsKey(type)) {
            throw new IllegalArgumentException();
        }
        codecs.put(type, codec);
    }

    public Codec getCodecFor(Type type) {
        return codecs.get(type);
    }

    public boolean hasCodecFor(Type type) {
        return getCodecFor(type) != null;
    }
}
