package com.kiszka.prj.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Table(name="child_access_tokens")
@Entity @Data @Accessors(chain = true)
public class ChildAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    private int id;
    @Column(name = "pin", nullable = false)
    private String pin;
    @Column(name = "qr_hash", nullable = false)
    private String qrHash;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kid_id", nullable = false)
    private Kid kid;
}