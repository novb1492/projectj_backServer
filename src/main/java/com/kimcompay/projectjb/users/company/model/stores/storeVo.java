package com.kimcompay.projectjb.users.company.model.stores;

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
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "stores")
@Entity
public class storeVo {
    
    @Id 
    @Column(name = "store_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sid;
 
    @Column(name = "company_id",nullable = false)
    private int cid;

    @Column(name = "store_address",nullable = false ,length = 50)
    private String saddress;

    @Column(name = "store_detail_address",nullable = false ,length = 50)
    private String sdetail_address;

    @Column(name = "store_road_address",length = 50)
    private String roadAddress;

    @Column(name = "store_postcode",nullable = false,length = 20 )
    private String spostcode;

    @Column(name = "store_phone",nullable = false ,length = 11)
    private String sphone;

    @Column(name = "store_tel",nullable = false ,length = 11)
    private String stel;

    @Column(name = "store_name",nullable = false ,length = 50)
    private String sname;
    
    @Column(name = "store_num",nullable = false ,length = 50)
    private String snum;

    @Column(name = "thumb_nail",nullable = false ,length = 255)
    private String simg;


    @Column(name = "openTime",nullable = false ,length = 20)
    private String openTime;

    @Column(name = "closeTime",nullable = false ,length = 20)
    private String closeTime;

    @Column(name = "store_created")
    @CreationTimestamp
    private Timestamp screated;

    @Column(name = "store_sleep",nullable = false ,columnDefinition = "TINYINT")
    private int ssleep;

    @Column(name = "minPrice",nullable = false )
    private int minPrice;

    @Column(name = "deliverRadius",nullable = false ,columnDefinition = "TINYINT")
    private int deliverRadius;

    @Column(name = "store_text",nullable = false ,length = 255)
    private String text;



}
