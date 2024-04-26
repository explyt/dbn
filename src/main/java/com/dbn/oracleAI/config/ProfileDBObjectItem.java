package com.dbn.oracleAI.config;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * POJO class for profile object list
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ProfileDBObjectItem {
    @NotNull
    public String owner;
    //profile object name, if null means all object of that owner
    public String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileDBObjectItem that = (ProfileDBObjectItem) o;
        // object name and owner ae case in-sensitive in Oracle DB
        // name can be null

        return owner.equalsIgnoreCase(that.owner) && (name != null && name.equalsIgnoreCase(that.name));
    }

    public boolean isEquivalentTo(DBObjectItem other) {
        if (!this.owner.equalsIgnoreCase(other.getOwner())) {
            return false;
        }
        // name in DBObjectItem are never null
        if (this.name != null && !this.name.equalsIgnoreCase(other.getName())) {
            return false;
        }
        return true;
    }
}
