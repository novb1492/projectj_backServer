package com.kimcompay.projectjb.users.company.model.stores;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface storeReviewsDao extends JpaRepository<storeReviewVo,Integer> {
    
    @Query(value = "select store_review_id as id,store_review_created as created,store_review_text as text,store_review_writer as writer"
    +",(select count(*) from store_reviews where store_id=?)totalCount"
    +" from store_reviews where store_id=? order by store_review_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>> findByStoreIdLimit(int storeId,int sameStoreId,int page,int pageSize);

    Optional<storeReviewVo>findByIdAndUserId(int reviewId,int userId);
}
