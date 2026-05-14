package com.auction.server.dao.transaction;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionCallback<T> {
  T doInTransaction(Connection conn) throws Exception;
}