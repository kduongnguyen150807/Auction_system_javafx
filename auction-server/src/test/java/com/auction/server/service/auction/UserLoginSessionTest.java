package com.auction.server.service.auction;

import com.auction.server.dao.user.UserDao;
import com.auction.shared.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserLoginSessionTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {

        userDao = new UserDao();

        User admin =
                userDao.getByUsername("admin");

        if (admin != null) {

            userDao.clearSessionToken(
                    admin.getId()
            );
        }
    }

    @Test
    void testLoginSuccess() {

        User user =
                userDao.login(
                        "admin",
                        "123456"
                );

        assertNotNull(user);

        assertEquals(
                "admin",
                user.getUsername()
        );

        assertNotNull(
                user.getSessionToken()
        );
    }

    @Test
    void testSingleDeviceLogin() {

        User firstLogin =
                userDao.login(
                        "admin",
                        "123456"
                );

        assertNotNull(firstLogin);

        User secondLogin =
                userDao.login(
                        "admin",
                        "123456"
                );

        assertNull(secondLogin);
    }

    @Test
    void testLogoutThenLoginAgain() {

        User user =
                userDao.login(
                        "admin",
                        "123456"
                );

        assertNotNull(user);

        boolean logout =
                userDao.clearSessionToken(
                        user.getId()
                );

        assertTrue(logout);

        User loginAgain =
                userDao.login(
                        "admin",
                        "123456"
                );

        assertNotNull(loginAgain);
    }
}