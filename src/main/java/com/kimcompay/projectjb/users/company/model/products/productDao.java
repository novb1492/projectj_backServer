package com.kimcompay.projectjb.users.company.model.products;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface productDao extends JpaRepository<productVo,Integer> {
    
    List<productVo>findByFlyerId(int flyerId);

    @Query(value = "select count(*) from flyers where flyer_id=?",nativeQuery = true)
    int countFlyerByFlyerId(int flyerId);

    @Query(value = "select a.*,b.product_event_id,b.product_event_date,b.product_event_price "
    +"from products a left join product_events b on a.product_id=b.product_id "
    +"where a.product_id=?",nativeQuery = true)
    List<Map<String,Object>>findByIdJoinEvent(int productId);

    @Modifying
    @Transactional
    @Query(value = "update products set product_name=? where product_id=?",nativeQuery = true)
    void updateProductNameById(String productName,int productId);

    
    @Modifying
    @Transactional
    @Query(value = "update products set price=? where product_id=?",nativeQuery = true)
    void updatePriceById(int price,int productId);

       
    @Modifying
    @Transactional
    @Query(value = "update products set origin=? where product_id=?",nativeQuery = true)
    void updateOriginById(String origin,int productId);

    @Modifying
    @Transactional
    @Query(value = "update products set product_img_path=? where product_id=?",nativeQuery = true)
    void updateImgPathById(String ImgPath,int productId);

    @Modifying
    @Transactional
    @Query(value = "update products set text=? where product_id=?",nativeQuery = true)
    void updateTextById(String text,int productId);

    @Modifying
    @Transactional
    @Query(value = "update products set category=? where product_id=?",nativeQuery = true)
    void updateCategoryById(String category,int productId);
}   