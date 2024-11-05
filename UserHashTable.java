/*
 
Implement a Hash Table
Java implementation of a hash table with string keys and values. This hash table supports the following features:

1. O(1) Lookup Time: Implemented using a hash function for fast indexing.
2. Custom Hash Function: Uses a simple but effective hash function.
3. Dynamic Resizing: Doubles the size of the table when the load factor exceeds a threshold.

a. O(1) Lookup Time: The get() method uses the hash function to determine the index of the key in constant time. If there are collisions, it searches through the linked list (in chained entries) at the given index, but if the hash function distributes keys evenly, this lookup remains efficient.

b. Hash Function: The hash function uses Java's Object.hashcode() to produce a hash value and then compress it to an index within the bounds of the table using the modulo operation. hashCode() is a simple yet effective approach for strings, as it produces relatively uniform distribution.

c. Collision Resolution: This implementation uses separate chaining, where each index of the hash table holds a linked list of entries that hash to that index. This ensures that each slot in the table can hold multiple entries, which allows efficient management of collisions.

d. Resizing: The resize() method doubles the capacity when the load factor (ratio of elements to table size) exceeds a threshold (0.75). It creates a new table and rehashes all entries, ensuring efficient performance as the table grows.
 
*/


import java.util.Objects;
public class UserHashTable {
    private static class Entry {
        final String key;
        String value;
        Entry next;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry[] table;
    private int size;
    private static final float LOAD_FACTOR = 0.75f;
    private static final int INITIAL_CAPACITY = 16;

    public UserHashTable() {
        table = new Entry[INITIAL_CAPACITY];
        size = 0;
    }

    // Hash function using Java's built-in hashCode and compression within table size
    private int hash(String key) {
        return Math.abs(Objects.hashCode(key)) % table.length;
    }

    // Adds a key-value pair to the hash table
    public void put(String key, String value) {
        if (size >= table.length * LOAD_FACTOR) {
            resize();
        }

        int index = hash(key);
        Entry current = table[index];

        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        Entry newEntry = new Entry(key, value);
        newEntry.next = table[index];
        table[index] = newEntry;
        size++;
    }

    // Retrieves the value associated with a given key
    public String get(String key) {
        int index = hash(key);
        Entry current = table[index];
       
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null; // Key not found
    }

    // Removes a key-value pair from the hash table
    public boolean remove(String key) {
        int index = hash(key);
        Entry current = table[index];
        Entry previous = null;

        while (current != null) {
            if (current.key.equals(key)) {
                if (previous == null) {
                    table[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    // Resizes the table to double its current capacity
    private void resize() {
        Entry[] oldTable = table;
        table = new Entry[oldTable.length * 2];
        size = 0;

        for (Entry entry : oldTable) {
            while (entry != null) {
                put(entry.key, entry.value);  // Rehash and add to new table
                entry = entry.next;
            }
        }
    }

    // Returns the number of elements in the table
    public int size() {
        return size;
    }

    public static void main(String[] args) {
        UserHashTable hashTable = new UserHashTable();

        hashTable.put("Shakti", "India");
        hashTable.put("John", "US");
        hashTable.put("Yinghao", "China");

        // Retrieve and print values
        System.out.println("Get value for Shakti: " + hashTable.get("Shakti"));
        System.out.println("Get value for Yinghao: " + hashTable.get("Yinghao"));

        // Demonstrate resizing by adding many items
        for (int i = 0; i < 50; i++) {
            hashTable.put("key" + i, "value" + i);
        }
        System.out.println("Table size after resizing: " + hashTable.size());

        // Demonstrate removing an item
        hashTable.remove("John");
        System.out.println("Get John after removal: " + hashTable.get("John"));  // Should print "null"
    }
}