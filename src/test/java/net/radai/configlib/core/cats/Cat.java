/*
 * This file is part of ConfigLib.
 *
 * ConfigLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ConfigLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ConfigLib.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.radai.configlib.core.cats;

import java.util.List;
import java.util.Objects;

/**
 * Created by Radai Rosenblatt
 */
public class Cat {
    private String name;
    private String description;
    private List<String> likes;

    public Cat() {
    }

    public Cat(String name, String description, List<String> likes) {
        this.name = name;
        this.description = description;
        this.likes = likes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cat cat = (Cat) o;
        return Objects.equals(name, cat.name) &&
                Objects.equals(description, cat.description) &&
                Objects.equals(likes, cat.likes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, likes);
    }
}
