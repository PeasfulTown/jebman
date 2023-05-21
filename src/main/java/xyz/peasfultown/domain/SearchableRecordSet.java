package xyz.peasfultown.domain;

import java.util.LinkedHashSet;

public class SearchableRecordSet<R extends Record> extends LinkedHashSet<R> {
    public R getById(int id) {
        for (R r : this) {
            if (r.getId() == id)
                return r;
        }
        return null;
    }

    public R getByName(String name) {
        for (R r : this) {
            if (r.getName().equalsIgnoreCase(name))
                return r;
        }
        return null;
    }
}
