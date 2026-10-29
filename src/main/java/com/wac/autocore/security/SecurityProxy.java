package com.wac.autocore.security;

import com.wac.autocore.model.enums.UserRole;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Ersätter @PreAuthorize: slår in en service bakom ett interface och kontrollerar
 * @RequiresRole mot SecurityContext före varje anrop.
 * Annotationen läses från interfacet, så det spelar ingen roll om target är en
 * CGLIB-proxy från @Transactional. Metoder utan @RequiresRole nekas (fail closed).
 */
public final class SecurityProxy {

    private SecurityProxy() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T protect(Class<T> iface, T target) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface.getName() + " is not an interface");
        }
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() != Object.class) {
                        checkAccess(iface, method);
                    }
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    private static void checkAccess(Class<?> iface, Method method) {
        RequiresRole rule = method.getAnnotation(RequiresRole.class);
        if (rule == null) {
            rule = iface.getAnnotation(RequiresRole.class);
        }
        if (rule == null) {
            throw new AccessDeniedException("No @RequiresRole on " + iface.getSimpleName() + "." + method.getName());
        }

        AuthUser user = SecurityContext.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Not logged in: " + method.getName()));

        UserRole[] required = rule.value();
        if (required.length > 0 && !user.hasAnyRole(required)) {
            throw new AccessDeniedException("Access denied: " + method.getName());
        }
    }
}
