package com.wac.autocore.security;

import com.wac.autocore.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecurityProxyTest {

    interface Demo {
        @RequiresRole
        String read();

        @RequiresRole({UserRole.ADMIN, UserRole.MECHANIC})
        String write();

        String unannotated();

        @RequiresRole
        void fail();
    }

    static class DemoImpl implements Demo {
        public String read() { return "read"; }
        public String write() { return "write"; }
        public String unannotated() { return "unannotated"; }
        public void fail() { throw new IllegalStateException("boom"); }
    }

    private final Demo demo = SecurityProxy.protect(Demo.class, new DemoImpl());

    @AfterEach
    void clear() {
        SecurityContext.clear();
    }

    private static void login(UserRole... roles) {
        SecurityContext.setCurrentUser(new AuthUser("test", Set.of(roles)));
    }

    @Test
    void notLoggedInIsDenied() {
        assertThrows(AccessDeniedException.class, demo::read);
    }

    @Test
    void loggedInCanCallAuthenticatedMethod() {
        login(UserRole.READ_ONLY);
        assertEquals("read", demo.read());
    }

    @Test
    void missingRoleIsDenied() {
        login(UserRole.READ_ONLY);
        assertThrows(AccessDeniedException.class, demo::write);
    }

    @Test
    void anyOfTheRolesIsEnough() {
        login(UserRole.MECHANIC);
        assertEquals("write", demo.write());
    }

    @Test
    void methodWithoutAnnotationIsDenied() {
        login(UserRole.ADMIN);
        assertThrows(AccessDeniedException.class, demo::unannotated);
    }

    @Test
    void targetExceptionIsUnwrapped() {
        login(UserRole.ADMIN);
        IllegalStateException e = assertThrows(IllegalStateException.class, demo::fail);
        assertEquals("boom", e.getMessage());
    }

    @Test
    void objectMethodsWorkWithoutLogin() {
        assertNotNull(demo.toString());
    }
}
