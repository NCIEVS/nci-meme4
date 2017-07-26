/*****************************************************************************
 *
 * Package:    com.lexical.meme.recipe;
 * Class:      RxEvent.java
 * 
 * Author:     OJC,BAC (4/1999)
 *
 *****************************************************************************/
package gov.nih.nlm.recipe;

/**
 *
 * This class is a recipe event.
 *
 * @author: Brian Carlsen (4/1999)
 * @version: 1.0
 *
 **/

import java.util.EventObject;

public class RxEvent extends EventObject {

    public RxEvent(Object o) {
        super(o);
    }

}

