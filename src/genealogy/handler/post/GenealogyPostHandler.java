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
 *  along with Genealogy.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2016
 */

package genealogy.handler.post;

import calliope.core.Base64;
import genealogy.exception.JSONParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import genealogy.handler.GenealogyHandler;
import genealogy.constants.Service;
import genealogy.constants.Params;
import genealogy.constants.GenealogyKeys;
import genealogy.exception.GenealogyException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import calliope.core.database.Connector;
import calliope.core.database.Connection;
import calliope.core.constants.Database;
import calliope.core.constants.Formats;
import calliope.core.constants.JSONKeys;
import calliope.core.exception.DbException;


/**
 * Handle posting or saving of project data
 * @author desmond
 */
public class GenealogyPostHandler extends GenealogyHandler
{
    int score;
    String review;
    String user;
    String format;
    public GenealogyPostHandler()
    {
        score = -1;
        format = Formats.MIME_MARKDOWN;
    }
    /**
     * Convert the userdata parameter into a JSON object
     * @param request the http request
     * @return the userdata object or null
     */
    protected JSONObject readUserdata( HttpServletRequest request )
    {
        String userdata = request.getParameter(Params.USERDATA);
        if ( userdata != null )
        {
            String key = "I tell a settlers tale of the old times";
            int klen = key.length();
            char[] data = Base64.decode( userdata );
            StringBuilder sb = new StringBuilder();
            for ( int i=0;i<data.length;i++ )
                sb.append((char)(data[i]^key.charAt(i%klen)));
            String json = sb.toString();
            //System.out.println("Delete: decoded json data="+json);
            return (JSONObject)JSONValue.parse(json);
        }
        else
            return null;
    }
    /**
     * Get the owner of the genealogy from the database
     * @param request the http request containing the docid
     * @return the owner's name or null
     * @throws DbException 
     */
    protected String getOwner( HttpServletRequest request ) throws DbException
    {
        Connection conn = Connector.getConnection();
        String docid = request.getParameter(Params.DOCID);
        if ( docid != null )
        {
            String shortID = simpleDocid(docid)+"/root";
            String jStr = conn.getFromDb(Database.GENEALOGY,shortID);  
            if ( jStr != null )
            {
                JSONObject jObj = (JSONObject)JSONValue.parse(jStr);
                return (String) jObj.get(JSONKeys.OWNER);
            }
            else
                return null;
        }
        else
            return null;
    }
    /**
     * Is the genealogy owner the person making the request?
     * @param request the request containing the userdata
     * @return true if the person named in userdata is the genealogy owner
     * @throws GenealogyException 
     */
    protected boolean isOwner( HttpServletRequest request ) 
        throws GenealogyException
    {
        try
        {
            JSONObject uObj = readUserdata(request);
            String owner = getOwner( request );
            if ( owner != null && uObj != null && uObj.containsKey(JSONKeys.NAME))
            {
                String name = (String)uObj.get(JSONKeys.NAME);
                return name.equals(owner);
            }
            else
                return false;
        }
        catch ( Exception e )
        {
            throw new GenealogyException(e);
        }
    }
    /**
     * Is the current user an editor
     * @param request the http request object
     * @return true if the user is an editor
     */
    protected boolean isEditor( HttpServletRequest request ) 
    {
        JSONObject uObj = readUserdata(request);
        if ( uObj != null )
        {
            JSONArray roles = (JSONArray)uObj.get(JSONKeys.ROLES);
            for ( int i=0;i<roles.size();i++ )
            {
                String role = (String)roles.get(i);
                if ( role.equals("editor") )
                    return true;
            }
        }
        return false;
    }
    public void handle( HttpServletRequest request,
        HttpServletResponse response, String urn ) throws GenealogyException
    {
        try
        {
            String service = calliope.core.Utils.first(urn);
            // DELETE not supported by all browsers, use POST
            if ( service.equals(Service.DELETE) )
            {
                urn = calliope.core.Utils.pop(urn);
                new GenealogyDeleteHandler().handle(request,response, urn);
            }
            else if ( service.equals(Service.CREATE) )
            {
                urn = calliope.core.Utils.pop(urn);
                new GenealogyCreateHandler().handle(request,response, urn);
            }
            else
            // add genealogy entry
            {
                docid = request.getParameter(Params.DOCID);
                if ( docid != null )
                {
                    Connection conn = Connector.getConnection();
                    JSONObject jObj;
                    String record = request.getParameter(Params.RECORD);
                    if ( record == null )
                        throw new Exception("Missing record");
                    else
                        jObj = (JSONObject)JSONValue.parse(record);
                    if ( jObj == null )
                        throw new JSONParseException("The json for "+docid+" was invalid");
                    if ( jObj.containsKey(JSONKeys.TYPE) 
                        && jObj.get(JSONKeys.TYPE).equals(GenealogyKeys.MARRIAGE) 
                        && jObj.containsKey(GenealogyKeys.ISROOT)
                        && ((Boolean)jObj.get(GenealogyKeys.ISROOT)) )
                    {
                        String rootId = rootDocid(docid);
                        String rootStr = conn.getFromDb(Database.GENEALOGY, rootId);
                        if ( rootStr != null )
                        {
                            JSONObject rootObj = (JSONObject)JSONValue.parse(rootStr);
                            rootObj.put(GenealogyKeys.MARRIAGE, docid);
                            conn.putToDb(Database.GENEALOGY, rootId, 
                                rootObj.toJSONString());
                        }
                    }
                    conn.putToDb(Database.GENEALOGY, docid, jObj.toJSONString());
                }
            }
        }
        catch ( Exception e )
        {
            throw new GenealogyException(e);
        }
    }
}
