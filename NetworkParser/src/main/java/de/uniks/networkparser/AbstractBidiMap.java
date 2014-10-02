package de.uniks.networkparser;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.uniks.networkparser.interfaces.BidiMap;

public abstract class AbstractBidiMap<K, V> implements BidiMap<K, V>
{

   protected Map<K, V> keyValue;
   protected Map<V, K> valueKey;
   
   protected AbstractBidiMap(final Map<K, V> keyValue, final Map<V, K> valueKey)
   {
      this.keyValue = keyValue;
      this.valueKey = valueKey;
   }
   
   @Override
   public int size() {
      return keyValue.size();
   }

   @Override
   public void clear() {
      keyValue.clear();
      valueKey.clear();
   }

   @Override
   public Collection<V> values() {
      return keyValue.values();
   }

   @Override
   public Set<K> keySet() {
      return keyValue.keySet();
   }

   @Override
   public boolean containKey(Object key) {
      return keyValue.containsKey(key);
   }

   @Override
   public boolean containValue(Object value) {
      return valueKey.containsKey(value);
   }

   @Override
   public BidiMap<K, V> without(Object key, Object value)
   {
      keyValue.remove(key, value);
      valueKey.remove(value, key); 
      return this;
   }

   @Override
   public BidiMap<K, V> with(K key, V value)
   {
      keyValue.put(key, value);
      valueKey.put(value, key);      
      return this;
   }

   @Override
   public V getValueItem(Object key)
   {
      return keyValue.get(key);
   }

   @Override
   public K getKey(Object value)
   {
      return valueKey.get(value);
   }

}
