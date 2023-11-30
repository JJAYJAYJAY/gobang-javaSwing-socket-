package eneity;

import java.io.Serializable;

public class Message implements Serializable {
    public String type;
    public Object value;
    public Object[] values;

    public Message(String type,Object value) {
        this.type=type;
        this.value = value;
    }

    public Message(String type) {
        this.type = type;
    }
    public Message(String type,Object[] values){
        this.type=type;
        this.values = values;
    }
}
