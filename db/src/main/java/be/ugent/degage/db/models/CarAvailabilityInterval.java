/* CarAvailabilityInterval.java
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

/**
 * Represents an interval in which a car is not available
 */
public class CarAvailabilityInterval {

    private int id;

    private int start; // start time, in seconds sinds sunday 00:00
    private int end;   // end time, in seconds since sunday 00:00

    public CarAvailabilityInterval(int id, int start, int end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    /**
     * Check whether this interval overlaps with the given interval
     */
    public boolean overlaps (CarAvailabilityInterval other) {
        return other.start < this.end && this.start < other.end;
    }

    public static final int SECONDS_IN_WEEK = 7*24*3600;

    /**
     * Check whether this interval overlaps cyclically with the given interval
     */
    public boolean overlapsCyclically(CarAvailabilityInterval other) {
        return other.start < this.end && this.start < other.end
                || other.start + SECONDS_IN_WEEK < this.end && this.start < other.end + SECONDS_IN_WEEK
                || other.start < this.end + SECONDS_IN_WEEK && this.start + SECONDS_IN_WEEK < other.end;
    }

    /**
     * Check whether there is a (cyclic) overlap within a (short) list of intervals
     */
    public static boolean containsOverlap (Iterable<CarAvailabilityInterval> list) {
        for (CarAvailabilityInterval interval1 : list) {
            for (CarAvailabilityInterval interval2 : list) {
                if (interval1 != interval2 && interval1.overlapsCyclically(interval2)) {
                    return true;
                }
            }
        }
        return false;
    }
}
