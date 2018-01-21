package com.trading.forex.repository;

import com.trading.forex.entity.EconomicCalendar;
import com.trading.forex.model.Importance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by hsouidi on 11/21/2017.
 */
public interface EconomicCalendarRepository extends JpaRepository<EconomicCalendar, EconomicCalendar.EconomicCalendarID> {

    @Query("select u from EconomicCalendar u  where economicCalendarID.eventDate between ?1 and ?2  and importance in ?3 ")
    List<EconomicCalendar> findAllByEventDateAndImportance(Date begin, Date end, List<Importance> importance);
}
