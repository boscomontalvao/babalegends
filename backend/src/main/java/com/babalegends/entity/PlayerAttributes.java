package com.babalegends.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_player_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlayerAttributes {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(name = "player_attribute_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "card_picture")
    private String cardPicture;

    @Column(nullable = false)
    private Short overall;

    @Column(nullable = false)
    private Short pace;

    @Column(nullable = false)
    private Short shooting;

    @Column(nullable = false)
    private Short passing;

    @Column(nullable = false)
    private Short dribbling;

    @Column(nullable = false)
    private Short defending;

    @Column(nullable = false)
    private Short physicality;
}
