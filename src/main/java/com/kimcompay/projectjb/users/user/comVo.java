package com.kimcompay.projectjb.users.user;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "companys")
@Entity
public class comVo {

    @Id 
    @Column(name = "cid",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cid;

    @Column(name = "cemail",nullable = false,unique = true,length = 50)
    private String cemail;

    @Column(name = "cpwd",nullable = false ,length = 200)
    private String cpwd;

    @Column(name = "caddress",nullable = false ,length = 50)
    private String caddress;

    @Column(name = "cdetail_address",nullable = false ,length = 50)
    private String cdetail_address;

    @Column(name = "cpostcode",nullable = false ,columnDefinition = "TINYINT")
    private int cpostcode;

    @Column(name = "cphone",nullable = false ,unique = true,length = 11)
    private String cphone;

    @Column(name = "ctel",nullable = false ,unique = true,length = 11)
    private String ctel;

    @Column(name = "clogin_date")
    private Timestamp clogin_date;

    @Column(name = "ccreated")
    @CreationTimestamp
    private Timestamp ccreated;

    @Column(name = "csleep",nullable = false ,columnDefinition = "TINYINT")
    private int csleep;

    @Column(name = "cnum",nullable = false,unique = true)
    private String cnum;

    @Column(name = "ckind",nullable = false,columnDefinition = "TINYINT")
    private int ckind;

    @Column(name = "start_time",nullable = false,columnDefinition = "TINYINT")
    private int start_time;

    @Column(name = "end_time",nullable = false,columnDefinition = "TINYINT")
    private int end_time;

}