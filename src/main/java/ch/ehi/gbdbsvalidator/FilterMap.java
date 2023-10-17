package ch.ehi.gbdbsvalidator;

public interface FilterMap extends java.lang.Iterable<String>{
    public void put(String key, String value);
    public String get(String key);
    public java.util.Iterator <String> iterator();
}
