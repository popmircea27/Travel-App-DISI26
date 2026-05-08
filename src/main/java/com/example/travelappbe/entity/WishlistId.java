package com.example.travelappbe.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class WishlistId implements Serializable {

    private UUID user;
    private UUID location;

    public WishlistId() {
    }

    public WishlistId(UUID user, UUID location) {
        this.user = user;
        this.location = location;
    }

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    public UUID getLocation() {
        return location;
    }

    public void setLocation(UUID location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishlistId that = (WishlistId) o;
        return Objects.equals(user, that.user) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, location);
    }
}