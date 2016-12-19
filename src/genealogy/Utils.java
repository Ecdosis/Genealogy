/*
 * This file is part of Genealogy.
 *
 *  Genealogy is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Genealogy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Genealogy. If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2016
 */
package genealogy;

/**
 * Some utility functions to generate ids
 * @author desmond
 */
public class Utils {
    /**
     * Generate a marriage id from the bride and groom's names
     * @param docid the docid
     * @param groom a full name including surname
     * @param bride a full name including surname
     * @return an id consisting of the docid + groom + bride
     */
    public static String makeMarriageId( String docid, String groom, String bride )
    {
        StringBuilder sb = new StringBuilder();
        sb.append(docid);
        sb.append("/");
        String modGroom = groom.replaceAll(" ", "_");
        sb.append( modGroom.replaceAll("'","") );
        sb.append("_");
        String modBride = bride.replaceAll(" ","_");
        sb.append( modBride.replaceAll("'","") );
        return sb.toString().toLowerCase();
    }
    /**
     * Generate a spouse id from the bride or groom's name
     * @param docid the docid
     * @param name the full name including surname
     * @return an id consisting of the docid + spouse name
     */
    public static String makePersonId( String docid, String name )
    {
        StringBuilder sb = new StringBuilder();
        sb.append(docid);
        sb.append("/");
        String modPerson = name.replaceAll(" ", "_");
        sb.append( modPerson.replaceAll("'","") );
        return sb.toString().toLowerCase();
    }
}
