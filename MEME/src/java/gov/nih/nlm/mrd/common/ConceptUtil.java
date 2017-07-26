package gov.nih.nlm.mrd.common;

import gov.nih.nlm.meme.common.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for the client.
 * 
 * @author MRD Group
 */
public class ConceptUtil {

	public ConceptUtil() {
	}

	public List getAttrList(String attributeName, MRDAttribute[] mrdAttributeArray) {
		List attributeList = new ArrayList();

		MRDAttribute mrdAttribute;

		for (int i = 0; mrdAttributeArray != null && i < mrdAttributeArray.length; i++) {

			mrdAttribute = mrdAttributeArray[i];

			if (mrdAttribute.getAName().equalsIgnoreCase(attributeName)) {
				attributeList.add(mrdAttribute);
			}
		}

		return attributeList;
	}

	public List getRelList(List mrdRelationshipsList, String relName,
			boolean inverseFlag) {

		List relList = new ArrayList();

		MRDRelationship mrdRelationship;

		for (int i = 0; mrdRelationshipsList != null
				&& i < mrdRelationshipsList.size(); i++) {

			mrdRelationship = (MRDRelationship) mrdRelationshipsList.get(i);
			Boolean iFlag = new Boolean(mrdRelationship.isInverse_flag());

			if (iFlag.equals(inverseFlag)
					&& mrdRelationship.getRelationship_name().equalsIgnoreCase(relName)) {
				relList.add(mrdRelationship);
			}
		}
		return relList;
	}

	public MRDAttribute getAttribute(String attributeName,
			MRDAttribute[] mrdAttributeArray) {

		MRDAttribute mrdAttribute = null;

		for (int i = 0; mrdAttributeArray != null && i < mrdAttributeArray.length; i++) {

			mrdAttribute = mrdAttributeArray[i];

			if (mrdAttribute.getAName().equalsIgnoreCase(attributeName)) {
				return mrdAttribute;
			}
		}

		return mrdAttribute;
	}
}
