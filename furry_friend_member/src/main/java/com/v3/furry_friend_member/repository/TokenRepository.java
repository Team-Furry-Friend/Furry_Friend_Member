package com.v3.furry_friend_member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.v3.furry_friend_member.entity.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Transactional
    void deleteTokenByUserId(Long memberId);

}
