package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;

public class SourceDifference {

  private String name;
  private Value[] newValues;
  private Value[] oldValues;

  public String getName() {
    return name;
  }

  public int getCount() {
    return newValues.length - oldValues.length;
  }

  public Value[] getNewValues() {
    return newValues;
  }

  public Value[] getOldValues() {
    return oldValues;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNewValues(Value[] newValues) {
    Arrays.sort(newValues, new Comparator(){
      public int compare(Object o1, Object o2) {
        if (o1 == o2 ) {
          return 0;
        }
        if(o1 == null) {
          return -1;
        }
        if(o2 == null) {
          return 1;
        }
        Value v1 = (Value) o1;
        Value v2 = (Value) o2;
        if ( v1.getValue() == v2.getValue() ) {
          return 0;
        }
        if(v1.getValue() == null) {
          return -1;
        }
        if(v2.getValue() == null) {
          return 1;
        }
        return v1.getValue().compareTo(v2.getValue());
      }
    });
    this.newValues = newValues;
  }

  public void setOldValues(Value[] oldValues) {
    Arrays.sort(oldValues, new Comparator(){
      public int compare(Object o1, Object o2) {
        if (o1 == o2 ) {
          return 0;
        }
        if(o1 == null) {
          return -1;
        }
        if(o2 == null) {
          return 1;
        }
        Value v1 = (Value) o1;
        Value v2 = (Value) o2;
        if ( v1.getValue() == v2.getValue() ) {
          return 0;
        }
        if(v1.getValue() == null) {
          return -1;
        }
        if(v2.getValue() == null) {
          return 1;
        }
        return v1.getValue().compareTo(v2.getValue());
      }
    });
    this.oldValues = oldValues;
  }

  public static class Value{
    private String value;
    private Map counts;
    private ArrayList sourceAbbreviations;


    public String getValue() {
      return value;
    }

    public int getCount(String sourceAbbreviation) {
      return ((Integer)counts.get(sourceAbbreviation)).intValue();
    }

    /**
     * Value
     */
    public Value() {
      sourceAbbreviations = new ArrayList();
      counts = new HashMap();
    }

    public String[] getSourceAbbreviations() {
      return (String[])sourceAbbreviations.toArray(new String[0]);
    }

    public void setValue(String value) {
      this.value = value;
    }

    public void setCount(String sourceAbbreviation, int count) {
      counts.put(sourceAbbreviation,new Integer(count));
    }

    public void setSourceAbbreviation(String sourceAbbreviation) {
      sourceAbbreviations.add(sourceAbbreviation);
    }

  }
}
