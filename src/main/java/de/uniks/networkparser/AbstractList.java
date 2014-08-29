package de.uniks.networkparser;

/*
 NetworkParser
 Copyright (c) 2011 - 2013, Stefan Lindel
 All rights reserved.

 Licensed under the EUPL, Version 1.1 or (as soon they
 will be approved by the European Commission) subsequent
 versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl5

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.
*/
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import de.uniks.networkparser.interfaces.BaseItem;
import de.uniks.networkparser.sort.EntityComparator;
import de.uniks.networkparser.sort.SortingDirection;
/**
 * The Class EntityList.
 */

public abstract class AbstractList<V> implements BaseItem {
	protected List<V> keys = new ArrayList<V>();
	protected Object[] hashTableKeys = null;
  
	protected boolean allowDuplicate = true;
	protected Comparator<V> cpr;

	protected static final int hashTableStartHashingThreshold = 420;
	protected static final float hashTableLoadThreshold = 0.7f;
	protected int entitySize = 1;

	public Comparator<V> comparator() {
		if (this.cpr==null) {
			withComparator(new EntityComparator<V>().withColumn(EntityComparator.LIST).withDirection(SortingDirection.ASC));
		}
		return cpr;
	}

	public boolean isComparator() {
		return (this.cpr!=null);
	}

	public AbstractList<V> withComparator(Comparator<V> comparator) {
		this.cpr = comparator;
		return this;
	}

	public AbstractList<V> withComparator(String column) {
		this.cpr = new EntityComparator<V>().withColumn(column).withDirection(SortingDirection.ASC);
		return this;
	}

	protected void hashTableAddKey(Object newValue, int pos)
	{
      this.hashTableKeys = hashTableAdd(this.hashTableKeys, this.keys, newValue, pos);
	}

	protected Object[] hashTableAdd(Object[] hashTable, List<?> items, Object newValue, int pos)
	{
		// EnsureCapacity
		if (hashTable == null) {
			if (items.size() * entitySize <= hashTableStartHashingThreshold) {
				return null;
			}
			return hashAdd(hashTableResize(hashTableStartHashingThreshold * entitySize
					* 3, items),newValue, pos);
		}
		if (items.size() * entitySize < hashTableStartHashingThreshold / 10) {
			return null;
		}
		if (items.size() * entitySize > hashTable.length
				* hashTableLoadThreshold) {
			// double hashTable size
			return hashAdd(hashTableResize(hashTable.length * 2, items), newValue, pos);
		}

		if (items.size() * entitySize < hashTable.length / 20) {
			// shrink hashTable size to a loadThreshold of 33%
			return hashAdd(hashTableResize(items.size() * entitySize * 3, items), newValue, pos);
		}
		return hashAdd(hashTable, newValue, pos);
   }

	protected Object[] hashAdd(Object[] hashTable, Object newValue, int pos) {
		int hashKey = hashKey(newValue.hashCode(), hashTable.length);
		while (true) {
			Object oldEntry = hashTable[hashKey];
			if (oldEntry == null) {
				hashTable[hashKey] = newValue;
				if (entitySize == 2) {
					hashTable[hashKey + 1] = pos;
				}
				return hashTable;
			}

			if (oldEntry.equals(newValue))
				return hashTable;

			hashKey = (hashKey + entitySize) % hashTable.length;
		}
	}

   /**
	 * Get the object value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws RuntimeException
	 *             If there is no value for the index.
	 */
	public V get(int index) throws RuntimeException {
		V object = this.keys.get(index);
		if (object == null) {
			throw new RuntimeException("EntityList[" + index + "] not found.");
		}
		return object;
	}

	/**
	 * Compares the two specified Object values. The sign of the integer value returned is the same as that of the integer that would be returned by the call:
	 *   new Object(o1).compareTo(new Object(o2))
	 * @param o1 the first Object to compare
	 * @param o2 the second Object to compare
	 * @return the value 0 if o1 is numerically equal to o2; a value less than 0 if o1 is numerically less than o2; and a value greater than 0 if o1 is numerically greater than o2.
	 */
	public int compare(V o1, V o2) {
		return comparator().compare(o1, o2);
	}

	public abstract AbstractList<V> getNewInstance();

	public void copyEntity(AbstractList<V> target, int pos) {
		target.addEntity(get(pos));
	}

