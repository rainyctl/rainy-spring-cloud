package cc.rainyctl.services.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cc.rainyctl.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Update("""
        UPDATE t_product
        SET stock = stock - #{count}
        WHERE id = #{productId}
          AND stock >= #{count}
    """)
    int deductStock(@Param("productId") Long productId,
                    @Param("count") int count);
}
