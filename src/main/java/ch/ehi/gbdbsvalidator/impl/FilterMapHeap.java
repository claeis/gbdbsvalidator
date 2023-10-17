package ch.ehi.gbdbsvalidator.impl;

import java.util.Iterator;

import ch.ehi.gbdbsvalidator.FilterMap;

public class FilterMapHeap  implements FilterMap  {
    private java.util.Map<String,String> map= new java.util.HashMap<String,String>();
    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public Iterator<String> iterator() {
        return map.keySet().iterator();
    }

}