	protected boolean addEntity(V newValue) {
		if (newValue == null)
			return false;
		if (cpr != null) {
			for (int i = 0; i < size(); i++) {
				int result = compare(get(i), newValue);
				if (result >= 0) {
					if (!isAllowDuplicate() && get(i) == newValue) {
						return false;
					}
					addKey(i, newValue);
					V beforeElement = null;
					if (i > 0) {
						beforeElement = this.keys.get(i - 1);
					}
					fireProperty(null, newValue, beforeElement, null);
					return true;
				}
			}
		}

		if (!isAllowDuplicate()) {
			if (this.contains(newValue)) {
				return false;
			}
		}

		boolean result = addKey(-1, newValue);
		if (result) {
			V beforeElement = null;
			if (size() > 1) {
				beforeElement = this.keys.get(size() - 1);
			}
			fireProperty(null, newValue, beforeElement, null);
		}
		return result;
	}

	public AbstractList<V> subSet(V fromElement, V toElement) {
		AbstractList<V> newList = getNewInstance();
	
		// PRE WHILE
		int pos=0;
		int size=size();
		while(pos<size) {
			if (compare(get(pos), fromElement)>=0) {
				copyEntity(newList, pos);
				break;
			}
			pos++;
		}
	
		// MUST COPY
		while(pos<size) {
			if (compare(get(pos), toElement)>=0) {
				break;
			}
			copyEntity(newList, pos++);
		}
		return newList;
	}

	/**
	 * Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive. (If fromIndex and toIndex are equal,
	 * the returned list is empty.) The returned list is backed by this list, so non-structural changes in the returned list are reflected in this list,
	 * and vice-versa. The returned list supports all of the optional list operations supported by this list.
	 *
	 * This method eliminates the need for explicit range operations (of the sort that commonly exist for arrays).
	 * Any operation that expects a list can be used as a range operation by passing a subList view instead of a whole list.
	 * For example, the following idiom removes a range of elements from a list:
	 *
	 * @param fromIndex low endpoint (inclusive) of the subList
	 * @param toIndex high endpoint (exclusive) of the subList
	 * @return a view of the specified range within this list
	 */
	public List<V> subList(int fromIndex, int toIndex) {
		return this.keys.subList(fromIndex, toIndex);
	}

    /**
     * Returns a view of the portion of this map whose keys are less than (or
     * equal to, if {@code inclusive} is true) {@code toKey}.  The returned
     * map is backed by this map, so changes in the returned map are reflected
     * in this map, and vice-versa.  The returned map supports all optional
     * map operations that this map supports.
     *
     * <p>The returned map will throw an {@code IllegalArgumentException}
     * on an attempt to insert a key outside its range.
     *
     * @param toElement high endpoint of the keys in the returned map
     * @param inclusive {@code true} if the high endpoint
     *        is to be included in the returned view
     * @return result a list with less item then the key      
     *
	*/
	public AbstractList<V> headSet(V toElement, boolean inclusive) {
		AbstractList<V> newList = getNewInstance();

		// MUST COPY
		for (int pos=0;pos<size();pos++) {
			int compare = compare(get(pos), toElement);
			if (compare==0) {
				if (inclusive) {
					copyEntity(newList, pos);
				}
				break;
			}else if (compare>0) {
				copyEntity(newList, pos);
				break;
			}
		}
		return newList;
	}

	/**
     * Returns a view of the portion of this map whose keys are greater than (or
     * equal to, if {@code inclusive} is true) {@code fromKey}.
     *
     * @param fromElement low endpoint of the keys in the returned map
     * @param inclusive {@code true} if the low endpoint
     *        is to be included in the returned view
     * @return a view of the portion of this map whose keys are greater than
     *         (or equal to, if {@code inclusive} is true) {@code fromKey}
     *        
     */
	public AbstractList<V> tailSet(V fromElement, boolean inclusive) {
		AbstractList<V> newList = getNewInstance();

		// PRE WHILE
		int pos=0;
		for (;pos<size();pos++) {
			int compare = compare(get(pos), fromElement);
			if (compare==0) {
				if (inclusive) {
					copyEntity(newList, pos);
				}
				break;
			}else if (compare>0) {
				copyEntity(newList, pos);
				break;
			}
		}

		// MUST COPY
		while(pos<size()) {
			copyEntity(newList, pos++);
		}
		return newList;
	}

	/**
	 * @return the First Element of the List
	 */
	public V first()
	{
	   if (this.keys.size() > 0)
	   {
	      return this.keys.get(0);
	   }
	   return null;
	}

