package org.jenkinsci.plugins.pollmailboxtrigger.mail;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Quick and dirty Properties implementation. (But with shorthand get/put methods).
 *
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public class CustomProperties {

    private Map<String, String> delegate = new HashMap<String, String>();

    public CustomProperties() {
    }

    public CustomProperties(Properties properties1) {
        putAll(properties1);
    }

    public CustomProperties(String properties) {
        Properties p = new Properties();
        try {
            if (properties != null && !"".equals(properties)) {
                p.load(new StringReader(properties));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        putAll(p);
    }

    public void putAll(Properties properties1) {
        for (String key : properties1.stringPropertyNames()) {
            this.delegate.put(key, properties1.getProperty(key));
        }
    }

    public Properties getProperties() {
        Properties p = new Properties();
        for (Map.Entry entry : delegate.entrySet()) {
            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                p.put(entry.getKey(), entry.getValue());
            }
        }
        return p;
    }

    public boolean has(String o) {
        return delegate.containsKey(o);
    }

    public boolean has(Enum o) {
        return has(o.name());
    }

    public String get(Enum o) {
        return get(o.name());
    }

    public String put(Enum s, String s2) {
        return put(s.name(), s2);
    }

    public String remove(Enum o) {
        return remove(o.name());
    }

    public String putIfBlank(Enum s, String s2) {
        return putIfBlank(s.name(), s2);
    }

    public String putIfBlank(String s, String s2) {
        if (!has(s) || get(s) == null || "".equals(get(s))) {
            return put(s, s2);
        }
        return null;
    }

    public String toString() {
        return MailWrapperUtils.Stringify.toString(delegate);
    }
    /* delegate methods */

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean containsKey(String o) {
        return delegate.containsKey(o);
    }

    public boolean containsValue(String o) {
        return delegate.containsValue(o);
    }

    public String get(String o) {
        return delegate.get(o);
    }

    public String put(String s, String s2) {
        return delegate.put(s, s2);
    }

    public String remove(String o) {
        return delegate.remove(o);
    }

    public void putAll(Map<? extends String, ? extends String> map) {
        delegate.putAll(map);
    }

    public void clear() {
        delegate.clear();
    }

    public Set<String> keySet() {
        return delegate.keySet();
    }

    public Collection<String> values() {
        return delegate.values();
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomProperties)) return false;
        CustomProperties that = (CustomProperties) o;
        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
