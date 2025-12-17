package mekonomen.repo;

import mekonomen.model.Item;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final List<Item> items = new ArrayList<Item>();

    public List<Item> getAll() {
        return new ArrayList<Item>(items);
    }

    public void setAll(List<Item> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
    }

    public void add(Item it) { items.add(it); }

    public boolean removeById(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }

    public Item findById(String id) {
        for (Item it : items) {
            if (it.getId().equals(id)) return it;
        }
        return null;
    }
}