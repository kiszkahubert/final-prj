package com.kiszka.prj.configs;

import lombok.Getter;

import java.security.Principal;

@Getter
public class KidPrincipal implements Principal {
    private final Integer kidId;

    public KidPrincipal(Integer kidId){
        this.kidId = kidId;
    }
    @Override
    public String getName() {
        return String.valueOf(this.kidId);
    }
}
