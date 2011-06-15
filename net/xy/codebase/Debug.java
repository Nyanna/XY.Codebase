/**
 * This file is part of XY.Codebase, Copyright 2011 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 * 
 * XY.Codebase is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * XY.Codebase is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with XY.Codebase. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.xy.codebase;

/**
 * higher jre level wrapper for debug class
 * 
 * @author xyan
 * 
 */
public class Debug {

    /**
     * concatenates various object to be proper displayed on console or vice
     * versa
     * 
     * @param args
     * @return value
     */
    public static String fields(final Object... args) {
        return net.xy.codebasel.Debug.fields(args);
    }

    /**
     * prints values in common format of "Message was wrong [*message*]"
     * 
     * @param message
     * @param args
     * @return
     */
    public static String values(final String message, final Object... args) {
        return net.xy.codebasel.Debug.values(message, args);
    }
}