	/**
	 * @return the Last Element of the List
	 */
	public V last()
	{
	   if (this.keys.size() > 0)
	   {
	      return this.keys.get(this.size()-1);
	   }
	  
	   return null;
	}

	/**
	 * @param index of value
	 * @return the entity
	 */
	public Object getKey(int index) {
		if (index<0 || index>this.keys.size()) {
			return null;
		}
		return this.keys.get(index);
	}

	/**
	 * Get the boolean value associated with an index. The string values "true"
	 * and "false" are converted to boolean.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 * @throws RuntimeException
	 *             If there is no value for the index or if the value is not
	 *             convertible to boolean.
	 */
	public boolean getBoolean(int index) throws RuntimeException {
		if (index==-1) {
			return false;
		}
		Object object = getItem(index);
		if (object.equals(Boolean.FALSE)
				|| (object instanceof String && ((String) object)
						.equalsIgnoreCase("false"))) {
			return false;
		} else if (object.equals(Boolean.TRUE)
				|| (object instanceof String && ((String) object)
						.equalsIgnoreCase("true"))) {
			return true;
		}
		throw new RuntimeException("EntityList[" + index
				+ "] is not a boolean.");
	}

	/**
	 * Get the double value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws RuntimeException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public double getDouble(int index) throws RuntimeException {
		Object object = getItem(index);
		try {
			return object instanceof Number ? ((Number) object).doubleValue()
					: Double.parseDouble((String) object);
		} catch (Exception e) {
			throw new RuntimeException("EntityList[" + index
					+ "] is not a number.");
		}
	}

	protected Object getItem(int index) {
		return getKey(index);
	}

	/**
	 * Get the int value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws RuntimeException
	 *             If the key is not found or if the value is not a number.
	 */
	public int getInt(int index) throws RuntimeException {
		Object object = getItem(index);
		try {
			return object instanceof Number ? ((Number) object).intValue()
					: Integer.parseInt((String) object);
		} catch (Exception e) {
			throw new RuntimeException("EntityList[" + index
					+ "] is not a number.");
		}
	}

	/**
	 * Get the long value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws RuntimeException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public long getLong(int index) throws RuntimeException {
		Object object = getItem(index);
		try {
			return object instanceof Number ? ((Number) object).longValue()
					: Long.parseLong((String) object);
		} catch (Exception e) {
			throw new RuntimeException("EntityList[" + index
					+ "] is not a number.");
		}
	}

	/**
	 * Get the string associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A string value.
	 * @throws RuntimeException
	 *             If there is no value for the index.
	 */
	public String getString(int index) throws RuntimeException {
		return getItem(index).toString();
	}

	/**
	 * Put or replace an object value in the EntityList. If the index is greater
	 * than the length of the EntityList, then null elements will be added as
	 * necessary to pad it out.
	 *
	 * @param index
	 *            The subscript.
	 * @param key
	 *            The value to put into the array. The value should be a
	 *            Boolean, Double, Integer, EntityList, Entity, Long, or String
	 *            object.
	 * @return this.
	 * @throws RuntimeException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */
	public AbstractList<V> put(int index, V key) throws RuntimeException {
		if (index < 0) {
			throw new RuntimeException("EntityList[" + index + "] not found.");
		}
		if (index < size()) {
		
			V oldValue = null;
			if (index>0) {
				oldValue = this.keys.get(index - 1);
				int position = getPositionKey(oldValue);
				if (position>=0) {
					// Replace in List
					this.hashTableKeys[position] = key;
				}
			}
			// Replace old Vlaue
            this.keys.set(index, key);
			fireProperty(oldValue, key, null, null);
		} else {
			addEntity(key);
		}
		return this;
	}

	/**
	 * Is Allow Duplicate Entity in the List
	 * @return boolean if the List allow duplicate Entities
	 */
	public boolean isAllowDuplicate() {
		return allowDuplicate;
	}

	@SuppressWarnings("unchecked")
	public <ST extends AbstractList<V>> ST withAllowDuplicate(boolean allowDuplicate) {
		this.allowDuplicate = allowDuplicate;
		return (ST) this;
	}

	/**
	 * Remove an index and close the hole.
	 *
	 * @param index
	 *            The index of the element to be removed.
	 * @return The value that was associated with the index, or null if there
	 *         was no value.
	 */
	public V remove(int index) {
		return removeItemByIndex(index);
	}

