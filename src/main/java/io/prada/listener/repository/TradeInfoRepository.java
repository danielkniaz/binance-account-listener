package io.prada.listener.repository;

import io.prada.listener.repository.model.TradeInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeInfoRepository extends JpaRepository<TradeInfoEntity, Long> {

}
