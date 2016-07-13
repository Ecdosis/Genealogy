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
package genealogy.handler.get;

import calliope.core.database.Connector;
import calliope.core.database.Connection;
import calliope.core.constants.JSONKeys;
import calliope.core.constants.Database;
import genealogy.constants.Params;
import genealogy.constants.GenealogyKeys;
import genealogy.exception.*;
import genealogy.handler.GenealogyHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import java.util.HashMap;

/**
 * Get a project document from the database
 * @author desmond
 */
public class GenealogyGetHandler extends GenealogyHandler
{
    /**
     * The root marriage
     */
    JSONObject root;
    HashMap<String,JSONObject>  persons;
    HashMap<String,JSONObject>  marriages;
    JSONObject makeChild( String childId ) throws Exception
    {
        JSONObject child = persons.get( childId );
        JSONObject newChild = new JSONObject();
        if ( child != null )
        {
            String childName = (String)child.get(JSONKeys.NAME);
            if ( childName == null )
                System.out.println("childName for "+childId+" is absent");
            else
                newChild.put( JSONKeys.NAME, childName );
            newChild.put( JSONKeys.TYPE, GenealogyKeys.PERSON);
            if ( child.containsKey(GenealogyKeys.BORN) )
                newChild.put( GenealogyKeys.BORN, child.get(GenealogyKeys.BORN));
            if ( child.containsKey(GenealogyKeys.NEE) )
                newChild.put( GenealogyKeys.NEE, child.get(GenealogyKeys.NEE));
            if ( child.containsKey(GenealogyKeys.DIED) )
                newChild.put( GenealogyKeys.DIED, child.get(GenealogyKeys.DIED));
            if ( child.containsKey(GenealogyKeys.MARRIAGES) )
            {
                JSONArray weddings = (JSONArray)child.get(GenealogyKeys.MARRIAGES);
                JSONArray newWeddings = new JSONArray();
                String simpleId = simpleDocid(docid);
                for ( int i=0;i<weddings.size();i++ )
                {
                    String spouse = (String)weddings.get(i);
                    String id1 = genealogy.Utils.makeMarriageId(simpleId,childName,spouse);
                    String id2 = genealogy.Utils.makeMarriageId(simpleId,spouse,childName);
                    JSONObject marriage = null;
                    if ( marriages.containsKey(id1) )
                        marriage = marriages.get(id1);
                    else if ( marriages.containsKey(id2) )
                        marriage = marriages.get(id2);
                    if ( marriage != null )
                    {
                        JSONObject newMarriage = new JSONObject();
                        makeMarriage( newMarriage, marriage );
                        newWeddings.add(newMarriage);
                    }
                    else
                        throw new Exception( "marriage between "
                            +spouse+" and "+childName+" not found");
                }
                newChild.put( GenealogyKeys.MARRIAGES, newWeddings );
            }
        }
        else
            System.out.println("Child "+childId+" missing");
        return newChild;
    }
    void addSpouse( String key, JSONObject parent, JSONObject marriage )
    {
        String simpleId = simpleDocid(docid);
        String spouse = (String)marriage.get(key);
        String spouseId = genealogy.Utils.makePersonId(simpleId,spouse);
        JSONObject spouseObj = (JSONObject) persons.get(spouseId);
        if ( spouseObj != null )
        {
            JSONObject copy = new JSONObject(spouseObj);
            copy.remove(JSONKeys.DOCID);
            if ( copy.containsKey(GenealogyKeys.MARRIAGES) )
                copy.remove(GenealogyKeys.MARRIAGES);
            parent.put( key, copy );
        }
        else
            System.out.println("Couldn't find "+key+" "+spouseId);
    }
    void makeMarriage( JSONObject parent, JSONObject marriage ) throws Exception
    {
        String simpleId = simpleDocid(docid);
        String type = (String)marriage.get(JSONKeys.TYPE);
        parent.put(JSONKeys.TYPE, type);
        if ( type.equals("defacto") )
            System.out.println("defacto!");
        addSpouse( GenealogyKeys.GROOM, parent, marriage );
        addSpouse( GenealogyKeys.BRIDE, parent, marriage );
        JSONArray ceremonyArr = (JSONArray)marriage.get(GenealogyKeys.CEREMONIES);
        if ( ceremonyArr != null )
            parent.put( GenealogyKeys.CEREMONIES, ceremonyArr );
        JSONArray children = (JSONArray)marriage.get(GenealogyKeys.CHILDREN);
        if ( children != null )
        {
            JSONArray kids = new JSONArray();
            parent.put(GenealogyKeys.CHILDREN,kids);
            for ( int i=0;i<children.size();i++ )
            {
                String childId = genealogy.Utils.makePersonId(simpleId,(String)children.get(i));
                kids.add( makeChild(childId) );
            }
        }
    }
    JSONObject buildTree() throws Exception
    {
        JSONObject tree = new JSONObject();
        if ( root != null )
        {
            makeMarriage( tree, root );
        }
        return tree;
    }
    /**
     * Handle a Get. Reconstruct the entire genealogy
     * @param request the http request with parameters
     * @param response the response object to write the genealogy to
     * @param urn the urn should be empty but is ignored
     * @throws GenealogyException 
     */
    public void handle(HttpServletRequest request,
            HttpServletResponse response, String urn) throws GenealogyException 
    {
        try 
        {
            if ( urn.length()== 0 )
            {
                docid = request.getParameter(Params.DOCID);
                Connection conn = Connector.getConnection();
                String[] docids = conn.listDocuments(Database.GENEALOGY, 
                    docid+"/.*", JSONKeys.DOCID);
                persons = new HashMap<String,JSONObject>();
                marriages = new HashMap<String,JSONObject>();
                // create person and marriage registries
                String rootId = rootDocid(docid);
                for ( int i=0;i<docids.length;i++ )
                {
                    if ( !docids[i].equals(rootId) )
                    {
                        String jStr = conn.getFromDb(Database.GENEALOGY, docids[i] );
                        JSONObject jObj = (JSONObject)JSONValue.parse(jStr);
                        jObj.remove(JSONKeys._ID);
                        if ( jObj.containsKey(JSONKeys.TYPE) )
                        {
                            if ( jObj.get(JSONKeys.TYPE).equals(GenealogyKeys.PERSON) )
                                persons.put( (String)jObj.get(JSONKeys.DOCID), jObj);
                            else
                                marriages.put((String)jObj.get(JSONKeys.DOCID),jObj);
                            //jObj.remove(JSONKeys.TYPE);
                        }
                        else
                            System.out.println("Record "+docids[i]+" lacking type field");
                    }
                }
                // find root of the family tree
                String rootRec = conn.getFromDb(Database.GENEALOGY,rootId);
                if ( rootRec != null )
                {
                    JSONObject rootObj = (JSONObject)JSONValue.parse( rootRec );
                    if ( rootObj.containsKey(GenealogyKeys.MARRIAGE) )
                        root = marriages.get((String)rootObj.get(GenealogyKeys.MARRIAGE));
                }
                JSONObject tree = buildTree();
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(tree.toJSONString());
            }
            else
                throw new Exception("Unknown GET service "+urn+"\n");
        } 
        catch (Exception e) 
        {
            try
            {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(e.getMessage());
            }
            catch ( Exception ex )
            {
                throw new GenealogyException(ex);
            }
        }
    }   
}
