package com.kimcompay.projectjb.payments.model.order;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface orderDao extends JpaRepository<orderVo,Integer>{

    @Query(value = "select a.*,b.product_name,b.product_img_path from orders a left join products b on a.product_id=b.product_id where a.order_mcht_trd_no=? and a.store_id=?",nativeQuery=true)
    List<Map<String,Object>>findByJoinProductMchtTrdNo(String mchtTrdNo,int storeId);

    @Query(value = "select a.* from orders a where a.order_mcht_trd_no=? and a.store_id=?",nativeQuery = true)
    List<orderVo>findByMchtTrdNoAndStoreId(String mchtTrdNo,int storeId);

    @Modifying
    @Transactional
    @Query(value ="update orders set oder_cancle_flag=? where order_id=?",nativeQuery = true )
    void updateStateById(int state,int orderId);
}