	protected V removeItemByIndex(int index) {
		if (index<0) {
			return null;
		}
		V oldValue = get(index);
		V beforeValue = null;
		if (index>0) {
			beforeValue = get(index - 1);
		}
		listRemove(index);
		hashTableRemove(oldValue);
		fireProperty(oldValue, null, beforeValue, null);

		return oldValue;
	}

	protected void listRemove(int index) {
		this.keys.remove(index);
	}

	protected Object[] hashTableResize(int size, List<?> keys) {
		Object[] items = new Object[size];
        for (int i=0;i<keys.size();i++) {
            hashAdd(items, keys.get(i), i);
        }
        return items;
	}

	protected int removeItemByObject(Object key) {
		int index;
		if (this.hashTableKeys != null) {
			if (entitySize==1) {
				// change hashTable to Object with ids
		         this.entitySize = 2;
		         this.hashTableKeys = hashTableResize(this.hashTableKeys.length*2, keys);
			}
			index=getPositionKey(key);
	    	if (index<0) {
	    		return -1;
	    	}
			index = (int) this.hashTableKeys[index + 1];
			int diff = index;
			if (index>this.keys.size()) {
				diff = this.keys.size() - 1;
			}
    		while( this.keys.get(diff)!=key) {
    			diff--;
    		}
			if (index - diff > 1000) {
				 removeItemByIndex(diff);
				 this.hashTableKeys = hashTableResize(this.hashTableKeys.length, keys);
				 return diff;
			}
			index = diff;
		}else{
			index=getPositionKey(key);
	    	if (index<0) {
	    		return -1;
	    	}
	    }
		removeItemByIndex(index);
		return index;

	}

	private void hashTableRemove(V oldValue)
   {
	   if (hashTableKeys == null) return;
	  
	   int hashKey = hashKey(oldValue.hashCode(), hashTableKeys.length);
	  
	   while (true)
	   {
	      Object oldEntry = hashTableKeys[hashKey];
	      if (oldEntry == null) return;
	      if (oldEntry.equals(oldValue))
	      {
	         int gapIndex = hashKey;
	         int lastIndex = gapIndex;
	        
	         // search later element to put in this gap
	         while (true)
	         {
	            hashKey = (hashKey + entitySize) % hashTableKeys.length;
	            oldEntry = hashTableKeys[hashKey];
	            if (oldEntry == null)
	            {
	               hashTableKeys[gapIndex] = hashTableKeys[lastIndex];
	               hashTableKeys[lastIndex] = null;
	               if (entitySize==2) {
	            	   hashTableKeys[gapIndex + 1] = hashTableKeys[lastIndex + 1];
	            	   hashTableKeys[lastIndex + 1] = null;
	               }
	               return;
	            }
	           
	            if (hashKey(oldEntry.hashCode(), hashTableKeys.length) <= gapIndex)
	            {
	               lastIndex = hashKey;
	            }
	         }
	      }
	      hashKey = (hashKey + entitySize) % hashTableKeys.length;
	   }
   }

   /**
     * Locate the Entity in the List
     * @param key Entity
     * @return the position of the Entity or -1
     */
    public int getIndex(Object key) {
    	return transformIndex( getPositionKey(key), key);
    }
   
    protected int transformIndex(int index, Object key) {
       if (this.hashTableKeys != null&& index >=0) {
          if (this.entitySize==2) {
             index = (int) this.hashTableKeys[index + 1];
             if (index >= this.keys.size()) {
                index = this.keys.size() - 1;
             }
             while( this.keys.get(index) != key) {
                index--;
             }
             return index;
          }
       }
       return index;
    }
       
    public AbstractList<V> withCopyList(List<V> reference) {
        this.keys = reference;
        return this;
    }

	/**
	 * If the List is Empty
	 *
	 * @return boolean of size
	 */
	public boolean isEmpty() {
        return keys.size() < 1;
    }

	public boolean contains(Object o) {
		return getPositionKey(o)>=0;
	}

    public int getPositionKey(Object o)
    {
        if (this.hashTableKeys != null)
        {
           int hashKey = hashKey(o.hashCode(), hashTableKeys.length);
           while (true)
           {
              Object value = hashTableKeys[hashKey];
              if (value == null) return -1;
              if (value.equals(o)) return hashKey;
              hashKey = (hashKey + entitySize) % hashTableKeys.length;
           }
        }
       
        // search from the end as in models we frequently ask for elements that have just been added to the end
        int pos  = this.keys.size() - 1;
        for (ListIterator<V> i = reverseListIterator();i.hasPrevious();) {
           if (i.previous().equals(o)) {
              return pos;
           }
           pos--;
        }
        return -1;
    }
  
