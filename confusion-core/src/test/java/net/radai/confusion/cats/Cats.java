/*
 * This file is part of Confusion.
 *
 * Confusion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Confusion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Confusion.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.confusion.cats;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class Cats {
    private String creator;
    private List<String> comments;
    private List<Cat> cats;

    public Cats() {
    }

    public Cats(String creator, List<String> comments, Cat ... cats) {
        this.creator = creator;
        this.comments = comments;
        if (cats == null || cats.length == 0) {
            this.cats = null;
        } else {
            this.cats = Arrays.asList(cats);
        }
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    @Valid
    public List<Cat> getCats() {
        return cats;
    }

    public void setCats(List<Cat> cats) {
        this.cats = cats;
    }
}
