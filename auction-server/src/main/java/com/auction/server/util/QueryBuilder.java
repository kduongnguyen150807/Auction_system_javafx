package com.auction.server.util;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {
    private StringBuilder query = new StringBuilder();
    private List<Object> parameters = new ArrayList<>();

    public QueryBuilder select(String Collumns){
        query.append("SELLECT ").append(Collumns).append(" ");
        return this;
    }

    public QueryBuilder from(String table){
        query.append("FROM ").append(table).append(" ");
        return this;
    }

    public QueryBuilder where(String condition, Object value){
        if(query.toString().contains("WHERE")){
            query.append("AND ");
        }else{
            query.append("WHERE ");
        }
        query.append(condition).append(" ? ");
        parameters.add(value);
        return this;
    }

    public QueryBuilder whereRaw(String condition) {
        if (query.toString().contains("WHERE")) {
            query.append("AND ");
        } else {
            query.append("WHERE ");
        }
        query.append(condition).append(" ");
        return this;
    }

    public String build() {
        return query.toString().trim();
    }

    public List<Object> getParameters() {
        return parameters;
    }

}

