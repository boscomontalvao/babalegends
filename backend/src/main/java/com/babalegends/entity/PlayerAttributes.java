package com.babalegends.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Short overall = 50;

    @Column(nullable = false)
    private Short pace = 50;

    @Column(nullable = false)
    private Short shooting = 50;

    @Column(nullable = false)
    private Short passing = 50;

    @Column(nullable = false)
    private Short dribbling = 50;

    @Column(nullable = false)
    private Short defending = 50;

    @Column(nullable = false)
    private Short physicality = 50;
}
