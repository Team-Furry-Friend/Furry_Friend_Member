package com.v3.furry_friend_member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FurryFriendMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(FurryFriendMemberApplication.class, args);
    }

}
