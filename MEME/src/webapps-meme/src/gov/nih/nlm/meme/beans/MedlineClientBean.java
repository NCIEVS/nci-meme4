package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.client.MedlineClient;
import gov.nih.nlm.meme.common.StageStatus;
/*****************************************************************************
*
* Package: gov.nih.nlm.meme.beans
* Object:  MedlineClientBean
*
* Changes:
*   04/28/2006 TTN (1-77HMD): Added MedlineService to process Medline data
*
*****************************************************************************/

public class MedlineClientBean extends ClientBean {
    private MedlineClient medlineClient;
    /**
     * MedlineClientBean
     * @throws MEMEException if failed to construct this class.
     */
    public MedlineClientBean() throws MEMEException {
        super();
        medlineClient = new MedlineClient();
    }

    public MedlineClient getMedlineClient() {
        configureClient(medlineClient);
        medlineClient.setMidService(getMidService());
        return medlineClient;
    }

    public StageStatus[] getStageStatus() throws MEMEException {
        return getMedlineClient().getMedlineStatus();
    }

    public StageStatus getStageStatus(int index) throws MEMEException {
        return getMedlineClient().getMedlineStatus()[index];
    }
}
