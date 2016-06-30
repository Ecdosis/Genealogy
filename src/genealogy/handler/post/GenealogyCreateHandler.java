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
import calliope.core.database.Connector;
import calliope.core.database.Connection;
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import genealogy.exception.GenealogyException;
import genealogy.constants.Params;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
/**
 * Create a new genealogy
 * @author desmond
 */
public class GenealogyCreateHandler extends GenealogyPostHandler
{
    public void handle( HttpServletRequest request,
        HttpServletResponse response, String urn ) throws GenealogyException
    {
        try
        {
            if ( isEditor(request) )
            {
                docid = request.getParameter(Params.DOCID);
                if ( docid != null )
                {
                    String rootId = rootDocid(docid);
                    if ( docid.equals(rootId) )
                    {
                        Connection conn = Connector.getConnection();
                        String jStr = conn.getFromDb(Database.GENEALOGY, docid );
                        if ( jStr != null )
                            throw new Exception("Genealogy already present "+docid);
                        else
                        {
                            JSONObject jObj = new JSONObject();
                            JSONObject uObj = readUserdata(request);
                            if ( uObj != null )
                            {
                                jObj.put(JSONKeys.OWNER,uObj.get(JSONKeys.NAME));
                                conn.putToDb(Database.GENEALOGY, 
                                    docid, jObj.toJSONString() );
                            }
                        }
                    }
                    else
                        throw new Exception("Docid not simple "+docid );
                }
                else throw new Exception("Missing docid");
            }
            else
                throw new GenealogyException("Not authorised");
        }
        catch ( Exception e )
        {
            throw new GenealogyException(e);
        }
    }
}
