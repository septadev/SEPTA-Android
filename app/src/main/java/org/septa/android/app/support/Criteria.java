package org.septa.android.app.support;

public class Criteria {

    private Operation operation;
    private String fieldName;
    private Object value;


    public Criteria(String fieldName, Operation operation, Object value) {
        this.operation = operation;
        this.fieldName = fieldName;
        this.value = value;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    public enum Operation {
        EQ("="), NE("<>"), GT(">"), LT("<"), GTE(">="), LTE("<=");

        private String opText;


        Operation(String opText) {
            this.opText = opText;
        }

        public String getOpText() {
            return opText;
        }

        public String toString() {
            return opText;
        }
    }


}
