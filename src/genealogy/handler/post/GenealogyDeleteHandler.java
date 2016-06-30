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
package genealogy.handler.post;
import genealogy.exception.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.core.database.*;
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import genealogy.constants.Params;

/**
 * Handle a DELETE request
 * @author desmond
 */
public class GenealogyDeleteHandler extends GenealogyPostHandler
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws GenealogyException
    {
        try
        {
            docid = request.getParameter(Params.DOCID);
            if ( docid != null )
            {
                String shortId = simpleDocid(docid);
                if ( isOwner(request) )
                {
                    Connection conn = Connector.getConnection();
                    // check if we are removing the entire genealogy
                    if ( docid.equals(rootDocid(docid)) )
                        conn.removeFromDbByExpr(Database.GENEALOGY, 
                            JSONKeys.DOCID, shortId+"/.*");
                    else // remove specific resource
                        conn.removeFromDb(Database.GENEALOGY, docid);
                }
            }
        }
        catch ( Exception e )
        {
            throw new GenealogyException(e);
        }
    }
}
