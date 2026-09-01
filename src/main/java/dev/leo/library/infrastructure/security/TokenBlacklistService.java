package dev.leo.library.infrastructure.security;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    public void blacklist(String jti) { blacklist.add(jti); }
    public boolean isBlacklisted(String jti) { return blacklist.contains(jti); }
}
