package com.projectw.domain.store.entity;

import com.projectw.common.entity.Timestamped;
import com.projectw.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class StoreLike extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private Boolean storeLike = true;

    public StoreLike(User user, Store store) {
        this.user = user;
        this.store = store;
    }

    public void changeLike() {
        this.storeLike = !this.storeLike;
    }
}
