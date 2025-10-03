package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.ChildAccessTokenDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.ChildAccessTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class ChildAccessTokenController {
    private final ChildAccessTokenService childAccessTokenService;

    public ChildAccessTokenController(ChildAccessTokenService childAccessTokenService){
        this.childAccessTokenService = childAccessTokenService;
    }
    @GetMapping()
    public List<ChildAccessTokenDTO> getTokensForParent(Authentication authentication){
        Parent parent = (Parent) authentication.getPrincipal();
        return childAccessTokenService.getTokensForParent(parent.getId());
    }
}
