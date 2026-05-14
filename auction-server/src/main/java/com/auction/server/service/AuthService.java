package com.auction.server.service;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.transaction.TransactionManager;
import com.auction.server.repository.UserRepository;
import com.auction.shared.dto.LoginCredentials;
import com.auction.shared.dto.RegisterCredentials;
import com.auction.shared.linkv2.Response;
import com.auction.shared.user.User;
import com.auction.shared.user.UserStatus;

public class AuthService {
  private final UserRepository userRepository;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Response<User> login(String requestId, LoginCredentials loginCredentials, HandlerContext handlerContext) {
    User user = TransactionManager.read(connection -> {
      return userRepository.login(loginCredentials.getUsername(), loginCredentials.getPassword(), connection);
    });
    if (user == null) {
      return Response.error(requestId, "Invalid password or username");
    }

    if (user.getStatus() == UserStatus.LOCKED) {
      return Response.error(requestId, "You are not allowed to login");
    }

    handlerContext.setUser(user);
    return Response.success(requestId, "success", user);
  }

  public Response<User> register(String requestId, RegisterCredentials credentials, HandlerContext handlerContext) {
    User user = TransactionManager.execute(conn -> {
      userRepository.register(
        credentials.getUsername(),
        credentials.getPassword(),
        credentials.getEmail(),
        credentials.getAge(),
        conn);
      return userRepository.login(credentials.getUsername(), credentials.getPassword(), conn);
    });

    if (user == null) {
      return Response.error(requestId, "Failed to register user");
    } else {
      handlerContext.setUser(user);
      return Response.success(requestId, "success", user);
    }
  }
}
