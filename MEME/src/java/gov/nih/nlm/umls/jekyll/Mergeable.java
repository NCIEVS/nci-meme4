/*
 * Mergeable.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.common.Concept;

/**
 * @see <a href="src/Mergeable.java.html">source </a>
 */
public interface Mergeable {

    public Concept getSourceConcept();

    public Concept getTargetConcept();

    public void removeSourceConcept() throws Exception;
}