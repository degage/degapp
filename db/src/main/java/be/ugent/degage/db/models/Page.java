/* Page.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

package be.ugent.degage.db.models;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Represents a page of a data set of objects of type T. Apart from being iterable it also has a
 * 'full size' which corresponds to the size of the original data set it is a page from and a page size
 * used in pagination.
 * <p>Mainly used for pagination: the full size is the size of the data set of which only
 * one page is shown.</p>
 */
public class Page<T> implements Iterable<T> {

    private Collection<T> base;

    private int pageSize;

    private int fullSize;

    public Page(Collection<T> base, int pageSize) {
        this.base = base;
        this.fullSize = -1;
        this.pageSize = pageSize;
    }

    public int getFullSize() {
        return fullSize;
    }

    public int getNrOfPages() {
        return (fullSize + pageSize - 1) / pageSize;
    }

    public void setFullSize(int fullSize) {
        this.fullSize = fullSize;
    }

    public Collection<T> getBase() {
        return base;
    }

    @Override
    public Iterator<T> iterator() {
        return base.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        base.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return base.spliterator();
    }
}
