/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  NotPersistentIfTransient
 *
 *****************************************************************************/

package gov.nih.nlm.meme.xml;

/**
 * Our XML serialization mechanism does not implicitly ignore transient
 * information (because it causes problems with some JFC classes).
 * An XMLSerliazer such as {@link ObjectXMLSerializer} should avoid serializing
 * any objects that are both transient instance variables and implementations
 * of this interface.
 *
 * @author MEME Group
 */

public interface NotPersistentIfTransient {

}
