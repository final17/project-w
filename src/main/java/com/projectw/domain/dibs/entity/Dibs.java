package com.projectw.domain.dibs.entity;

import com.projectw.domain.store.entity.Store;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Dibs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    public Dibs(User user, Store store) {
        this.user = user;
        this.store = store;
    }
}
