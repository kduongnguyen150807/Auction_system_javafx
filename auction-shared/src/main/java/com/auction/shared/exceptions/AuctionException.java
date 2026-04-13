package com.auction.shared.exceptions;

import java.io.Serializable;
public class AuctionException extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;
    public AuctionException() {
        super();
    }
    public AuctionException(String message) {
        super(message);
    }
}