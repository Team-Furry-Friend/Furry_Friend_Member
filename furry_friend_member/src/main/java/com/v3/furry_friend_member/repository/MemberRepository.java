package com.v3.furry_friend_member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.v3.furry_friend_member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

        //mid를 매개변수로 받아서
        //social의 값이 false인 데이터를 전부 찾아오는 메서드
        // @Query("select m from Member m where m.email = :email and m.social = false")
        // Optional<Member> getWithRoles(String email);

        @EntityGraph(attributePaths = "roleSet", type = EntityGraph.EntityGraphType.LOAD)
        @Query("select m from Member m where m.email = :email")
        Optional<Member> findByEmail(@Param("email") String email);

        //이메일 중복확인
        boolean existsByEmail(String email);
        
        // 사용자 찾기
        Member findByMid(Long mid);
}

