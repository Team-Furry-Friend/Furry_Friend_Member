package com.v3.furry_friend_member.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "roleSet")
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mid;

    private String mpw;

    //위의 아이디는 고유 번호이고 email을 아이디로 쓸 예정
    private String email;

    private String name;

    private String address;

    private String phone;
    
    //삭제 여부
    private boolean del;

    //로그인 정보
    private boolean social;

    //권한 -여러 개의 권한을 소유
    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private Set<MemberRole> roleSet = new HashSet<>();

    public void changePassword(String mpw){
        this.mpw = mpw;
    }

    //권한 추가
    public void addRole(MemberRole memberRole){
        this.roleSet.add(memberRole);
    }
}