    /**
     * Get the HashKey from a Object with Max HashTableIndex and StepSize of EntitySize
     * @param hashKey the hashKey of a Object
     * @param len the max Length of all Hashvalues
     * @return the hasKey
     */
    public int hashKey(int hashKey, int len)
    {
       int tmp = (hashKey + hashKey % entitySize) % len;
      
      return (tmp < 0) ? -tmp:tmp;
    }

    public Iterator<V> iterator() {
        return keys.iterator();
    }
   
    public Object[] toArray() {
        return keys.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return keys.toArray(a);
    }
   
    public boolean containsAll(Collection<?> c) {
       for (Object o : c)
       {
          if ( ! this.contains(o)) return false;
       }
       return true;
    }

    public boolean removeAll(Collection<?> c) {
        return removeAll(c.iterator());
    }
   
    @SuppressWarnings("unchecked")
	public boolean removeAll(Iterator<?> i) {
		while(i.hasNext()) {
			removeItemByObject((V)i.next());
		}
		return true;
	}
   
    public void clear() {
    	removeAll(iterator());
    }

    public V set(int index, V element) {
        return keys.set(index, element);
    }

    /**
     * Add a Element after the Element from the second Parameter
     * @param element element to add
     * @param beforeElement element before the element
     * @return the List
     */
    public AbstractList<V> withInsert(V element, V beforeElement) {
    	int index = getIndex(beforeElement);
    	add(index, element);
    	return this;
    }
   
    public abstract AbstractList<V> with(Object... values);
   
    @SuppressWarnings("unchecked")
	public <ST extends AbstractList<V>> ST withoutList(Collection<?> values) {
		for (Iterator<?> i = values.iterator(); i.hasNext();) {
			without(i.next());
		}
		return (ST) this;
	}

	@SuppressWarnings("unchecked")
	public <ST extends AbstractList<V>> ST without(Object... values) {
		if (values==null) {
			return null;
		}
		for (Object item : values) {
		
			removeItemByObject((V) item);
		}
		return (ST)this;
	}

	public boolean retainAll(Collection<?> c) {
		for (int i=0;i<size();) {
			if (!c.contains(get(i))) {
				remove(i);
			}
			else
			{
			   i++;
			}
		}
		return true;
	}

	@Override
   public AbstractList<V> clone() {
	   return clone(getNewInstance());
   }

    public void add(int index, V element) {
    	addKey(index, element);
        V beforeValue = null;
        if (index>0) {
        	beforeValue = get(index - 1);
        	fireProperty(null, element, beforeValue, element);
        }
    }

	public AbstractList<V> clone(AbstractList<V> newInstance) {
		newInstance.withComparator( this.cpr);
		newInstance.withAllowDuplicate( isAllowDuplicate());
		newInstance.withList(this.keys);
		return newInstance;
	}

	@SuppressWarnings("unchecked")
	public <ST extends AbstractList<V>> ST withList(Collection<?> values) {
		for (Iterator<?> i = values.iterator(); i.hasNext();) {
			with(i.next());
		}
		return (ST) this;
	}

    public int indexOf(Object o) {
        return keys.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return keys.lastIndexOf(o);
    }

    public ListIterator<V> listIterator() {
        return keys.listIterator();
    }

    public ListIterator<V> listIterator(int index) {
        return keys.listIterator(index);
	}
   
    public ListIterator<V> reverseListIterator() {
       return keys.listIterator(keys.size());
    }

	public int size() {
		return this.keys.size();
	}

	/**
	 * Add a Key to internal List and Array if nesessary
	 * @param newValue the new Value
	 * @param pos the new Position -1 = End
	 * @return if value is added
	 */
	protected boolean addKey(int pos, V newValue) {
		boolean result = true;
		if (pos==-1) {
			result = this.keys.add(newValue);
			pos = this.keys.size();
		}else{
			this.keys.add(pos, newValue);
		}
		if (result) {
			this.hashTableAddKey(newValue, pos);
		}
		return result;
	}

	protected void fireProperty(Object oldElement, Object newElement, Object beforeElement, Object value) {
	}
}