package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.MappingClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.MapSet;
import gov.nih.nlm.meme.common.Mapping;
import gov.nih.nlm.meme.exception.MEMEException;

import java.util.ArrayList;
import java.util.Iterator;

public class MappingBean
    extends ClientBean {

  private MappingClient mc = null;
  private AuxiliaryDataClient auxc = null;

  /**
   * This is a no-argument constructor that call the super class.
   * @throws MEMEException if failed to construct this class.
   */
  public MappingBean() throws MEMEException {
    super();
    mc = new MappingClient();
    mc.setAuthority(new Authority.Default("MTH"));
    auxc = new AuxiliaryDataClient();
  }

  /**
   * Returns the instance of mapping client.
   * @return An object {@link MappingClient}.
   */
  public MappingClient getMappingClient() {
    configureClient(mc);
    mc.setMidService(getMidService());
    return mc;
  }

  /**
   * Returns the instance of auxiliary data client.
   * @return An object {@link AuxiliaryDataClient}.
   */
  public AuxiliaryDataClient getAuxiliaryDataClient() {
    configureClient(auxc);
    auxc.setMidService(getMidService());
    return auxc;
  }

  public ArrayList getMapSetNames() throws MEMEException{
    ArrayList names = new ArrayList();
    for(Iterator iter = getMapSets().iterator(); iter.hasNext();) {
      names.add(((MapSet)iter.next()).getName());
    }
    return names;
  }

  public MapSet getMapSet(int concept_id) throws MEMEException{
    return getMappingClient().getMapSetWithoutMappings(concept_id);
  }

  public ArrayList getMappings(MapSet mapset, int start, int end) throws MEMEException{
    Mapping[] mappings = getMappingClient().getMappings(mapset, start, end);
    ArrayList list = new ArrayList();
    for(int i=0; i<mappings.length; i++) {
      list.add(mappings[i]);
    }
    return list;
  }

  public int getMappingCount(MapSet mapset) throws MEMEException {
    return getMappingClient().getMappingCount(mapset);
  }

  public ArrayList getMapSets() throws MEMEException {
    MapSet[] maps = getMappingClient().getMapSets();
    ArrayList mapsets = new ArrayList(maps.length);
    mapsets = new ArrayList(maps.length);
    for(int i=0; i<maps.length; i++){
      mapsets.add(maps[i]);
    }
    return mapsets;
  }

  /**
   * Returns "SELECTED" if the specified host matches the client bean host.
   * @param host the server host name.
   * @return a flag indicating whether or not an HTML option element should
   * be selected.
   */
  public String getHostSelectedFlag(String host) {
    if (host.equals(getHost()))
      return "SELECTED";
    else return "";
  }

}
