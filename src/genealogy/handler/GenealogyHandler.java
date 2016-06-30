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
package genealogy.handler;
import genealogy.exception.GenealogyException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract super-class for all handlers: PUT, POST, DELETE, GET
 * @author ddos
 */
abstract public class GenealogyHandler 
{
    protected String docid;
    protected String simpleDocid( String longDocId )
    {
        String[] parts = longDocId.split("/");
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<2&&i<parts.length;i++ )
        {
            if ( sb.length()>0 )
                sb.append("/");
            sb.append(parts[i]);
        }
        return sb.toString();
    }
    protected String rootDocid( String longDocId )
    {
        return simpleDocid(longDocId)+"/root";
    }
    public GenealogyHandler()
    {
    }
    public abstract void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws GenealogyException;
}
