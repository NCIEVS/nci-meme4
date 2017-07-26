/**
 * Transferable.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.common.Concept;

/**
 * The class that is interested in transferring concept(s) to the SEL screen
 * should implement this interface.
 * 
 * @see <a href="src/Transferable.java.html">source </a>
 */
public interface Transferable {

    public Concept[] getConcepts();

    public int[] getConceptIds();
}