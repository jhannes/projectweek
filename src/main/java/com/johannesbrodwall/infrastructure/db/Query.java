package com.johannesbrodwall.infrastructure.db;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Query {

    @Getter
    private Object[] parameters;

    public Query(String field, Object value) {
        this.parameters = new Object[] { value };
    }

    public String getWhereClause(String... supportedFields) {
        boolean first = true;

        String result = "";
        for (String field : supportedFields) {
            if (!first) result += " AND ";
            first = false;
            result += field + " = ?";
        }

        return result;
    }

}
