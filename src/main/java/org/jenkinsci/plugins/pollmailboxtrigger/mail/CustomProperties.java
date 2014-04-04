package org.jenkinsci.plugins.pollmailboxtrigger.mail;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Quick and dirty Properties implementation. (But with shorthand get/put methods).
 *
 * @author nickgrealy@gmail.com
 */
@SuppressWarnings("unused")
public class CustomProperties {

    private Map<String, String> delegate = new HashMap<String, String>();

    public CustomProperties() {
    }

    public CustomProperties(Properties properties1) {
        putAll(properties1);
    }

    public CustomProperties(String properties) throws IOException {
        Properties p = new Properties();
        p.load(new StringReader(properties));
        putAll(p);
    }

    public void putAll(Properties properties1) {
        for (String key : properties1.stringPropertyNames()) {
            this.delegate.put(key, properties1.getProperty(key));
        }
    }

    public Properties getProperties() {
        Properties p = new Properties();
        p.putAll(delegate);
        return p;
    }

    public boolean has(String o) {
        return delegate.containsKey(o);
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
